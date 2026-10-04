package net.kimon.kimon.stats;

import java.util.EnumMap;
import java.util.Map;

/**
 * Immutable snapshot of a player's six {@link Attribute}s plus their unspent Training Points (TP).
 *
 * <p>This is the heart of Kimon's progression economy and is intentionally free of any Minecraft
 * dependency, so every rule (starting values, TP cost curve, affordability, clamping) is
 * unit-testable on a plain JVM.</p>
 *
 * <h2>TP economy</h2>
 * Each attribute point costs more than the last. The cost to raise an attribute from level
 * {@code n} to {@code n+1} is:
 * <pre>{@code cost(n) = BASE_COST + round(n * COST_RATE)}</pre>
 * This is an original formula inspired by the general "rising upgrade cost" idea documented for
 * training-based RPG mods (see {@code docs/DESIGN.md}); the constants are balanced for Kimon.
 */
public final class StatBlock {

    /** Every attribute starts here. */
    public static final int START_VALUE = 5;

    /** Hard ceiling per attribute. */
    public static final int MAX_VALUE = 10_000;

    private final Map<Attribute, Integer> values;
    private final long trainingPoints;

    private StatBlock(Map<Attribute, Integer> values, long trainingPoints) {
        this.values = values;
        this.trainingPoints = Math.max(0, trainingPoints);
    }

    /** A fresh character: every attribute at {@link #START_VALUE}, zero TP. */
    public static StatBlock initial() {
        EnumMap<Attribute, Integer> map = new EnumMap<>(Attribute.class);
        for (Attribute a : Attribute.VALUES) {
            map.put(a, START_VALUE);
        }
        return new StatBlock(map, 0L);
    }

    /** Construct from explicit values (used by serialization). Missing attributes default to START. */
    public static StatBlock of(Map<Attribute, Integer> values, long trainingPoints) {
        EnumMap<Attribute, Integer> map = new EnumMap<>(Attribute.class);
        for (Attribute a : Attribute.VALUES) {
            map.put(a, clampValue(values.getOrDefault(a, START_VALUE)));
        }
        return new StatBlock(map, trainingPoints);
    }

    /** Current value of an attribute. */
    public int get(Attribute attribute) {
        return values.get(attribute);
    }

    /** Unspent Training Points. */
    public long trainingPoints() {
        return trainingPoints;
    }

    /** TP cost to raise {@code attribute} by one point from its current value (default curve). */
    public long costToRaise(Attribute attribute) {
        return costToRaise(attribute, CostParams.DEFAULTS);
    }

    /** TP cost to raise {@code attribute} by one point with an explicit cost curve. */
    public long costToRaise(Attribute attribute, CostParams params) {
        return params.costAt(values.get(attribute));
    }

    /** TP cost to raise an attribute that currently sits at {@code level} (default curve). */
    public static long costAt(int level) {
        return CostParams.DEFAULTS.costAt(level);
    }

    /** Whether the player can currently afford to raise {@code attribute} by one point. */
    public boolean canRaise(Attribute attribute) {
        return canRaise(attribute, CostParams.DEFAULTS);
    }

    public boolean canRaise(Attribute attribute, CostParams params) {
        return values.get(attribute) < MAX_VALUE && trainingPoints >= costToRaise(attribute, params);
    }

    /**
     * Returns a new block with {@code attribute} raised one point and the TP cost deducted, or
     * {@code this} unchanged if it can't be afforded or is already maxed.
     */
    public StatBlock raise(Attribute attribute) {
        return raise(attribute, CostParams.DEFAULTS);
    }

    public StatBlock raise(Attribute attribute, CostParams params) {
        if (!canRaise(attribute, params)) {
            return this;
        }
        long cost = costToRaise(attribute, params);
        EnumMap<Attribute, Integer> next = new EnumMap<>(values);
        next.put(attribute, next.get(attribute) + 1);
        return new StatBlock(next, trainingPoints - cost);
    }

    /** Outcome of a bulk purchase. */
    public record RaiseResult(StatBlock block, int raised, long spent) {
    }

    /**
     * Buys up to {@code count} points of {@code attribute}, one at a time at the rising price, and
     * stops at the first point that can't be afforded or when the attribute is maxed. This is what
     * the x10 / x100 / x1000 buttons do. Always validated here, never trusted from the client.
     */
    public RaiseResult raiseMany(Attribute attribute, int count, CostParams params) {
        StatBlock current = this;
        int raised = 0;
        int wanted = Math.max(0, count);
        while (raised < wanted) {
            StatBlock next = current.raise(attribute, params);
            if (next == current) {
                break;
            }
            current = next;
            raised++;
        }
        return new RaiseResult(current, raised, trainingPoints - current.trainingPoints);
    }

    /** Returns a new block with {@code amount} TP added (clamped at zero). */
    public StatBlock addTrainingPoints(long amount) {
        return new StatBlock(new EnumMap<>(values), trainingPoints + amount);
    }

    /** Returns a new block with TP set to an explicit value. */
    public StatBlock withTrainingPoints(long tp) {
        return new StatBlock(new EnumMap<>(values), tp);
    }

    /** Immutable view of the attribute values. */
    public Map<Attribute, Integer> asMap() {
        return new EnumMap<>(values);
    }

    private static int clampValue(int v) {
        if (v < 0) {
            return 0;
        }
        return Math.min(v, MAX_VALUE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StatBlock other)) {
            return false;
        }
        return trainingPoints == other.trainingPoints && values.equals(other.values);
    }

    @Override
    public int hashCode() {
        return 31 * values.hashCode() + Long.hashCode(trainingPoints);
    }

    @Override
    public String toString() {
        return "StatBlock" + values + " tp=" + trainingPoints;
    }
}
