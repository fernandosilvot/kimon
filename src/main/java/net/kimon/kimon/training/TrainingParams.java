package net.kimon.kimon.training;

/**
 * Tunable values for training weights and gravity. Pure data built from the server config; the
 * defaults are [PROP] starting points (the research only says weights lower damage/defense and raise
 * TP chance, gravity lowers STR/DEX and raises TP chance, and the first device was 10G).
 *
 * @param weightPenaltyPerPoint  melee damage lost per point of effective weight (0.002 = 0.2%)
 * @param maxWeightPenalty       cap of that penalty
 * @param weightTpBonusPerPoint  TP-chance bonus per point of effective weight
 * @param gravityStatDrop        how fast STR/DEX fall with gravity: factor = 1 / (1 + (G-1)*drop)
 * @param gravityTpBonusPerG     TP-chance bonus per extra G
 * @param maxTpMultiplier        cap of the TP-chance multiplier
 * @param deviceGravity          gravity inside a Gravity Device's field
 * @param scanRadius             radius in blocks of a Gravity Device's field
 * @param gravityAttributeFactor how much of (G-1) is applied to the real gravity attribute (movement feel)
 * @param maxWeight              cap of the carried weight
 */
public record TrainingParams(
        double weightPenaltyPerPoint,
        double maxWeightPenalty,
        double weightTpBonusPerPoint,
        double gravityStatDrop,
        double gravityTpBonusPerG,
        double maxTpMultiplier,
        double deviceGravity,
        int scanRadius,
        double gravityAttributeFactor,
        double maxWeight) {

    public static final TrainingParams DEFAULTS =
            new TrainingParams(0.002, 0.6, 0.01, 0.1, 0.05, 3.0, 10.0, 8, 0.1, 200.0);
}
