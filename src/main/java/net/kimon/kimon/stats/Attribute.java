package net.kimon.kimon.stats;

/**
 * The six trainable character attributes of Kimon.
 *
 * <p>This is an original attribute set inspired by the general shape of training-driven anime-RPG
 * progression (see {@code docs/DESIGN.md}). Names are deliberately generic and original — no
 * third-party terminology.</p>
 *
 * <p>Each attribute governs a derived combat stat (applied elsewhere); this enum only identifies
 * the attributes and carries display/serialization keys.</p>
 */
public enum Attribute {
    /** Governs melee damage. */
    STRENGTH("strength"),
    /** Governs movement speed and defense. */
    DEXTERITY("dexterity"),
    /** Governs max health (Body). */
    CONSTITUTION("constitution"),
    /** Governs energy-attack power. */
    WILLPOWER("willpower"),
    /** Governs TP gain rate and resource caps. */
    MIND("mind"),
    /** Governs max energy and regeneration. */
    SPIRIT("spirit");

    private final String key;

    Attribute(String key) {
        this.key = key;
    }

    /** Stable lowercase identifier used for serialization and translation keys. */
    public String key() {
        return key;
    }

    /**
     * Finds an attribute by name, case-insensitive. Accepts the full key ({@code dexterity}), the
     * abbreviations used by the design docs ({@code str dex con wil mnd spi}) and the names the
     * attributes had before ({@code agility vitality energy focus}). Returns null if unknown.
     */
    public static Attribute byKey(String name) {
        if (name == null) {
            return null;
        }
        return switch (name.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "strength", "str" -> STRENGTH;
            case "dexterity", "dex", "agility" -> DEXTERITY;
            case "constitution", "con", "vitality" -> CONSTITUTION;
            case "willpower", "wil", "will", "energy" -> WILLPOWER;
            case "mind", "mnd", "focus" -> MIND;
            case "spirit", "spi" -> SPIRIT;
            default -> null;
        };
    }

    /** All attributes, in declaration order. Cached to avoid repeated array allocation. */
    public static final Attribute[] VALUES = values();
}
