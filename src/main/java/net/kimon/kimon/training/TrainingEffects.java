package net.kimon.kimon.training;

/**
 * Pure rules for what a {@link TrainingLoad} does to the player. No Minecraft types.
 */
public final class TrainingEffects {

    private TrainingEffects() {
    }

    /** Multiplier on melee damage from carried weight: 1.0 with none, down to {@code 1 - maxWeightPenalty}. */
    public static double damageFactor(TrainingLoad load, TrainingParams p) {
        double penalty = Math.min(p.maxWeightPenalty(), load.effectiveWeight() * p.weightPenaltyPerPoint());
        return 1.0 - Math.max(0.0, penalty);
    }

    /** Multiplier on STR and DEX from gravity: 1.0 at 1G, falling as gravity rises. */
    public static double statFactor(TrainingLoad load, TrainingParams p) {
        return 1.0 / (1.0 + (load.gravity() - 1.0) * p.gravityStatDrop());
    }

    /** Multiplier on the chance of earning TP per hit: weight and gravity make training pay more. */
    public static double tpChanceMultiplier(TrainingLoad load, TrainingParams p) {
        double bonus = load.effectiveWeight() * p.weightTpBonusPerPoint()
                + (load.gravity() - 1.0) * p.gravityTpBonusPerG();
        return Math.min(p.maxTpMultiplier(), 1.0 + Math.max(0.0, bonus));
    }

    /** The amount applied to the vanilla gravity attribute (as a multiplier-total modifier). */
    public static double gravityAttributeModifier(TrainingLoad load, TrainingParams p) {
        return (load.gravity() - 1.0) * p.gravityAttributeFactor();
    }

    /** Total carried weight from a list of (weight per item) x (count), capped at {@code maxWeight}. */
    public static double totalWeight(double[] weights, int[] counts, TrainingParams p) {
        double total = 0.0;
        for (int i = 0; i < weights.length; i++) {
            total += Math.max(0.0, weights[i]) * Math.max(0, counts[i]);
        }
        return Math.min(p.maxWeight(), total);
    }

    /** The gravity felt given how many Gravity Devices cover the player (devices don't stack). */
    public static double gravityFor(boolean insideDeviceField, TrainingParams p) {
        return insideDeviceField ? Math.max(1.0, p.deviceGravity()) : 1.0;
    }
}
