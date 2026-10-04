package net.kimon.kimon.wish;

/**
 * The possible rewards granted by a Wish Orb. Pure data (amounts only) so the reward table is
 * unit-testable without Minecraft. Inspired by the "wish" idea from the design research, with
 * original rewards tied to Kimon's own progression (Power and Training Points).
 */
public enum Wish {
    /** A modest Training-Point windfall. */
    TRAINING_BOON("training_boon", 0, 500L),
    /** A large Training-Point windfall. */
    GREAT_TRAINING_BOON("great_training_boon", 0, 2000L),
    /** A surge of raw Power. */
    POWER_SURGE("power_surge", 250, 0L),
    /** A big surge of raw Power. */
    GREAT_POWER_SURGE("great_power_surge", 1000, 0L),
    /** A balanced blessing of both. */
    BALANCED_BLESSING("balanced_blessing", 300, 300L);

    private final String key;
    private final int powerReward;
    private final long tpReward;

    Wish(String key, int powerReward, long tpReward) {
        this.key = key;
        this.powerReward = powerReward;
        this.tpReward = tpReward;
    }

    public String key() {
        return key;
    }

    /** Power granted by this wish. */
    public int powerReward() {
        return powerReward;
    }

    /** Training Points granted by this wish. */
    public long tpReward() {
        return tpReward;
    }

    public static final Wish[] VALUES = values();

    /**
     * Picks a wish deterministically from a roll value, so the selection logic can be unit-tested.
     *
     * @param roll any non-negative int (e.g. from the world's RNG)
     * @return the chosen wish
     */
    public static Wish fromRoll(int roll) {
        int index = Math.floorMod(roll, VALUES.length);
        return VALUES[index];
    }
}
