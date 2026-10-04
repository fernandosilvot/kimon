package net.kimon.kimon.power;

/**
 * Global Energy regeneration speed (server option {@code ki.regenRate}). Pure data: the multiplier
 * is applied on top of the base regen percentage.
 */
public enum KiRegenRate {
    SLOW(0.5),
    NORMAL(1.0),
    FAST(1.5),
    FASTER(2.0);

    private final double multiplier;

    KiRegenRate(double multiplier) {
        this.multiplier = multiplier;
    }

    public double multiplier() {
        return multiplier;
    }
}
