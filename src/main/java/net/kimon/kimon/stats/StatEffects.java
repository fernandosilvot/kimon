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

        set(player, Attributes.MAX_HEALTH, HEALTH_ID, StatCalculator.bonusHealth(stats));
        set(player, Attributes.ATTACK_DAMAGE, DAMAGE_ID, StatCalculator.bonusAttackDamage(stats));
        set(player, Attributes.MOVEMENT_SPEED, SPEED_ID, StatCalculator.bonusMovementSpeed(stats));

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
