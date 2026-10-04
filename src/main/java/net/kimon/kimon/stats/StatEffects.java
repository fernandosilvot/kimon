package net.kimon.kimon.stats;

import net.kimon.kimon.Kimon;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Applies the derived values from {@link StatCalculator} to a player's vanilla attributes, using
 * transient {@link AttributeModifier}s keyed by stable {@link Identifier}s.
 *
 * <p>Server-side and idempotent: re-applying overwrites the previous modifiers from the current
 * {@link StatBlock}, so values can never stack or drift. Call after any attribute change and on
 * login/respawn.</p>
 */
public final class StatEffects {

    private StatEffects() {
    }

    private static final Identifier HEALTH_ID = Identifier.fromNamespaceAndPath(Kimon.MODID, "stat_health");
    private static final Identifier DAMAGE_ID = Identifier.fromNamespaceAndPath(Kimon.MODID, "stat_damage");
    private static final Identifier SPEED_ID = Identifier.fromNamespaceAndPath(Kimon.MODID, "stat_speed");

    /** Recomputes and applies all attribute-derived bonuses for the player from their StatBlock. */
    public static void apply(Player player) {
        StatBlock stats = player.getData(ModStatAttachments.STATS.get());
        CharacterProfile profile = player.getData(ModStatAttachments.PROFILE.get());

        set(player, Attributes.MAX_HEALTH, HEALTH_ID, StatCalculator.bonusHealth(stats, profile));
        set(player, Attributes.ATTACK_DAMAGE, DAMAGE_ID, StatCalculator.bonusAttackDamage(stats, profile));
        net.kimon.kimon.training.TrainingLoad load = player.getData(ModStatAttachments.LOAD.get());
        double dexFactor = net.kimon.kimon.training.TrainingEffects.statFactor(load,
                net.kimon.kimon.config.KimonConfig.trainingParams());
        net.kimon.kimon.power.PowerState state = player.getData(net.kimon.kimon.power.ModAttachments.STATE.get());
        net.kimon.kimon.power.MasteryData mastery = player.getData(net.kimon.kimon.power.ModAttachments.MASTERY.get());
        net.kimon.kimon.power.FormEffect form = net.kimon.kimon.power.FormEffect.of(
                state.form().def(), mastery.damageBonus(state.form()));
        set(player, Attributes.MOVEMENT_SPEED, SPEED_ID,
                StatCalculator.bonusMovementSpeed(form.dexterity(stats.get(net.kimon.kimon.stats.Attribute.DEXTERITY)), profile) * dexFactor);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void set(Player player, Holder<Attribute> attribute, Identifier id, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        if (amount != 0.0) {
            instance.addOrUpdateTransientModifier(
                    new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
