package net.kimon.kimon.power;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.config.KimonConfig;
import net.kimon.kimon.stats.CharacterProfile;
import net.kimon.kimon.stats.ModStatAttachments;
import net.kimon.kimon.stats.StatBlock;
import net.kimon.kimon.skill.ModSkillAttachments;
import net.kimon.kimon.skill.SkillData;
import net.kimon.kimon.skill.SkillHandler;
import net.kimon.kimon.stats.Attribute;
import net.kimon.kimon.stats.StatEffects;
import net.kimon.kimon.stats.StatCalculator;
import net.kimon.kimon.stats.TpGain;
import net.kimon.kimon.stats.TpParams;
import net.kimon.kimon.training.TrainingEffects;
import net.kimon.kimon.training.TrainingLoad;
import net.kimon.kimon.training.TrainingParams;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Server-side combat + resource loop for Phase 3.
 *
 * <ul>
 *   <li>{@link PlayerTickEvent.Post}: advances each player's {@link PowerState} (Release charge/decay,
 *       Energy/Stamina regen) from their attribute-derived maxima.</li>
 *   <li>{@link LivingIncomingDamageEvent}: when a Kimon player lands a melee hit, adds a Strength×
 *       Release damage bonus on top of the vanilla hit.</li>
 *   <li>{@link LivingDamageEvent.Post}: tells the attacking player how much damage they actually
 *       dealt (the "see how hard you hit" feedback).</li>
 * </ul>
 */
@EventBusSubscriber(modid = Kimon.MODID)
public final class CombatHandler {

    private static final double DT = 0.05; // one tick = 1/20 s

    /**
     * Players whose current melee hit was empowered (paid its Energy/Stamina cost). Set in the
     * incoming-damage event, consumed in the post-damage event of the same hit, and cleared each tick
     * so a cancelled hit can never leak into the next one. Server thread only.
     */
    private static final Set<UUID> PAID_HITS = new HashSet<>();

