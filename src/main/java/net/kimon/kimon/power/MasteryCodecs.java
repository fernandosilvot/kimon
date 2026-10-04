package net.kimon.kimon.power;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * Serialization for {@link MasteryData}: a {@link MapCodec} for NBT persistence and a
 * {@link StreamCodec} for sync. Levels are stored as a map from form id to level.
 *
 * <p>Saves from before forms became data stored three fixed fields ({@code surge}, {@code ascent},
 * {@code zenith}); they are still read, and mapped onto the Super Saiyan forms they became.</p>
 */
public final class MasteryCodecs {

    private MasteryCodecs() {
    }

    public static final MapCodec<MasteryData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, Codec.INT).optionalFieldOf("levels", Map.of())
                    .forGetter(m -> toIdMap(m.asLevelMap())),
            Codec.INT.optionalFieldOf("surge", 0).forGetter(m -> 0),
            Codec.INT.optionalFieldOf("ascent", 0).forGetter(m -> 0),
            Codec.INT.optionalFieldOf("zenith", 0).forGetter(m -> 0)
    ).apply(instance, MasteryCodecs::build));

    public static final Codec<MasteryData> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, MasteryData> STREAM_CODEC = StreamCodec.of(
            (buf, m) -> {
                Map<Form, Integer> levels = m.asLevelMap();
                buf.writeVarInt(levels.size());
                levels.forEach((form, level) -> {
                    buf.writeIdentifier(form.id());
                    buf.writeVarInt(level);
                });
            },
            buf -> {
                Map<Form, Integer> levels = new HashMap<>();
                int n = buf.readVarInt();
                for (int i = 0; i < n; i++) {
                    levels.put(new Form(buf.readIdentifier()), buf.readVarInt());
                }
                return MasteryData.of(levels);
            }
    );

    private static Map<Identifier, Integer> toIdMap(Map<Form, Integer> levels) {
        Map<Identifier, Integer> out = new HashMap<>();
        levels.forEach((form, level) -> out.put(form.id(), level));
        return out;
    }

    private static MasteryData build(Map<Identifier, Integer> levels, int surge, int ascent, int zenith) {
        Map<Form, Integer> map = new HashMap<>();
        levels.forEach((id, level) -> map.put(new Form(id), level));
        // Old saves: keep the best of what was stored under the old names.
        map.merge(Form.SUPER_SAIYAN, surge, Math::max);
        map.merge(Form.SUPER_SAIYAN_2, ascent, Math::max);
        map.merge(Form.SUPER_SAIYAN_3, zenith, Math::max);
        return MasteryData.of(map);
    }
}
