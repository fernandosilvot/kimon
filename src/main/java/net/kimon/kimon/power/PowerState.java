package net.kimon.kimon.power;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Immutable per-player combat resource state: Release %, Energy, Stamina and the active {@link Form}.
 *
 * <p>This is the "feel" core described in the design research: a <b>Release %</b> that scales combat
 * output, an <b>Energy</b> pool that regenerates faster the lower your Release, and a
 * <b>transformation</b> that multiplies combat output while draining Energy. All math is pure (no
 * Minecraft types besides the network codec) so it is unit-testable.</p>
 *
 * <h2>Release state machine</h2>
 * Release only changes through input: <b>charge</b> raises it, <b>discharge</b> lowers it (and wins
 * if both are held), and with no input it holds (STABLE) while upkeep slowly drains Energy. Running
 * out of Energy sends the player to {@link ReleaseState#EXHAUSTED} until Energy recovers.
 *
 * @param release      current Release %, in {@code [0, maxRelease]}
 * @param energy       current Energy (Ki), in {@code [0, maxEnergy]}
 * @param stamina      current Stamina, in {@code [0, maxStamina]}
 * @param charging     input: the charge key is held
 * @param discharging  input: the discharge key is held (takes priority over charging)
 * @param turbo        input: Turbo is held (faster charge, extra Energy cost)
 * @param releaseState the state-machine state computed by the last {@link #tick}
 * @param form         the active transformation
 * @param regenLock    ticks left in the post-damage regeneration lock (server-only, not synced)
 */
public record PowerState(double release, double energy, double stamina,
                         boolean charging, boolean discharging, boolean turbo,
                         ReleaseState releaseState, Form form, int regenLock) {

    public static final PowerState INITIAL =
            new PowerState(0, 0, 0, false, false, false, ReleaseState.STABLE, Form.BASE, 0);

    /** Convenience constructor: no regeneration lock. */
    public PowerState(double release, double energy, double stamina,
                      boolean charging, boolean discharging, boolean turbo,
                      ReleaseState releaseState, Form form) {
        this(release, energy, stamina, charging, discharging, turbo, releaseState, form, 0);
    }

    /** Convenience constructor: no discharge/turbo input, STABLE state. */
    public PowerState(double release, double energy, double stamina, boolean charging, Form form) {
        this(release, energy, stamina, charging, false, false, ReleaseState.STABLE, form, 0);
    }

    /**
     * Stream codec for syncing live resources to the owning client. The discharge input is not sent
     * (the client derives it from {@link ReleaseState#LOWERING}).
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, PowerState> STREAM_CODEC =
            StreamCodec.of(
                    (buf, s) -> {
                        buf.writeDouble(s.release());
                        buf.writeDouble(s.energy());
                        buf.writeDouble(s.stamina());
                        buf.writeBoolean(s.charging());
                        buf.writeBoolean(s.turbo());
                        buf.writeVarInt(s.releaseState().ordinal());
                        buf.writeUtf(s.form().id().toString());
                    },
                    buf -> new PowerState(
                            buf.readDouble(), buf.readDouble(), buf.readDouble(),
                            buf.readBoolean(), false, buf.readBoolean(),
                            ReleaseState.byOrdinal(buf.readVarInt()),
                            Form.fromStored(buf.readUtf()),
                            0)
            );

    /** Minimum Release % to be "active" (earn TP, use powers). Fixed by the design, not configurable. */
    public static final double MIN_ACTIVE_RELEASE = 5.0;

    /** Release % above which charging slows down (the "feels slow past 50%" of the research). */
    private static final double SLOWDOWN_THRESHOLD = 50.0;

    /** Advances the state using {@link PowerParams#DEFAULTS}. */
    public PowerState tick(double dt, double maxRelease, double maxEnergy, double maxStamina) {
        return tick(dt, maxRelease, maxEnergy, maxStamina, PowerParams.DEFAULTS);
    }

    /**
     * Advances the state by {@code dt} seconds. Besides Release/Energy/Stamina, the active form
     * drains Energy per second; running out of Energy drops the form back to BASE and Release to 0.
     *
     * @param dt          seconds elapsed (e.g. 0.05 for one tick)
     * @param maxRelease  the player's current Release ceiling
     * @param maxEnergy   max Energy from attributes
     * @param maxStamina  max Stamina from attributes
     * @param p           balance values
     */
    public PowerState tick(double dt, double maxRelease, double maxEnergy, double maxStamina, PowerParams p) {
        double cap = clamp(maxRelease, p.hardMaxRelease());

        // EXHAUSTED persists until Energy has recovered enough; input is ignored meanwhile.
        boolean exhausted = releaseState == ReleaseState.EXHAUSTED
                && energy < maxEnergy * p.exhaustRecoverPct();

        double nextRelease = Math.min(release, cap);
        boolean rising = false;
        boolean lowering = false;
        if (exhausted) {
            nextRelease = 0;
        } else if (discharging) {
            double lowered = Math.max(0, nextRelease - p.dischargeRate() * dt);
            lowering = lowered < nextRelease;
            nextRelease = lowered;
        } else if (charging) {
            double rate = p.chargeRate()
                    * (turbo ? p.turboMult() : 1.0)
                    * (nextRelease < SLOWDOWN_THRESHOLD ? 1.0 : p.slowdownAbove50());
            double raised = Math.min(cap, nextRelease + rate * dt);
            rising = raised > nextRelease;
            nextRelease = raised;
        }

        // Release upkeep, form drain and Turbo all cost Energy; low Release regenerates it.
        double nextEnergy = energy;
        nextEnergy -= maxEnergy * p.upkeepFactor() * Math.pow(nextRelease / 100.0, 2) * dt;
        nextEnergy -= form.energyDrainPerSecond() * dt;
        if (turbo && rising) {
            nextEnergy -= maxEnergy * p.turboKiDrain() * dt;
        }
        boolean locked = regenLock > 0;
        if (!locked && nextRelease < p.kiRegenCutoff()) {
            double factor = Math.max(0, 1.0 - nextRelease / p.kiRegenCutoff());
            nextEnergy += maxEnergy * p.kiRegenPct() * p.kiRegenRate().multiplier() * factor * dt;
        }
        nextEnergy = clamp(nextEnergy, maxEnergy);

        Form nextForm = form;
        ReleaseState nextState;
        if (nextEnergy <= 0 || exhausted) {
            // Can't sustain Release or a form with no Energy.
            nextEnergy = Math.max(0, nextEnergy);
            nextRelease = 0;
            nextForm = Form.BASE;
            nextState = ReleaseState.EXHAUSTED;
        } else {
            if (!nextForm.isBase() && nextRelease < nextForm.requiredRelease()) {
                // Release dropped below what the form needs to stay active → revert.
                nextForm = Form.BASE;
            }
            if (lowering) {
                nextState = ReleaseState.LOWERING;
            } else if (charging && !discharging && cap > 0 && nextRelease >= cap) {
                nextState = ReleaseState.AT_MAX;
            } else if (rising) {
                nextState = ReleaseState.CHARGING;
            } else {
                nextState = ReleaseState.STABLE;
            }
        }

        boolean staminaBlocked = locked && p.staminaRegenLocked();
        double nextStamina = staminaBlocked
                ? clamp(stamina, maxStamina)
                : clamp(stamina + maxStamina * 0.05 * dt, maxStamina);

        return new PowerState(nextRelease, nextEnergy, nextStamina,
                charging, discharging, turbo, nextState, nextForm, Math.max(0, regenLock - 1));
    }

    /** Sets the three held-key inputs; the next {@link #tick} acts on them. */
    public PowerState withInput(boolean charging, boolean discharging, boolean turbo) {
        return new PowerState(release, energy, stamina, charging, discharging, turbo, releaseState, form, regenLock);
    }

    public PowerState withCharging(boolean c) {
        return withInput(c, discharging, turbo);
    }

    public PowerState withForm(Form f) {
        return new PowerState(release, energy, stamina, charging, discharging, turbo, releaseState, f, regenLock);
    }

    /**
     * Instantly drops Release to 0 and de-transforms (the "reset" key). An EXHAUSTED player stays
     * exhausted until Energy recovers.
     */
    public PowerState reset() {
        ReleaseState next = releaseState == ReleaseState.EXHAUSTED ? ReleaseState.EXHAUSTED : ReleaseState.STABLE;
        return new PowerState(0, energy, stamina, charging, discharging, turbo, next, Form.BASE, regenLock);
    }

    public PowerState withResources(double release, double energy, double stamina,
                                    double maxRelease, double maxEnergy, double maxStamina) {
        return new PowerState(
                clamp(release, maxRelease),
                clamp(energy, maxEnergy),
                clamp(stamina, maxStamina),
                charging, discharging, turbo, releaseState, form, regenLock);
    }

    /**
     * Starts (or restarts) the post-damage regeneration lock: no Energy regen for {@code ticks}.
     * A longer remaining lock is never shortened.
     */
    public PowerState hurt(int ticks) {
        return new PowerState(release, energy, stamina, charging, discharging, turbo, releaseState, form,
                Math.max(regenLock, Math.max(0, ticks)));
    }

    /** The combat multiplier contributed by Release (0.0 at 0% up to 1.0 at 100%, more if overcharged). */
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
