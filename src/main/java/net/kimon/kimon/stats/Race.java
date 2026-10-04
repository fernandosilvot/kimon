package net.kimon.kimon.stats;

import java.util.EnumMap;
import java.util.Map;

/**
 * Playable races. Each race sets the character's starting attribute spread (original, Kimon-only
 * names — no third-party terminology) and a per-attribute percent modifier applied to derived
 * combat stats.
 *
 * <p>Starting spreads and the general "each race leans a different way" shape are adapted from the
 * design research (see {@code docs/DESIGN.md}); numbers are Kimon's own.</p>
 */
public enum Race {
    /** Balanced all-rounder. */
    HUMAN("human", spread(10, 10, 10, 10, 10, 10), mods(0, 0, 0, 0, 0.10, 0.10)),
    /** High physical + energy offense, low focus/spirit. */
    TITAN("titan", spread(15, 10, 10, 15, 5, 5), mods(0.30, 0, 0, 0.20, 0, 0)),
    /** Low body, huge energy power and spirit. */
    SAGE("sage", spread(5, 5, 7, 10, 15, 18), mods(-0.10, 0, 0.10, 0.30, 0, 0.20)),
    /** High defense, speed and stamina; average offense. */
    FROST("frost", spread(10, 5, 15, 5, 5, 15), mods(0, 0.30, 0.10, 0, 0, 0.10)),
    /** High agility, regeneration-focused; low vitality/energy. */
    MYSTIC("mystic", spread(10, 15, 8, 7, 10, 10), mods(0.10, 0.10, 0, 0, 0.10, 0)),
    /** Hybrid: between balanced and offensive. */
    HYBRID("hybrid", spread(10, 10, 10, 15, 10, 5), mods(0.15, 0, 0, 0.15, 0.05, 0.05));

    private final String key;
    private final Map<Attribute, Integer> startingSpread;
    private final Map<Attribute, Double> modifiers;

    Race(String key, Map<Attribute, Integer> startingSpread, Map<Attribute, Double> modifiers) {
        this.key = key;
        this.startingSpread = startingSpread;
        this.modifiers = modifiers;
    }

    public String key() {
        return key;
    }

    /** Starting attribute values for a new character of this race. */
    public Map<Attribute, Integer> startingSpread() {
        return new EnumMap<>(startingSpread);
    }

    /** Percent modifier (e.g. 0.30 = +30%) applied to the derived stat of a given attribute. */
    public double modifier(Attribute attribute) {
        return modifiers.getOrDefault(attribute, 0.0);
    }

    /** A fresh {@link StatBlock} seeded with this race's starting attributes. */
    public StatBlock newStatBlock() {
        return StatBlock.of(startingSpread, 0L);
    }

    /** Resolve a race by its key, or null. */
    public static Race byKey(String key) {
        for (Race r : values()) {
            if (r.key.equalsIgnoreCase(key)) {
                return r;
            }
        }
        return null;
    }

    public static final Race[] VALUES = values();

    private static Map<Attribute, Integer> spread(int str, int agi, int vit, int ene, int foc, int spi) {
        EnumMap<Attribute, Integer> m = new EnumMap<>(Attribute.class);
        m.put(Attribute.STRENGTH, str);
        m.put(Attribute.AGILITY, agi);
        m.put(Attribute.VITALITY, vit);
        m.put(Attribute.ENERGY, ene);
        m.put(Attribute.FOCUS, foc);
        m.put(Attribute.SPIRIT, spi);
        return m;
    }

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
