package net.kimon.kimon.power;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.stats.CharacterProfile;
import net.kimon.kimon.stats.ModStatAttachments;
import net.kimon.kimon.stats.StatBlock;
import net.kimon.kimon.stats.StatCalculator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

    private CombatHandler() {
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        StatBlock stats = player.getData(ModStatAttachments.STATS.get());
        CharacterProfile profile = player.getData(ModStatAttachments.PROFILE.get());
        PowerState state = player.getData(ModAttachments.STATE.get());
        MasteryData mastery = player.getData(ModAttachments.MASTERY.get());

        double maxRelease = StatCalculator.maxRelease(stats);
        double maxEnergy = StatCalculator.maxEnergy(stats, profile);
        double maxStamina = StatCalculator.maxStamina(stats, profile);

        PowerState next = state.tick(DT, maxRelease, maxEnergy, maxStamina);

        // Mastery makes the active form cheaper: tick() drained the base amount, so refund the
        // portion saved by mastery (only while still in that form after the tick).
        Form activeForm = next.form();
        if (activeForm != Form.BASE && next.energy() > 0) {
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

        if (!approxEqual(next, state)) {
            player.setData(ModAttachments.STATE.get(), next);
        }
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

        double bonus = StatCalculator.meleeDamageBonus(stats, profile, state.releaseMultiplier())
                * mastery.effectiveDamageMultiplier(state.form());
        if (bonus > 0) {
            event.setAmount(event.getAmount() + (float) bonus);
        }
    }

    @SubscribeEvent
    static void onDamagePost(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            float dealt = event.getInflictedDamage();
            if (dealt > 0) {
                // Action-bar feedback: "Hit for 12.5".
                attacker.sendSystemMessage(
                        Component.translatable("msg.kimon.hit", String.format("%.1f", dealt)), true);
            }
        }
    }

    /**
     * Transforms the player one step up (to the next available form) or down (toward BASE).
     * Validates the target form's tier/Release requirements server-side.
     */
    public static void transform(ServerPlayer player, boolean up) {
        PowerState state = player.getData(ModAttachments.STATE.get());
        PowerData power = player.getData(ModAttachments.POWER.get());
        int tier = PowerScaling.tiers(power.power());

        Form current = state.form();
        if (up) {
            Form target = current.next();
            if (target == null) {
                player.sendSystemMessage(Component.translatable("msg.kimon.form_max"), true);
                return;
            }
            if (!target.isAvailable(tier, state.release())) {
                player.sendSystemMessage(Component.translatable("msg.kimon.form_locked",
                        Component.translatable("form.kimon." + target.key()),
                        target.requiredTier(),
                        (int) target.requiredRelease()), true);
                return;
            }
            player.setData(ModAttachments.STATE.get(), state.withForm(target));
            player.sendSystemMessage(Component.translatable("msg.kimon.form_up",
                    Component.translatable("form.kimon." + target.key())), true);
        } else {
            Form target = current.previous();
            player.setData(ModAttachments.STATE.get(), state.withForm(target));
            player.sendSystemMessage(Component.translatable("msg.kimon.form_down",
                    Component.translatable("form.kimon." + target.key())), true);
        }
    }

    private static boolean approxEqual(PowerState a, PowerState b) {
        return a.charging() == b.charging()
                && a.form() == b.form()
                && Math.abs(a.release() - b.release()) < 1e-4
                && Math.abs(a.energy() - b.energy()) < 1e-4
                && Math.abs(a.stamina() - b.stamina()) < 1e-4;
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

        if (!EnergyBlast.canFire(state.energy())) {
            player.sendSystemMessage(Component.translatable("msg.kimon.no_energy"), true);
            return;
        }

        // Deduct energy immediately (so spamming drains you), re-sync state.
        PowerState afterCost = state.withResources(
                state.release(),
                state.energy() - EnergyBlast.energyCost(),
                state.stamina(),
                StatCalculator.maxRelease(stats),
                StatCalculator.maxEnergy(stats, profile),
                StatCalculator.maxStamina(stats, profile));
        player.setData(ModAttachments.STATE.get(), afterCost);

        HitResult hit = ProjectileUtil.getHitResultOnViewVector(
                player,
                e -> e != player && e.isPickable() && e instanceof LivingEntity,
                EnergyBlast.RANGE);

        if (hit.getType() == HitResult.Type.ENTITY
                && ((EntityHitResult) hit).getEntity() instanceof LivingEntity target) {
            double dmg = EnergyBlast.damage(stats, profile, state.releaseMultiplier())
                    * mastery.effectiveDamageMultiplier(state.form());
            DamageSource source = player.damageSources().indirectMagic(player, player);
            if (player.level() instanceof ServerLevel serverLevel) {
                target.hurtServer(serverLevel, source, (float) dmg);
            }
            player.sendSystemMessage(
                    Component.translatable("msg.kimon.blast_hit", String.format("%.1f", dmg)), true);
        } else {
            player.sendSystemMessage(Component.translatable("msg.kimon.blast_miss"), true);
        }
    }
}
