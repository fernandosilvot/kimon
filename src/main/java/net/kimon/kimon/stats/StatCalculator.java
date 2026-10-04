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

    private static StatMods mods(CharacterProfile profile) {
        return profile == null ? StatMods.NONE : profile.mods();
    }

    // --- Bonuses WITH race/class modifiers (used in-game). Each derived stat has its own modifier
    // column from the design tables: Body, Melee, Run, Max Ki, Stamina. ---

    public static double bonusHealth(StatBlock stats, CharacterProfile profile) {
        return bonusHealth(stats) * StatMods.factor(mods(profile).body());
    }

    public static double bonusAttackDamage(StatBlock stats, CharacterProfile profile) {
        return bonusAttackDamage(stats) * StatMods.factor(mods(profile).melee());
    }

    public static double bonusMovementSpeed(StatBlock stats, CharacterProfile profile) {
        return bonusMovementSpeed(stats) * StatMods.factor(mods(profile).run());
    }

    public static double maxEnergy(StatBlock stats, CharacterProfile profile) {
        return maxEnergy(stats) * StatMods.factor(mods(profile).maxKi());
    }

    /** Max Energy with a configurable Energy-per-SPIRIT ratio ({@code ki.perSPI}). */
    public static double maxEnergy(StatBlock stats, CharacterProfile profile, double energyPerSpirit) {
        return maxEnergy(stats, energyPerSpirit) * StatMods.factor(mods(profile).maxKi());
    }

    public static double maxStamina(StatBlock stats, CharacterProfile profile) {
        return maxStamina(stats) * StatMods.factor(mods(profile).stamina());
    }

    /** Melee bonus for an effective STRENGTH value, with race/class modifiers, scaled by Release. */
    public static double meleeDamageBonus(double effectiveStrength, CharacterProfile profile, double releaseFraction) {
        double clamped = Math.max(0.0, Math.min(1.0, releaseFraction));
        return bonusAttackDamageFor(effectiveStrength) * StatMods.factor(mods(profile).melee()) * clamped;
    }

    /** Movement-speed bonus for an effective DEXTERITY value, with race/class modifiers. */
    public static double bonusMovementSpeed(double effectiveDexterity, CharacterProfile profile) {
        return bonusMovementSpeedFor(effectiveDexterity) * StatMods.factor(mods(profile).run());
    }

    public static double meleeDamageBonus(StatBlock stats, CharacterProfile profile, double releaseFraction) {
        double clamped = Math.max(0.0, Math.min(1.0, releaseFraction));
        return bonusAttackDamage(stats, profile) * clamped;
    }

    /** Bonus max health contributed by VITALITY. */
    public static double bonusHealth(StatBlock stats) {
        return Math.min(MAX_BONUS_HEALTH, above(stats, Attribute.CONSTITUTION) * HEALTH_PER_VITALITY);
    }

    /** Bonus attack damage contributed by STRENGTH. */
    public static double bonusAttackDamage(StatBlock stats) {
        return bonusAttackDamageFor(stats.get(Attribute.STRENGTH));
    }

    /** Bonus attack damage for an effective STRENGTH value (e.g. after a form's multiplier). */
    public static double bonusAttackDamageFor(double effectiveStrength) {
        return Math.min(MAX_BONUS_DAMAGE,
                Math.max(0.0, effectiveStrength - StatBlock.START_VALUE) * DAMAGE_PER_STRENGTH);
    }

    /** Bonus movement speed contributed by DEXTERITY. */
    public static double bonusMovementSpeed(StatBlock stats) {
        return bonusMovementSpeedFor(stats.get(Attribute.DEXTERITY));
    }

    /** Bonus movement speed for an effective DEXTERITY value (e.g. after a form's multiplier). */
    public static double bonusMovementSpeedFor(double effectiveDexterity) {
        return Math.min(MAX_BONUS_SPEED,
                Math.max(0.0, effectiveDexterity - StatBlock.START_VALUE) * SPEED_PER_AGILITY);
    }

    // --- Resource maxima (Phase 3) ---

    /** Max Energy (Ki) per SPIRIT point. */
    public static final double ENERGY_PER_SPIRIT = 40.0;
    /** Max Stamina per VITALITY point. */
    public static final double STAMINA_PER_VITALITY = 3.5;
    /** Maximum Energy pool from SPIRIT. */
    public static double maxEnergy(StatBlock stats) {
        return maxEnergy(stats, ENERGY_PER_SPIRIT);
    }

    /** Maximum Energy pool from SPIRIT with an explicit per-point ratio. */
    public static double maxEnergy(StatBlock stats, double energyPerSpirit) {
        return stats.get(Attribute.SPIRIT) * energyPerSpirit;
    }

    /** Maximum Stamina pool from VITALITY. */
    public static double maxStamina(StatBlock stats) {
        return stats.get(Attribute.CONSTITUTION) * STAMINA_PER_VITALITY;
    }

    /**
     * The Release ceiling: {@code baseMax} plus the bonus from skills (Potential Unlock: +5% per level,
     * so 50% → 100% at level 10), never above {@code hardMax}.
     */
    public static double maxRelease(double baseMax, double skillBonus, double hardMax) {
        return Math.min(hardMax, Math.max(0.0, baseMax + skillBonus));
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
