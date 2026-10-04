package net.kimon.kimon.stats;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A playable race as loaded from {@code data/<namespace>/races/<name>.json}: the starting attribute
 * spread and the stat modifiers. The id is the file's location, not part of the JSON.
 *
 * <pre>{
 *   "attributes": { "strength": 15, "agility": 10, "vitality": 10, "energy": 15, "focus": 5, "spirit": 5 },
 *   "modifiers":  { "melee": 30, "ki_power": 20, "fly": 10 }
 * }</pre>
 *
 * @param attributes starting value of each of the six attributes
 * @param modifiers  percent modifiers on the derived stats
 */
public record RaceDef(Map<Attribute, Integer> attributes, StatMods modifiers) {

    public RaceDef {
        attributes = new EnumMap<>(attributes);
    }

    /** The raw keyed form used by the JSON codec before validation. */
    private record Raw(Map<String, Integer> attributes, StatMods modifiers) {
    }

    public static final Codec<RaceDef> CODEC = RecordCodecBuilder.<Raw>create(i -> i.group(
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("attributes").forGetter(Raw::attributes),
            StatMods.CODEC.optionalFieldOf("modifiers", StatMods.NONE).forGetter(Raw::modifiers)
    ).apply(i, Raw::new)).flatXmap(RaceDef::fromRaw, def -> DataResult.success(def.toRaw()));

    private static DataResult<RaceDef> fromRaw(Raw raw) {
        EnumMap<Attribute, Integer> map = new EnumMap<>(Attribute.class);
        for (Attribute a : Attribute.VALUES) {
            Integer v = raw.attributes().get(a.key());
            if (v == null) {
                return DataResult.error(() -> "race is missing attribute '" + a.key() + "'");
            }
            if (v < 0 || v > StatBlock.MAX_VALUE) {
                return DataResult.error(() -> "attribute '" + a.key() + "' must be in 0.." + StatBlock.MAX_VALUE);
            }
            map.put(a, v);
        }
        for (String key : raw.attributes().keySet()) {
            boolean known = false;
            for (Attribute a : Attribute.VALUES) {
                known |= a.key().equals(key);
            }
            if (!known) {
                return DataResult.error(() -> "unknown attribute '" + key + "'");
            }
        }
        return DataResult.success(new RaceDef(map, raw.modifiers()));
    }

    private Raw toRaw() {
        Map<String, Integer> keyed = new HashMap<>();
        attributes.forEach((a, v) -> keyed.put(a.key(), v));
        return new Raw(keyed, modifiers);
    }

    /** A fresh {@link StatBlock} seeded with this race's starting attributes and no TP. */
    public StatBlock newStatBlock() {
        return StatBlock.of(attributes, 0L);
    }

    /** Sum of the six starting attributes (60 for every shipped race). */
    public int totalPoints() {
        int total = 0;
        for (int v : attributes.values()) {
            total += v;
        }
        return total;
    }
}
