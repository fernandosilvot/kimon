package net.kimon.kimon.stats;

/**
 * Tunable curve for the TP cost of raising an attribute by one point (the research's "UC").
 *
 * <p>The exact formula of the reference game is not public, only its knobs (cost rate, a per-attribute
 * multiplier, a "start minus" that delays when costs begin to climb, and a minimum). This is a
 * [PROP] curve that exposes those knobs:</p>
 *
 * <pre>x = level * attributeMultiplier
 * cost = max(minCost, round(baseCost + costRate * x + x² / startMinus))</pre>
 *
 * <p>The linear term keeps early levels cheap and predictable; the quadratic term, divided by
 * {@code startMinus}, makes high levels climb steeply but only after a delay.</p>
 *
 * @param baseCost            flat part of the cost
 * @param costRate            linear growth per (scaled) level
 * @param attributeMultiplier scales the level before the curve (0.75 by default, as in the research)
 * @param startMinus          divisor of the quadratic term; larger = costs start climbing later
 * @param minCost             floor of the cost
 */
public record CostParams(double baseCost, double costRate, double attributeMultiplier,
                         double startMinus, long minCost) {

    public static final CostParams DEFAULTS = new CostParams(1.0, 0.5, 0.75, 140.0, 1L);

    /** TP cost to raise an attribute that currently sits at {@code level}. */
    public long costAt(int level) {
        double x = Math.max(0, level) * attributeMultiplier;
        double divisor = Math.max(1e-9, startMinus);
        double cost = baseCost + costRate * x + (x * x) / divisor;
        return Math.max(minCost, Math.round(cost));
    }
}
