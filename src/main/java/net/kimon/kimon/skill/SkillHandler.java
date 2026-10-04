package net.kimon.kimon.skill;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.config.KimonConfig;
import net.kimon.kimon.stats.Attribute;
import net.kimon.kimon.stats.ModStatAttachments;
import net.kimon.kimon.stats.StatBlock;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Server-side skill glue: learning (TP + Mind validated by {@link SkillRules}), and applying the
 * passive effects — Endurance on incoming damage, Jump on the jump/fall attributes. Flight, Dash and
 * the Ki Sense readout have their own classes.
 */
@EventBusSubscriber(modid = Kimon.MODID)
public final class SkillHandler {

    private static final Identifier JUMP_ID = Identifier.fromNamespaceAndPath(Kimon.MODID, "skill_jump");
    private static final Identifier FALL_ID = Identifier.fromNamespaceAndPath(Kimon.MODID, "skill_fall");

    private SkillHandler() {
    }

    /** Raises a skill by one level for the player, telling them what happened. */
    public static void learn(ServerPlayer player, Identifier id) {
        SkillCatalog catalog = SkillCatalog.current();
        StatBlock stats = player.getData(ModStatAttachments.STATS.get());
        SkillData data = player.getData(ModSkillAttachments.SKILLS.get());
        int budget = SkillRules.mindBudget(stats.get(Attribute.MIND), KimonConfig.skillParams().mindPerPoint());

        SkillRules.Result result = SkillRules.learn(data, catalog, id, stats.trainingPoints(), budget);
        Component name = skillName(id);
        switch (result.outcome()) {
            case LEARNED -> {
                player.setData(ModSkillAttachments.SKILLS.get(), result.data());
                player.setData(ModStatAttachments.STATS.get(), stats.addTrainingPoints(-result.tpSpent()));
                applyEffects(player);
                player.sendSystemMessage(Component.translatable("msg.kimon.skill_learned", name,
                        result.data().level(id)), true);
            }
            case MAX_LEVEL -> player.sendSystemMessage(Component.translatable("msg.kimon.skill_max", name), true);
            case NOT_ENOUGH_TP -> player.sendSystemMessage(Component.translatable("msg.kimon.skill_no_tp", name), true);
            case NOT_ENOUGH_MIND -> player.sendSystemMessage(Component.translatable("msg.kimon.skill_no_mind", name), true);
            case UNKNOWN_SKILL -> player.sendSystemMessage(Component.translatable("msg.kimon.skill_unknown"), true);
        }
    }

    /** The display name of a skill (falls back to its id path for datapack skills without a lang entry). */
    public static Component skillName(Identifier id) {
        return Component.translatableWithFallback("skill." + id.getNamespace() + "." + id.getPath(), id.getPath());
    }

    /** Re-applies the attribute-based effects (Jump) from the player's current skills. */
    public static void applyEffects(Player player) {
        SkillData data = player.getData(ModSkillAttachments.SKILLS.get());
        SkillCatalog catalog = SkillCatalog.current();
        set(player, Attributes.JUMP_STRENGTH, JUMP_ID,
                SkillEffects.total(data, catalog, SkillEffect.JUMP_BOOST) / 100.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        set(player, Attributes.SAFE_FALL_DISTANCE, FALL_ID,
                SkillEffects.total(data, catalog, SkillEffect.SAFE_FALL), AttributeModifier.Operation.ADD_VALUE);
    }

    private static void set(Player player, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                            Identifier id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        if (amount != 0.0) {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }

    /** Endurance: a fraction of every hit the player takes is absorbed. */
    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            double factor = SkillEffects.damageTakenFactor(
                    player.getData(ModSkillAttachments.SKILLS.get()), SkillCatalog.current());
            if (factor < 1.0) {
                event.setAmount((float) (event.getAmount() * factor));
            }
        }
    }

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            applyEffects(player);
        }
    }

    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            applyEffects(player);
        }
    }
}
