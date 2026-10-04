package net.kimon.kimon.power;

/**
 * Temporary transformation states ("forms") that multiply combat output while draining Energy.
 *
 * <p>Original names/values; the "forms multiply offense, cost Energy per second, and gate on
 * progression" shape is adapted from the design research (see {@code docs/DESIGN.md}). Forms are
 * unlocked by reaching Power tiers and require a minimum Release to activate.</p>
 *
 * <p>Pure data — no Minecraft types — so the balance is unit-testable. The combat multiplier
 * applies to melee and Energy Blast damage; the drain is paid per second by the server tick loop.</p>
 */
public enum Form {
    /** No transformation. The baseline. */
    BASE("base", 1.0, 0.0, 0, 0.0),
    /** First form: modest boost, cheap. */
    SUPER_SAIYAN("super_saiyan", 1.5, 2.0, 5, 10.0),
    /** Second form: strong boost, pricier. */
    SUPER_SAIYAN_2("super_saiyan_2", 2.0, 5.0, 15, 25.0),
    /** Peak form: huge boost, expensive, high requirements. */
    SUPER_SAIYAN_3("super_saiyan_3", 3.0, 12.0, 30, 40.0);

    private final String key;
    private final double damageMultiplier;
    private final double energyDrainPerSecond;
    private final int requiredTier;
    private final double requiredRelease;

    Form(String key, double damageMultiplier, double energyDrainPerSecond,
         int requiredTier, double requiredRelease) {
        this.key = key;
        this.damageMultiplier = damageMultiplier;
        this.energyDrainPerSecond = energyDrainPerSecond;
        this.requiredTier = requiredTier;
        this.requiredRelease = requiredRelease;
    }

    public String key() {
        return key;
    }

    /** Multiplier applied to melee and Energy-Blast damage while in this form. */
    public double damageMultiplier() {
        return damageMultiplier;
    }

    /** Energy drained per second to sustain this form. */
    public double energyDrainPerSecond() {
        return energyDrainPerSecond;
    }

    /** Minimum Power tier required to use this form. */
    public int requiredTier() {
        return requiredTier;
    }

    /** Minimum Release % required to activate / sustain this form. */
    public double requiredRelease() {
        return requiredRelease;
    }

    /**
     * Whether a player with the given Power tier and Release can activate/sustain this form.
     * BASE is always available.
     */
    public boolean isAvailable(int powerTier, double release) {
        return this == BASE || (powerTier >= requiredTier && release >= requiredRelease);
    }

    /** The next form up the ladder, or null if already at the top. */
    public Form next() {
        int i = ordinal();
        return i + 1 < VALUES.length ? VALUES[i + 1] : null;
    }

    /** The previous form down the ladder (BASE at the bottom). */
    public Form previous() {
        int i = ordinal();
        return i > 0 ? VALUES[i - 1] : BASE;
    }

    /**
     * Finds a form by key, case-insensitive; spaces count as underscores, and the names the forms had
     * before ({@code surge}, {@code ascent}, {@code zenith}) still work.
     */
    public static Form byKey(String key) {
        if (key == null) {
            return null;
        }
        String normalized = key.trim().toLowerCase(java.util.Locale.ROOT).replace(' ', '_');
        normalized = switch (normalized) {
            case "surge" -> "super_saiyan";
            case "ascent" -> "super_saiyan_2";
            case "zenith" -> "super_saiyan_3";
            default -> normalized;
        };
        for (Form f : VALUES) {
            if (f.key.equals(normalized)) {
                return f;
            }
        }
        return null;
    }

    /**
     * The highest form a player can currently reach given their tier and Release.
     * Returns BASE if none of the transformed forms are available.
     */
    public static Form highestAvailable(int powerTier, double release) {
        Form best = BASE;
        for (Form f : VALUES) {
            if (f != BASE && f.isAvailable(powerTier, release)) {
                best = f;
            }
        }
        return best;
    }

    public static final Form[] VALUES = values();
}
