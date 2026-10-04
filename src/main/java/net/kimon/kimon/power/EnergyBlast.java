package net.kimon.kimon.power;

import net.kimon.kimon.stats.Attribute;
import net.kimon.kimon.stats.CharacterProfile;
import net.kimon.kimon.stats.StatBlock;

/**
 * Pure (Minecraft-free) rules for the Energy Blast attack: how much damage it deals and how much
 * Energy it costs, from the attacker's Energy attribute, race/class profile and current Release.
 *
 * <p>Keeping this a pure helper means the balance is unit-testable without the game. The actual
 * raycast, Energy deduction and damage application live server-side in {@link CombatHandler}.</p>
 */
public final class EnergyBlast {

    private EnergyBlast() {
    }

    /** Damage per Energy-attribute point above start, before Release scaling. */
    public static final double DAMAGE_PER_ENERGY = 0.15;
    /** Flat base damage so a blast always does something once you can afford it. */
    public static final double BASE_DAMAGE = 2.0;
    /** Flat Energy cost to fire. */
    public static final double ENERGY_COST = 15.0;
    /** Range of the blast in blocks. */
    public static final double RANGE = 24.0;

    private static int energyAbove(StatBlock stats) {
        return Math.max(0, stats.get(Attribute.ENERGY) - StatBlock.START_VALUE);
    }

    private static double mod(CharacterProfile profile) {
        if (profile == null) {
            return 1.0;
        }
        return Math.max(0.0, 1.0 + profile.totalModifier(Attribute.ENERGY));
    }

    /**
     * Damage a blast would deal.
     *
     * @param stats           attacker attributes
     * @param profile         race/class profile (energy modifier applies), may be null
     * @param releaseFraction release/100 in [0,1] — a blast at 0% Release does only its base
     * @return damage in half-hearts
     */
    public static double damage(StatBlock stats, CharacterProfile profile, double releaseFraction) {
        double clamped = Math.max(0.0, Math.min(1.0, releaseFraction));
        double scaled = (BASE_DAMAGE + energyAbove(stats) * DAMAGE_PER_ENERGY) * mod(profile);
        // Base always applies; the Energy-scaled part is gated by Release.
        return BASE_DAMAGE * mod(profile) + (scaled - BASE_DAMAGE * mod(profile)) * clamped;
    }

    /** Energy cost to fire (flat for now). */
    public static double energyCost() {
        return ENERGY_COST;
    }

    /** Whether the player has enough Energy to fire. */
    public static boolean canFire(double currentEnergy) {
        return currentEnergy >= ENERGY_COST;
    }
}
