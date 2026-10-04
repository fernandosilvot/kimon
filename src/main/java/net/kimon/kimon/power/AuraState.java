package net.kimon.kimon.power;

/**
 * The little a player's neighbours need to draw their aura: whether they are charging, using Turbo,
 * and a coarse Release bucket. Other players never receive the real Release/Energy numbers.
 *
 * @param charging charging (or at max with the charge key held)
 * @param turbo    Turbo held
 * @param tier     Release bucket, {@code floor(release / 10)}, 0..20
 */
public record AuraState(boolean charging, boolean turbo, int tier) {

    public static final AuraState NONE = new AuraState(false, false, 0);

    /** Width of one Release bucket, in %. */
    public static final double TIER_STEP = 10.0;

    public static AuraState of(PowerState state) {
        boolean showing = state.releaseState() == ReleaseState.CHARGING
                || state.releaseState() == ReleaseState.AT_MAX;
        int tier = (int) Math.min(20, Math.max(0, Math.floor(state.release() / TIER_STEP)));
        return new AuraState(showing, showing && state.turbo(), tier);
    }
}
