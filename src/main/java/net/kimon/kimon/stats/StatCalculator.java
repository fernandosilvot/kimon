package net.kimon.kimon.stats;

/**
 * Pure, Minecraft-free translation of a {@link StatBlock}'s six attributes into derived combat
 * values. All balancing math lives here so it can be unit-tested on a plain JVM.
 *
 * <p>The formulas follow the design notes (see {@code docs/DESIGN.md}), expressed as bonuses
 * <em>above</em> the vanilla baseline so they can be applied as additive attribute modifiers:
 * only the points above the starting value contribute, so a brand-new character plays like
 * vanilla.</p>
 */
public final class StatCalculator {

    private StatCalculator() {
    }

    /** Bonus max-health (half-hearts) per VITALITY point above the start value. */
    public static final double HEALTH_PER_VITALITY = 0.4;

    /** Bonus attack damage per STRENGTH point above the start value. */
    public static final double DAMAGE_PER_STRENGTH = 0.1;

    /** Bonus movement speed (flat add to the 0.1 base) per AGILITY point above the start value. */
    public static final double SPEED_PER_AGILITY = 0.0008;

    /** Caps keep bonuses bounded regardless of how high an attribute is pushed. */
    public static final double MAX_BONUS_HEALTH = 400.0;   // +200 hearts
    public static final double MAX_BONUS_DAMAGE = 200.0;
    public static final double MAX_BONUS_SPEED = 0.1;      // +100% of base speed

    private static int above(StatBlock stats, Attribute attribute) {
        return Math.max(0, stats.get(attribute) - StatBlock.START_VALUE);
    }

    /** Bonus max health contributed by VITALITY. */
    public static double bonusHealth(StatBlock stats) {
        return Math.min(MAX_BONUS_HEALTH, above(stats, Attribute.VITALITY) * HEALTH_PER_VITALITY);
    }

    /** Bonus attack damage contributed by STRENGTH. */
    public static double bonusAttackDamage(StatBlock stats) {
        return Math.min(MAX_BONUS_DAMAGE, above(stats, Attribute.STRENGTH) * DAMAGE_PER_STRENGTH);
    }

    /** Bonus movement speed contributed by AGILITY. */
    public static double bonusMovementSpeed(StatBlock stats) {
        return Math.min(MAX_BONUS_SPEED, above(stats, Attribute.AGILITY) * SPEED_PER_AGILITY);
    }
}
