package net.kimon.kimon.power;

import net.kimon.kimon.Kimon;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Applies the combat bonuses computed by {@link PowerScaling} to a player's vanilla attributes,
 * using transient {@link AttributeModifier}s keyed by stable {@link Identifier}s.
 *
 * <p>Because each modifier has a fixed id, re-applying simply overwrites the previous one via
 * {@link AttributeInstance#addOrUpdateTransientModifier}. The modifiers are <em>transient</em> (not
 * serialized onto the attribute) because Power itself is the serialized source of truth — on every
 * login/respawn we recompute and re-apply from the stored Power, which keeps the two from drifting
 * or stacking.</p>
 *
 * <p>All work here is server-side. Health/damage/speed are authoritative on the logical server;
 * the client sees the result through normal attribute syncing.</p>
 */
public final class PowerEffects {

    private PowerEffects() {
    }

    private static final Identifier HEALTH_ID = Identifier.fromNamespaceAndPath(Kimon.MODID, "power_health");
    private static final Identifier ATTACK_ID = Identifier.fromNamespaceAndPath(Kimon.MODID, "power_attack");
    private static final Identifier SPEED_ID = Identifier.fromNamespaceAndPath(Kimon.MODID, "power_speed");

    /**
     * Recomputes and applies all Power-derived attribute bonuses for the given player from their
     * current {@link PowerData}. Safe to call repeatedly (idempotent for a given Power value).
     *
     * @param player the player to update (server-side)
     */
    public static void apply(Player player) {
        int power = player.getData(ModAttachments.POWER.get()).power();

        applyModifier(player, Attributes.MAX_HEALTH, HEALTH_ID,
                PowerScaling.bonusHealth(power), AttributeModifier.Operation.ADD_VALUE);
        applyModifier(player, Attributes.ATTACK_DAMAGE, ATTACK_ID,
                PowerScaling.bonusAttackDamage(power), AttributeModifier.Operation.ADD_VALUE);
        applyModifier(player, Attributes.MOVEMENT_SPEED, SPEED_ID,
                PowerScaling.bonusMovementSpeed(power), AttributeModifier.Operation.ADD_VALUE);

        // Clamp current health into the (possibly changed) max so lowering a bonus can't leave the
        // player with more health than allowed.
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void applyModifier(Player player, Holder<Attribute> attribute, Identifier id,
                                      double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        // Remove any previous value first, then add the fresh one (addOrUpdate also works, but an
        // explicit remove keeps behavior obvious when the bonus drops to zero).
        instance.removeModifier(id);
        if (amount != 0.0) {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }
}
