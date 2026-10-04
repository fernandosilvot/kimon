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

    /** Multiplier (1 + combined race/class modifier) for an attribute, floored at 0. */
    private static double mod(CharacterProfile profile, Attribute attribute) {
        if (profile == null) {
            return 1.0;
        }
        return Math.max(0.0, 1.0 + profile.totalModifier(attribute));
    }

    // --- Bonuses WITH race/class profile (used in-game) ---

    public static double bonusHealth(StatBlock stats, CharacterProfile profile) {
        return bonusHealth(stats) * mod(profile, Attribute.VITALITY);
    }

    public static double bonusAttackDamage(StatBlock stats, CharacterProfile profile) {
        return bonusAttackDamage(stats) * mod(profile, Attribute.STRENGTH);
    }

    public static double bonusMovementSpeed(StatBlock stats, CharacterProfile profile) {
        return bonusMovementSpeed(stats) * mod(profile, Attribute.AGILITY);
    }

    public static double maxEnergy(StatBlock stats, CharacterProfile profile) {
        return maxEnergy(stats) * mod(profile, Attribute.SPIRIT);
    }

    public static double maxStamina(StatBlock stats, CharacterProfile profile) {
        return maxStamina(stats) * mod(profile, Attribute.VITALITY);
    }

    public static double meleeDamageBonus(StatBlock stats, CharacterProfile profile, double releaseFraction) {
        double clamped = Math.max(0.0, Math.min(1.0, releaseFraction));
        return bonusAttackDamage(stats, profile) * clamped;
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

    // --- Resource maxima (Phase 3) ---

    /** Max Energy (Ki) per SPIRIT point. */
    public static final double ENERGY_PER_SPIRIT = 40.0;
    /** Max Stamina per VITALITY point. */
    public static final double STAMINA_PER_VITALITY = 3.5;
    /** Extra Release ceiling per FOCUS point above start (a stand-in for "Potential Unlock"). */
    public static final double RELEASE_PER_FOCUS = 1.0;
    /** Hard cap on the bonus Release ceiling from FOCUS. */
    public static final double MAX_BONUS_RELEASE = 50.0;

    /** Maximum Energy pool from SPIRIT. */
    public static double maxEnergy(StatBlock stats) {
        return stats.get(Attribute.SPIRIT) * ENERGY_PER_SPIRIT;
    }

    /** Maximum Stamina pool from VITALITY. */
    public static double maxStamina(StatBlock stats) {
        return stats.get(Attribute.VITALITY) * STAMINA_PER_VITALITY;
    }

    /** The player's Release ceiling: base 50 plus a FOCUS-driven bonus, capped at 100. */
    public static double maxRelease(StatBlock stats) {
        double bonus = Math.min(MAX_BONUS_RELEASE, above(stats, Attribute.FOCUS) * RELEASE_PER_FOCUS);
        return Math.min(100.0, 50.0 + bonus);
    }

    /**
     * Effective melee damage bonus when attacking: Strength bonus scaled by the current Release
     * fraction. At 0% Release you get none of it (you must "power up" to hit hard).
     *
     * @param stats            the attacker's attributes
     * @param releaseFraction  release/100, in [0,1]
     * @return bonus melee damage to add on top of the vanilla hit
     */
    public static double meleeDamageBonus(StatBlock stats, double releaseFraction) {
        double clamped = Math.max(0.0, Math.min(1.0, releaseFraction));
        return bonusAttackDamage(stats) * clamped;
    }
}
