package net.kimon.kimon.stats;

/**
 * Tunable values for how Training Points are earned. Pure data; {@code KimonConfig} builds one from
 * the server config. Defaults come from {@code docs/02-release-ki-stats.md} (formula) and
 * {@code docs/03-resto-sistemas.md} (hit probability) — all [COM]/[PROP], to be tuned by playtesting.
 *
 * @param baseAmount        flat TP per successful hit ({@code tp.baseAmount})
 * @param perFocusStep      extra TP per FOCUS step at full Release ({@code tp.perFocusStep})
 * @param focusDivisor      FOCUS points per step, i.e. the floor(FOCUS/divisor) of the formula
 * @param hitChance         probability that a qualifying hit grants TP at all ({@code tp.hitChance})
 * @param altarStaminaCost  fraction of max Stamina one Training Altar use costs
 * @param powerPerPoint     Power gained per attribute point bought with TP
 */
public record TpParams(
        int baseAmount,
        int perFocusStep,
        int focusDivisor,
        double hitChance,
        double altarStaminaCost,
        int powerPerPoint) {

    /** Release % needed to earn TP (the research's "at least 5%"). */
    public static final double MIN_RELEASE = 5.0;

    public static final TpParams DEFAULTS = new TpParams(2, 2, 5, 0.2, 0.25, 5);
}
