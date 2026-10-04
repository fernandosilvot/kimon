package net.kimon.kimon.power;

/**
 * Where the Release % currently is in its lifecycle (the "chargeState" synced for the aura and HUD).
 *
 * <ul>
 *   <li>{@link #STABLE} — no input; Release holds its value (only upkeep applies).</li>
 *   <li>{@link #CHARGING} — charge held and Release still rising.</li>
 *   <li>{@link #AT_MAX} — charge held and Release already at the ceiling.</li>
 *   <li>{@link #LOWERING} — discharge held and Release dropping.</li>
 *   <li>{@link #EXHAUSTED} — Energy ran out: Release is 0 and input is ignored until Energy
 *       recovers to {@link PowerParams#exhaustRecoverPct()} of the maximum.</li>
 * </ul>
 */
public enum ReleaseState {
    STABLE,
    CHARGING,
    AT_MAX,
    LOWERING,
    EXHAUSTED;

    public static final ReleaseState[] VALUES = values();

    /** Safe lookup used by the network codec (out-of-range values fall back to STABLE). */
    public static ReleaseState byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : STABLE;
    }
}
