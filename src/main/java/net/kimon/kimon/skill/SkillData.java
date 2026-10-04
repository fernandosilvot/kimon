package net.kimon.kimon.skill;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * A player's skill levels, by skill id. Immutable; skills at level 0 are simply absent.
 */
public final class SkillData {

    public static final SkillData EMPTY = new SkillData(Map.of());

    public static final Codec<SkillData> CODEC = Codec.unboundedMap(Identifier.CODEC, Codec.INT)
            .xmap(SkillData::new, SkillData::levels);

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeVarInt(data.levels.size());
                data.levels.forEach((id, level) -> {
                    buf.writeIdentifier(id);
                    buf.writeVarInt(level);
                });
            },
            buf -> {
                Map<Identifier, Integer> map = new TreeMap<>();
                int n = buf.readVarInt();
                for (int i = 0; i < n; i++) {
                    map.put(buf.readIdentifier(), buf.readVarInt());
                }
                return new SkillData(map);
            });

    private final Map<Identifier, Integer> levels;

    public SkillData(Map<Identifier, Integer> levels) {
        TreeMap<Identifier, Integer> clean = new TreeMap<>();
        levels.forEach((id, level) -> {
            if (level != null && level > 0) {
                clean.put(id, level);
            }
        });
        this.levels = Collections.unmodifiableMap(clean);
    }

    /** Level of a skill, 0 if not learned. */
    public int level(Identifier id) {
        return levels.getOrDefault(id, 0);
    }

    public Map<Identifier, Integer> levels() {
        return levels;
    }

    /** A copy with the skill set to {@code level} (0 removes it). */
    public SkillData with(Identifier id, int level) {
        TreeMap<Identifier, Integer> copy = new TreeMap<>(levels);
        copy.put(id, level);
        return new SkillData(copy);
    }

    /** Mind points used by every learned level (skills missing from the catalog use none). */
    public int mindUsed(SkillCatalog catalog) {
        int used = 0;
        for (Map.Entry<Identifier, Integer> e : levels.entrySet()) {
            SkillDef def = catalog.get(e.getKey());
            if (def != null) {
                used += def.mindCostOfLevel() * Math.min(e.getValue(), def.maxLevel());
            }
        }
        return used;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SkillData other && levels.equals(other.levels);
    }

    @Override
    public int hashCode() {
        return levels.hashCode();
    }

    @Override
    public String toString() {
        return "SkillData" + levels;
    }
}
