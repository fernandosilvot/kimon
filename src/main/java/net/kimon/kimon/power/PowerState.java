package net.kimon.kimon.power;

/**
 * Immutable per-player combat resource state: Release %, Energy, Stamina and the active {@link Form}.
 *
 * <p>This is the "feel" core described in the design research: a <b>Release %</b> that scales combat
 * output, an <b>Energy</b> pool that regenerates faster the lower your Release, and a
 * <b>transformation</b> that multiplies combat output while draining Energy. All math is pure (no
 * Minecraft types) so it is unit-testable.</p>
 *
 * @param release  current Release %, in {@code [0, maxRelease]}
 * @param energy   current Energy (Ki), in {@code [0, maxEnergy]}
 * @param stamina  current Stamina, in {@code [0, maxStamina]}
 * @param charging whether the player is actively charging (holding the charge key)
 * @param form     the active transformation
 */
public record PowerState(double release, double energy, double stamina, boolean charging, Form form) {

    public static final PowerState INITIAL = new PowerState(0, 0, 0, false, Form.BASE);

    /** Stream codec for syncing live resources to the owning client. */
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, PowerState> STREAM_CODEC =
            net.minecraft.network.codec.StreamCodec.of(
                    (buf, s) -> {
                        buf.writeDouble(s.release());
                        buf.writeDouble(s.energy());
                        buf.writeDouble(s.stamina());
                        buf.writeBoolean(s.charging());
                        buf.writeVarInt(s.form().ordinal());
                    },
                    buf -> new PowerState(
                            buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean(),
                            Form.VALUES[Math.floorMod(buf.readVarInt(), Form.VALUES.length)])
            );

    public static final double BASE_MAX_RELEASE = 50.0;
    public static final double HARD_MAX_RELEASE = 100.0;

    public static final double CHARGE_RATE = 25.0;
    public static final double SLOWDOWN_ABOVE_50 = 0.5;
    public static final double DECAY_RATE = 10.0;
    public static final double MIN_ACTIVE_RELEASE = 5.0;

    public static final double ENERGY_REGEN_PCT = 0.04;
    public static final double ENERGY_REGEN_CUTOFF = 50.0;

    public static final double STAMINA_REGEN_PCT = 0.05;

    public static final double UPKEEP_FACTOR = 0.004;

    /**
     * Advances the state by {@code dt} seconds. Besides Release/Energy/Stamina, the active form
     * drains Energy per second; running out of Energy drops the form back to BASE (and Release to 0).
     *
     * @param dt          seconds elapsed (e.g. 0.05 for one tick)
     * @param maxRelease  the player's current Release ceiling
     * @param maxEnergy   max Energy from attributes
     * @param maxStamina  max Stamina from attributes
     */
    public PowerState tick(double dt, double maxRelease, double maxEnergy, double maxStamina) {
        double cappedMaxRelease = Math.min(HARD_MAX_RELEASE, Math.max(0, maxRelease));

        double nextEnergy = energy;
        double nextRelease = release;
        Form nextForm = form;

        if (charging && energy > 0) {
            double rate = CHARGE_RATE * (release < 50.0 ? 1.0 : SLOWDOWN_ABOVE_50);
            nextRelease = Math.min(cappedMaxRelease, release + rate * dt);
        } else {
            nextRelease = Math.max(0, release - DECAY_RATE * dt);
        }

        // Release upkeep + form drain both cost Energy.
        double upkeep = maxEnergy * UPKEEP_FACTOR * Math.pow(nextRelease / 100.0, 2) * dt;
        nextEnergy -= upkeep;
        nextEnergy -= form.energyDrainPerSecond() * dt;

        if (nextRelease < ENERGY_REGEN_CUTOFF) {
            double factor = Math.max(0, 1.0 - nextRelease / ENERGY_REGEN_CUTOFF);
            nextEnergy += maxEnergy * ENERGY_REGEN_PCT * factor * dt;
        }
        nextEnergy = clamp(nextEnergy, maxEnergy);

        if (nextEnergy <= 0) {
            nextEnergy = 0;
            nextRelease = 0;
            nextForm = Form.BASE; // can't sustain a form with no Energy
        } else if (nextForm != Form.BASE && nextRelease < nextForm.requiredRelease()) {
            // Release dropped below what the form needs to stay active → revert.
            nextForm = Form.BASE;
        }

        double nextStamina = clamp(stamina + maxStamina * STAMINA_REGEN_PCT * dt, maxStamina);

        return new PowerState(nextRelease, nextEnergy, nextStamina, charging, nextForm);
    }

    public PowerState withCharging(boolean c) {
        return new PowerState(release, energy, stamina, c, form);
    }

    public PowerState withForm(Form f) {
        return new PowerState(release, energy, stamina, charging, f);
    }

    public PowerState withResources(double release, double energy, double stamina,
                                    double maxRelease, double maxEnergy, double maxStamina) {
        return new PowerState(
                clamp(release, Math.min(HARD_MAX_RELEASE, maxRelease)),
                clamp(energy, maxEnergy),
                clamp(stamina, maxStamina),
                charging, form);
    }

    /** The combat multiplier contributed by Release (0.0 at 0% up to 1.0 at 100%). */
    public double releaseMultiplier() {
        return release / 100.0;
    }

    /** The combat multiplier contributed by the active form. */
    public double formMultiplier() {
        return form.damageMultiplier();
    }

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
