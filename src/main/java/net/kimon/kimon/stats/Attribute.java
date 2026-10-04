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
    AGILITY("agility"),
    /** Governs max health (Body). */
    VITALITY("vitality"),
    /** Governs energy-attack power. */
    ENERGY("energy"),
    /** Governs TP gain rate and resource caps. */
    FOCUS("focus"),
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

    /** All attributes, in declaration order. Cached to avoid repeated array allocation. */
    public static final Attribute[] VALUES = values();
}
