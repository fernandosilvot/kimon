package net.kimon.kimon.stats;

import java.util.EnumMap;
import java.util.Map;

/**
 * Playable classes. A class only tweaks the per-attribute percent modifiers (stacked additively
 * with the {@link Race}'s). Original names/values; shape adapted from the design research.
 */
public enum PlayerClass {
    /** More physical power, less energy. */
    WARRIOR("warrior", mods(0.10, -0.10, 0.10, 0, 0, -0.10)),
    /** Balanced, slightly more stamina/focus. */
    BRAWLER("brawler", mods(0, 0, 0, 0.10, 0.10, 0)),
    /** More energy power and spirit, less physical. */
    CHANNELER("channeler", mods(-0.10, 0.10, -0.10, 0.20, 0, 0.20));

    private final String key;
    private final Map<Attribute, Double> modifiers;

    PlayerClass(String key, Map<Attribute, Double> modifiers) {
        this.key = key;
        this.modifiers = modifiers;
    }

    public String key() {
        return key;
    }

    public double modifier(Attribute attribute) {
        return modifiers.getOrDefault(attribute, 0.0);
    }

    public static PlayerClass byKey(String key) {
        for (PlayerClass c : values()) {
            if (c.key.equalsIgnoreCase(key)) {
                return c;
            }
        }
        return null;
    }

    public static final PlayerClass[] VALUES = values();

    private static Map<Attribute, Double> mods(double str, double agi, double vit, double ene, double foc, double spi) {
        EnumMap<Attribute, Double> m = new EnumMap<>(Attribute.class);
        m.put(Attribute.STRENGTH, str);
        m.put(Attribute.AGILITY, agi);
        m.put(Attribute.VITALITY, vit);
        m.put(Attribute.ENERGY, ene);
        m.put(Attribute.FOCUS, foc);
        m.put(Attribute.SPIRIT, spi);
        return m;
    }
}
