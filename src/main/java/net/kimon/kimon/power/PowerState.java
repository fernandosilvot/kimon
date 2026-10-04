package net.kimon.kimon.power;

/**
 * Immutable per-player combat resource state: Release %, Energy and Stamina.
 *
 * <p>This is the "feel" core described in the design research: a <b>Release %</b> that scales combat
 * output, and an <b>Energy</b> pool that regenerates faster the lower your Release — the classic
 * "power up vs. recover" tension. All math is pure (no Minecraft types) so it is unit-testable.</p>
 *
 * <p>Values are plain doubles; the owning server advances them each tick via {@link #tick}. Max
 * Energy/Stamina come from the player's attributes and are passed in per tick rather than stored.</p>
 *
 * @param release  current Release %, in {@code [0, maxRelease]}
 * @param energy   current Energy (Ki), in {@code [0, maxEnergy]}
 * @param stamina  current Stamina, in {@code [0, maxStamina]}
 * @param charging whether the player is actively charging (holding the charge key)
 */
public record PowerState(double release, double energy, double stamina, boolean charging) {

    public static final PowerState INITIAL = new PowerState(0, 0, 0, false);

    /** Stream codec for syncing live resources to the owning client. */
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, PowerState> STREAM_CODEC =
            net.minecraft.network.codec.StreamCodec.of(
                    (buf, s) -> {
                        buf.writeDouble(s.release());
                        buf.writeDouble(s.energy());
                        buf.writeDouble(s.stamina());
                        buf.writeBoolean(s.charging());
                    },
                    buf -> new PowerState(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean())
            );

    /** Base maximum Release before any Potential-style unlock. */
    public static final double BASE_MAX_RELEASE = 50.0;
    /** Absolute ceiling for Release. */
    public static final double HARD_MAX_RELEASE = 100.0;

    /** Release gained per second while charging below 50%. */
    public static final double CHARGE_RATE = 25.0;
    /** Charging slows past 50% (the documented "feels slower past 50%"). */
    public static final double SLOWDOWN_ABOVE_50 = 0.5;
    /** Release lost per second while not charging. */
    public static final double DECAY_RATE = 10.0;
    /** Release below which attacks/TP are gated. */
    public static final double MIN_ACTIVE_RELEASE = 5.0;

    /** Fraction of max Energy regenerated per second at 0% Release. */
    public static final double ENERGY_REGEN_PCT = 0.04;
    /** Release at/above which Energy no longer regenerates. */
    public static final double ENERGY_REGEN_CUTOFF = 50.0;

    /** Fraction of max Stamina regenerated per second. */
    public static final double STAMINA_REGEN_PCT = 0.05;

    /** Energy upkeep per second to hold Release, scaled by (release/100)^2. */
    public static final double UPKEEP_FACTOR = 0.004;

    /**
     * Advances the state by {@code dt} seconds given the current maxima.
     *
     * @param dt          seconds elapsed (e.g. 0.05 for one tick)
     * @param maxRelease  the player's current Release ceiling
     * @param maxEnergy   max Energy from attributes
     * @param maxStamina  max Stamina from attributes
     * @return the next immutable state
     */
    public PowerState tick(double dt, double maxRelease, double maxEnergy, double maxStamina) {
        double cappedMaxRelease = Math.min(HARD_MAX_RELEASE, Math.max(0, maxRelease));

        double nextEnergy = energy;
        double nextRelease = release;

        // Charging raises Release (slower past 50%) and costs upkeep energy; otherwise it decays.
        if (charging && energy > 0) {
            double rate = CHARGE_RATE * (release < 50.0 ? 1.0 : SLOWDOWN_ABOVE_50);
            nextRelease = Math.min(cappedMaxRelease, release + rate * dt);
        } else {
            nextRelease = Math.max(0, release - DECAY_RATE * dt);
        }

        // Energy upkeep for holding Release (quadratic in release).
        double upkeep = maxEnergy * UPKEEP_FACTOR * Math.pow(nextRelease / 100.0, 2) * dt;
        nextEnergy -= upkeep;

        // Energy regenerates more the lower the Release; nothing at/above the cutoff.
        if (nextRelease < ENERGY_REGEN_CUTOFF) {
            double factor = Math.max(0, 1.0 - nextRelease / ENERGY_REGEN_CUTOFF);
            nextEnergy += maxEnergy * ENERGY_REGEN_PCT * factor * dt;
        }
        nextEnergy = clamp(nextEnergy, maxEnergy);

        // Out of energy: Release collapses to 0.
        if (nextEnergy <= 0) {
            nextEnergy = 0;
            nextRelease = 0;
        }

        // Stamina always trickles back.
        double nextStamina = clamp(stamina + maxStamina * STAMINA_REGEN_PCT * dt, maxStamina);

        return new PowerState(nextRelease, nextEnergy, nextStamina, charging);
    }

    /** @return a copy with the charging flag set. */
    public PowerState withCharging(boolean c) {
        return new PowerState(release, energy, stamina, c);
    }

    /** @return a copy with explicit resource values (clamped to the given maxima). */
    public PowerState withResources(double release, double energy, double stamina,
                                    double maxRelease, double maxEnergy, double maxStamina) {
        return new PowerState(
                clamp(release, Math.min(HARD_MAX_RELEASE, maxRelease)),
                clamp(energy, maxEnergy),
                clamp(stamina, maxStamina),
                charging);
    }

    /** The combat multiplier contributed by Release (0.0 at 0% up to 1.0 at 100%). */
    public double releaseMultiplier() {
        return release / 100.0;
    }

    /** Whether the player has enough Release to use powers / earn progression. */
    public boolean isActive() {
        return release >= MIN_ACTIVE_RELEASE;
    }

    private static double clamp(double v, double max) {
        if (v < 0) {
            return 0;
        }
        return Math.min(v, Math.max(0, max));
    }
}
