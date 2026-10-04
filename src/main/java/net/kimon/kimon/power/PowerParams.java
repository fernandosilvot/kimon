package net.kimon.kimon.power;

/**
 * Tunable balance values for the Release / Energy loop. Pure data (no Minecraft types) so the loop in
 * {@link PowerState} stays unit-testable; {@code KimonConfig} builds one of these from the server
 * config. The defaults here are the starting points from {@code docs/02-release-ki-stats.md}
 * ("Opciones de config"); they are [PROP] values meant to be tuned by playtesting.
 *
 * @param baseMaxRelease    base Release ceiling in % ({@code release.baseMax})
 * @param allowOvercharge   whether Release may go past 100% up to 200% ({@code release.allowOvercharge})
 * @param chargeRate        Release gained per second while charging ({@code release.chargeRate})
 * @param slowdownAbove50   rate multiplier once Release is at or above 50% ({@code release.slowdownAbove50})
 * @param turboMult         charge-rate multiplier while Turbo is held ({@code release.turboMult})
 * @param turboKiDrain      fraction of max Energy drained per second while charging with Turbo
 * @param dischargeRate     Release lost per second while discharging ({@code release.dischargeRate})
 * @param upkeepFactor      per-second Energy upkeep factor for holding Release ({@code release.upkeepFactor})
 * @param kiPerSpirit       max Energy per SPIRIT point ({@code ki.perSPI})
 * @param kiRegenPct        fraction of max Energy regenerated per second at 0% Release ({@code ki.regenPct})
 * @param kiRegenRate       global regen speed ({@code ki.regenRate})
 * @param kiRegenCutoff     Release % at/above which Energy no longer regenerates ({@code ki.regenCutoffRelease})
 * @param exhaustRecoverPct fraction of max Energy needed to leave EXHAUSTED
 * @param regenLockTicks    ticks without Energy regeneration after being hurt by a living entity
 *                          ({@code combat.regenLockTicks}, 600 = 30 s)
 * @param staminaRegenLocked whether the same lock also stops Stamina regeneration
 * @param hitStaminaCost    fraction of max Stamina an empowered melee hit costs ({@code combat.hitStaminaCost})
 */
public record PowerParams(
        double baseMaxRelease,
        boolean allowOvercharge,
        double chargeRate,
        double slowdownAbove50,
        double turboMult,
        double turboKiDrain,
        double dischargeRate,
        double upkeepFactor,
        double kiPerSpirit,
        double kiRegenPct,
        KiRegenRate kiRegenRate,
        double kiRegenCutoff,
        double exhaustRecoverPct,
        int regenLockTicks,
        boolean staminaRegenLocked,
        double hitStaminaCost) {

    /** Compatibility constructor: combat values take their defaults. */
    public PowerParams(double baseMaxRelease, boolean allowOvercharge, double chargeRate,
                       double slowdownAbove50, double turboMult, double turboKiDrain,
                       double dischargeRate, double upkeepFactor, double kiPerSpirit,
                       double kiRegenPct, KiRegenRate kiRegenRate, double kiRegenCutoff,
                       double exhaustRecoverPct) {
        this(baseMaxRelease, allowOvercharge, chargeRate, slowdownAbove50, turboMult, turboKiDrain,
                dischargeRate, upkeepFactor, kiPerSpirit, kiRegenPct, kiRegenRate, kiRegenCutoff,
                exhaustRecoverPct, 600, false, 0.02);
    }

    /** Release ceiling when overcharge is enabled. */
    public static final double OVERCHARGE_MAX = 200.0;
    /** Release ceiling when overcharge is disabled. */
    public static final double NORMAL_MAX = 100.0;

    public static final PowerParams DEFAULTS = new PowerParams(
            50.0, false, 10.0, 0.5, 1.5, 0.01, 25.0, 0.002,
            40.0, 0.02, KiRegenRate.NORMAL, 50.0, 0.05, 600, false, 0.02);

    /** Absolute Release ceiling: 100%, or 200% with overcharge. */
    public double hardMaxRelease() {
        return allowOvercharge ? OVERCHARGE_MAX : NORMAL_MAX;
    }
}
