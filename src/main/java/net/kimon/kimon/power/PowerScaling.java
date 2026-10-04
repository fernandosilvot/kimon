package net.kimon.kimon.power;

/**
 * Pure, Minecraft-free translation of a {@link PowerData} value into concrete combat bonuses.
 *
 * <p>This is the balancing core of Kimon's progression: it decides how much a given amount of Power
 * is "worth" as bonus max-health, attack damage, and movement speed. Keeping it a plain static
 * helper (no entity/world references) means every balancing rule is unit-testable on a plain JVM.</p>
 *
 * <h2>Design</h2>
 * Bonuses scale in discrete <em>tiers</em>: every {@link #POWER_PER_TIER} points of Power grants one
 * tier. The number of tiers is capped at {@link #MAX_TIERS} so bonuses can never run away. Each
 * tier adds a fixed, deliberately conservative amount per attribute.
 *
 * <p>Mechanics here are inspired by the general idea of training-driven RPG progression (as seen in
 * many mods and games) but all names, numbers, and formulas are original to Kimon.</p>
 */
public final class PowerScaling {

    private PowerScaling() {
    }

    /** How much Power is needed to earn one bonus tier. */
    public static final int POWER_PER_TIER = 25;

    /** Maximum number of tiers a player can reach (keeps bonuses bounded). */
    public static final int MAX_TIERS = 50;

    /** Bonus max health (half-hearts) granted per tier. 2.0 = one full heart. */
    public static final double HEALTH_PER_TIER = 2.0;

    /** Bonus attack damage granted per tier. */
    public static final double ATTACK_PER_TIER = 0.5;

    /**
     * Bonus movement speed granted per tier, as a fraction of the vanilla base (0.1).
     * 0.004 per tier => +2% of base speed per tier, +100% at MAX_TIERS... so we keep it small.
     */
    public static final double SPEED_PER_TIER = 0.002;

    /**
     * The number of bonus tiers earned for a given Power value.
     *
     * @param power the player's current Power (already clamped by {@link PowerData})
     * @return tier count in {@code [0, MAX_TIERS]}
     */
    public static int tiers(int power) {
        if (power <= 0) {
            return 0;
        }
        int tiers = power / POWER_PER_TIER;
        return Math.min(tiers, MAX_TIERS);
    }

    /** Bonus max-health (in half-hearts) for the given Power. */
    public static double bonusHealth(int power) {
        return tiers(power) * HEALTH_PER_TIER;
    }

    /** Bonus attack damage for the given Power. */
    public static double bonusAttackDamage(int power) {
        return tiers(power) * ATTACK_PER_TIER;
    }

    /** Bonus movement speed (flat add to the speed attribute) for the given Power. */
    public static double bonusMovementSpeed(int power) {
        return tiers(power) * SPEED_PER_TIER;
    }
}
