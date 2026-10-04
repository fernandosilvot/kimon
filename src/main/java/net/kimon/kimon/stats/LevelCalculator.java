package net.kimon.kimon.stats;

/**
 * Character level, derived from the attributes: every 5 attribute points above 55 is one level, so a
 * fresh character (60 points) is level 1. Pure and unit-tested; the level is a requirement for some
 * later content (forms, skills) and shown on the sheet.
 */
public final class LevelCalculator {

    private LevelCalculator() {
    }

    /** Points the starting spread already has before the first level counts. */
    public static final int BASELINE = 55;
    /** Attribute points per level. */
    public static final int POINTS_PER_LEVEL = 5;

    /** Total attribute points of the block. */
    public static int totalPoints(StatBlock stats) {
        int total = 0;
        for (Attribute a : Attribute.VALUES) {
            total += stats.get(a);
        }
        return total;
    }

    /** The character level, at least 1. */
    public static int level(StatBlock stats) {
        return level(totalPoints(stats));
    }

    public static int level(int totalPoints) {
        return Math.max(1, (totalPoints - BASELINE) / POINTS_PER_LEVEL);
    }

    /** Attribute points still needed to reach the next level. */
    public static int pointsToNextLevel(StatBlock stats) {
        int total = totalPoints(stats);
        int next = level(total) + 1;
        return Math.max(1, BASELINE + next * POINTS_PER_LEVEL - total);
    }
}
