package net.kimon.kimon.stats;

/**
 * Pure rules for earning Training Points by fighting, the only organic TP source.
 *
 * <p>Formula from the design research: {@code TP = 2 + 2 * floor(FOCUS/5) * Release/100}, only while
 * Release is at least 5%, and only on a probability roll per qualifying hit. There is no way to earn
 * TP without a Release above the minimum and a target to hit.</p>
 */
public final class TpGain {

    private TpGain() {
    }

    /** Whether the given Release % is high enough to earn TP at all. */
    public static boolean eligible(double releasePercent) {
        return releasePercent >= TpParams.MIN_RELEASE;
    }

    /**
     * The TP a successful hit is worth (before the probability roll); 0 below the minimum Release.
     *
     * @param focus          the FOCUS attribute used for the calculation
     * @param releasePercent current Release %, e.g. 50 for 50%
     */
    public static long amount(int focus, double releasePercent, TpParams p) {
        if (!eligible(releasePercent)) {
            return 0L;
        }
        int steps = Math.max(0, focus) / Math.max(1, p.focusDivisor());
        double value = p.baseAmount() + (double) p.perFocusStep() * steps * (releasePercent / 100.0);
        return (long) Math.floor(value);
    }

    /**
     * TP actually granted for one hit, given a uniform random {@code roll} in [0,1).
     *
     * @return the amount, or 0 if Release is too low or the roll misses {@code hitChance}
     */
    public static long forHit(int focus, double releasePercent, double roll, TpParams p) {
        if (!eligible(releasePercent) || roll >= p.hitChance()) {
            return 0L;
        }
        return amount(focus, releasePercent, p);
    }
}