    private CombatHandler() {
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PAID_HITS.remove(player.getUUID());
        StatBlock stats = player.getData(ModStatAttachments.STATS.get());
        CharacterProfile profile = player.getData(ModStatAttachments.PROFILE.get());
        PowerState state = player.getData(ModAttachments.STATE.get());
        MasteryData mastery = player.getData(ModAttachments.MASTERY.get());

        // A form the race can't use (e.g. after changing race or a datapack reload) is dropped.
        if (!FormRules.validFor(FormCatalog.current(), profile.raceId(), state.form())) {
            state = state.withForm(Form.BASE);
        }

        PowerParams params = KimonConfig.params();
        double maxRelease = ReleaseCeiling.of(player);
        double maxEnergy = StatCalculator.maxEnergy(stats, profile, params.kiPerSpirit());
        double maxStamina = StatCalculator.maxStamina(stats, profile);

        PowerState next = state.tick(DT, maxRelease, maxEnergy, maxStamina, params);

        // Mastery makes the active form cheaper: tick() drained the base amount, so refund the
        // portion saved by mastery (only while still in that form after the tick).
        Form activeForm = next.form();
        if (!activeForm.isBase() && next.energy() > 0) {
            double saved = (activeForm.energyDrainPerSecond() - mastery.effectiveDrain(activeForm)) * DT;
            if (saved > 0) {
                next = next.withResources(next.release(), next.energy() + saved, next.stamina(),
                        maxRelease, maxEnergy, maxStamina);
            }
            // Practising the form raises its mastery over time.
            MasteryData grown = mastery.practice(activeForm, DT);
            if (grown.level(activeForm) != mastery.level(activeForm)) {
                player.setData(ModAttachments.MASTERY.get(), grown);
            }
        }

        boolean formChanged = !next.form().equals(player.getData(ModAttachments.STATE.get()).form());
        if (!SyncPolicy.sameForSync(next, state) || next.regenLock() != state.regenLock() || formChanged) {
            player.setData(ModAttachments.STATE.get(), next);
        }
        if (formChanged) {
            StatEffects.apply(player); // the form's Dexterity multiplier changes the speed bonus
        }

        // Throttled send of the (possibly updated) state to the owner and the aura to neighbours.
        SyncHandler.tick(player);
    }

    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker) || attacker.level().isClientSide()) {
            return;
        }
        // Only amplify direct melee (the attacker is the direct damage dealer).
        if (event.getSource().getDirectEntity() != attacker) {
            return;
        }

        StatBlock stats = attacker.getData(ModStatAttachments.STATS.get());
        CharacterProfile profile = attacker.getData(ModStatAttachments.PROFILE.get());
        PowerState state = attacker.getData(ModAttachments.STATE.get());
        MasteryData mastery = attacker.getData(ModAttachments.MASTERY.get());

        // Without an active Release the hit is plain vanilla: no bonus, no cost, no TP.
        if (!state.isActive()) {
            return;
        }

        // An empowered hit costs Energy and Stamina; if you can't pay, you hit with vanilla damage.
        PowerParams params = KimonConfig.params();
        double maxRelease = ReleaseCeiling.of(attacker);
        double maxEnergy = StatCalculator.maxEnergy(stats, profile, params.kiPerSpirit());
        double maxStamina = StatCalculator.maxStamina(stats, profile);
        double kiCost = CombatCosts.kiPerHit(stats.get(Attribute.STRENGTH));
        double staminaCost = CombatCosts.staminaPerHit(maxStamina, params.hitStaminaCost());
        if (!CombatCosts.canPay(state.energy(), state.stamina(), kiCost, staminaCost)) {
            if (attacker instanceof ServerPlayer serverAttacker) {
                serverAttacker.sendSystemMessage(Component.translatable("msg.kimon.hit_weak"), true);
            }
            return;
        }
        attacker.setData(ModAttachments.STATE.get(), state.withResources(
                state.release(), state.energy() - kiCost, state.stamina() - staminaCost,
                maxRelease, maxEnergy, maxStamina));
        PAID_HITS.add(attacker.getUUID());

        // Carried weight lowers damage and gravity lowers STR; both scale the bonus part of the hit.
        TrainingLoad load = attacker.getData(ModStatAttachments.LOAD.get());
        TrainingParams training = KimonConfig.trainingParams();
        FormEffect form = FormEffect.of(state.form().def(), mastery.damageBonus(state.form()));
        double bonus = StatCalculator.meleeDamageBonus(form.strength(stats.get(Attribute.STRENGTH)), profile,
                        state.releaseMultiplier())
                * TrainingEffects.damageFactor(load, training)
                * TrainingEffects.statFactor(load, training);
        if (bonus > 0) {
            event.setAmount(event.getAmount() + (float) bonus);
        }
    }

    @SubscribeEvent
    static void onDamagePost(LivingDamageEvent.Post event) {
        float dealt = event.getInflictedDamage();

        // Being hurt by a living entity locks Energy regeneration for a while.
        if (dealt > 0 && event.getEntity() instanceof ServerPlayer victim
                && event.getSource().getEntity() instanceof LivingEntity) {
            PowerState victimState = victim.getData(ModAttachments.STATE.get());
            victim.setData(ModAttachments.STATE.get(),
                    victimState.hurt(KimonConfig.params().regenLockTicks()));
        }

        // Only direct melee: Energy Blast awards its own TP in fireEnergyBlast.
        if (event.getSource().getEntity() instanceof ServerPlayer attacker
                && event.getSource().getDirectEntity() == attacker && dealt > 0) {
            // TP only comes from empowered hits (the ones that paid their cost).
            long tp = PAID_HITS.remove(attacker.getUUID()) ? awardTp(attacker, event.getEntity()) : 0L;
            // Action-bar feedback: "Hit for 12.5" (+ the TP earned, if any).
            attacker.sendSystemMessage(tp > 0
                    ? Component.translatable("msg.kimon.hit_tp", String.format("%.1f", dealt), tp)
                    : Component.translatable("msg.kimon.hit", String.format("%.1f", dealt)), true);
        }
    }

    /**
     * Rolls and grants Training Points for a hit on {@code target}: needs Release of at least 5% and
     * a successful probability roll. Against another player the target's FOCUS is used (as in the
     * research); otherwise the attacker's. This is the only organic source of TP.
     *
     * @return the TP granted (0 if none)
     */
    static long awardTp(ServerPlayer attacker, LivingEntity target) {
        PowerState state = attacker.getData(ModAttachments.STATE.get());
        if (!TpGain.eligible(state.release()) || target == attacker) {
            return 0L;
        }
        StatBlock source = target instanceof ServerPlayer other
                ? other.getData(ModStatAttachments.STATS.get())
                : attacker.getData(ModStatAttachments.STATS.get());
        TpParams params = KimonConfig.tpParams();
        double chanceMultiplier = TrainingEffects.tpChanceMultiplier(
                attacker.getData(ModStatAttachments.LOAD.get()), KimonConfig.trainingParams());
        long tp = TpGain.forHit(source.get(Attribute.MIND), state.release(),
                attacker.getRandom().nextDouble(), params, chanceMultiplier);
        if (tp > 0) {
            StatBlock own = attacker.getData(ModStatAttachments.STATS.get());
            attacker.setData(ModStatAttachments.STATS.get(), own.addTrainingPoints(tp));
        }
        return tp;
    }

    /**
     * Transforms the player one rung up their race's ladder, or one down (toward Base). Going up needs
     * the racial skill level the form asks for and enough Release; everything is checked server-side.
     */
    public static void transform(ServerPlayer player, boolean up) {
        PowerState state = player.getData(ModAttachments.STATE.get());
        CharacterProfile profile = player.getData(ModStatAttachments.PROFILE.get());
        SkillData skills = player.getData(ModSkillAttachments.SKILLS.get());
        FormCatalog catalog = FormCatalog.current();
        java.util.List<Form> ladder = FormRules.ladder(catalog, profile.raceId());

        if (!up) {
            Form target = FormRules.previous(ladder, state.form());
            player.setData(ModAttachments.STATE.get(), state.withForm(target));
            StatEffects.apply(player);
            player.sendSystemMessage(Component.translatable("msg.kimon.form_down", formName(target)), true);
            return;
        }

        Form target = FormRules.next(ladder, state.form());
        if (target == null) {
            player.sendSystemMessage(Component.translatable(ladder.isEmpty()
                    ? "msg.kimon.form_none" : "msg.kimon.form_max"), true);
            return;
        }
        FormDef def = catalog.get(target.id());
        switch (FormRules.canEnter(catalog, profile.raceId(), skills, target, state.release())) {
            case OK -> {
                player.setData(ModAttachments.STATE.get(), state.withForm(target));
                StatEffects.apply(player);
                player.sendSystemMessage(Component.translatable("msg.kimon.form_up", formName(target)), true);
            }
            case SKILL_LOCKED -> player.sendSystemMessage(Component.translatable("msg.kimon.form_locked_skill",
                    formName(target), SkillHandler.skillName(def.skill()), def.skillLevel()), true);
            case NEEDS_RELEASE -> player.sendSystemMessage(Component.translatable("msg.kimon.form_locked_release",
                    formName(target), (int) def.minRelease()), true);
            default -> player.sendSystemMessage(Component.translatable("msg.kimon.form_none"), true);
        }
    }

    /** The display name of a form (falls back to its id path for datapack forms without a lang entry). */
    public static Component formName(Form form) {
        return Component.translatableWithFallback(
                "form." + form.id().getNamespace() + "." + form.id().getPath(), form.id().getPath());
    }

    /** Damage the player takes is divided by the active form's divisor (stronger forms are tougher). */
    @SubscribeEvent
    static void onIncomingDamageToPlayer(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            double divisor = player.getData(ModAttachments.STATE.get()).form().def().damageTakenDivisor();
            if (divisor > 1.0) {
                event.setAmount((float) (event.getAmount() / divisor));
            }
        }
    }

    /**
     * Fires an Energy Blast from the given server player: raycasts along their view, and if it hits
     * a living entity within range, deducts Energy and deals Energy×Release-scaled magic damage.
     * Server-authoritative — called from the fire-blast packet handler.
     */
    public static void fireEnergyBlast(ServerPlayer player) {
        StatBlock stats = player.getData(ModStatAttachments.STATS.get());
        CharacterProfile profile = player.getData(ModStatAttachments.PROFILE.get());
        PowerState state = player.getData(ModAttachments.STATE.get());
        MasteryData mastery = player.getData(ModAttachments.MASTERY.get());
        PowerParams params = KimonConfig.params();

        if (!EnergyBlast.canFire(state.energy())) {
            player.sendSystemMessage(Component.translatable("msg.kimon.no_energy"), true);
            return;
        }

        // Deduct energy immediately (so spamming drains you), re-sync state.
        PowerState afterCost = state.withResources(
                state.release(),
                state.energy() - EnergyBlast.energyCost(),
                state.stamina(),
                ReleaseCeiling.of(player),
                StatCalculator.maxEnergy(stats, profile, params.kiPerSpirit()),
                StatCalculator.maxStamina(stats, profile));
        player.setData(ModAttachments.STATE.get(), afterCost);

        HitResult hit = ProjectileUtil.getHitResultOnViewVector(
                player,
                e -> e != player && e.isPickable() && e instanceof LivingEntity,
                EnergyBlast.RANGE);

        if (hit.getType() == HitResult.Type.ENTITY
                && ((EntityHitResult) hit).getEntity() instanceof LivingEntity target) {
            FormEffect form = FormEffect.of(state.form().def(), mastery.damageBonus(state.form()));
            double dmg = EnergyBlast.damage(form.willpower(stats.get(Attribute.WILLPOWER)), profile,
                    state.releaseMultiplier());
            DamageSource source = player.damageSources().indirectMagic(player, player);
            if (player.level() instanceof ServerLevel serverLevel) {
                target.hurtServer(serverLevel, source, (float) dmg);
            }
            long tp = awardTp(player, target);
            player.sendSystemMessage(tp > 0
                    ? Component.translatable("msg.kimon.blast_hit_tp", String.format("%.1f", dmg), tp)
                    : Component.translatable("msg.kimon.blast_hit", String.format("%.1f", dmg)), true);
        } else {
            player.sendSystemMessage(Component.translatable("msg.kimon.blast_miss"), true);
        }
    }
}
