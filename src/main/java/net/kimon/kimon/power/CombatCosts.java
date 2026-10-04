package net.kimon.kimon.power;

/**
 * Pure costs of an empowered melee hit. Per the design research, a hit spends Energy
 * ({@code 1 + STR/200}, so stronger characters pay more) and Stamina; with too little of either the
 * hit falls back to plain vanilla damage.
 */
public final class CombatCosts {

    private CombatCosts() {
    }

    /** Energy an empowered hit costs for a character with the given STRENGTH. */
    public static double kiPerHit(int strength) {
        return 1.0 + Math.max(0, strength) / 200.0;
    }

    /** Stamina an empowered hit costs: a fraction of the stamina pool. */
    public static double staminaPerHit(double maxStamina, double fraction) {
        return Math.max(0.0, maxStamina) * Math.max(0.0, fraction);
    }

    /** Whether the player can pay both costs. */
    public static boolean canPay(double energy, double stamina, double kiCost, double staminaCost) {
        return energy >= kiCost && stamina >= staminaCost;
    }
}
