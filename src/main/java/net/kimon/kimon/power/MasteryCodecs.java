package net.kimon.kimon.power;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Serialization for {@link MasteryData}: a {@link MapCodec} for NBT persistence and a
 * {@link StreamCodec} for sync. Stored as one int per non-BASE form.
 */
public final class MasteryCodecs {

    private MasteryCodecs() {
    }

    public static final MapCodec<MasteryData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf(Form.SURGE.key(), 0).forGetter(m -> m.level(Form.SURGE)),
            Codec.INT.optionalFieldOf(Form.ASCENT.key(), 0).forGetter(m -> m.level(Form.ASCENT)),
            Codec.INT.optionalFieldOf(Form.ZENITH.key(), 0).forGetter(m -> m.level(Form.ZENITH))
    ).apply(instance, MasteryCodecs::build));

    public static final Codec<MasteryData> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, MasteryData> STREAM_CODEC = StreamCodec.of(
            (buf, m) -> {
                buf.writeVarInt(m.level(Form.SURGE));
                buf.writeVarInt(m.level(Form.ASCENT));
                buf.writeVarInt(m.level(Form.ZENITH));
            },
            buf -> build(buf.readVarInt(), buf.readVarInt(), buf.readVarInt())
    );

    private static MasteryData build(int surge, int ascent, int zenith) {
        Map<Form, Integer> levels = new EnumMap<>(Form.class);
        levels.put(Form.SURGE, surge);
        levels.put(Form.ASCENT, ascent);
        levels.put(Form.ZENITH, zenith);
        return MasteryData.of(levels);
    }
}
