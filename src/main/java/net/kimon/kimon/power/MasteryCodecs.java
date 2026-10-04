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
 * {@link StreamCodec} for sync. Stored as one int per non-BASE form, under fixed field names that
 * predate the forms' current names so existing saves keep loading.
 */
public final class MasteryCodecs {

    private MasteryCodecs() {
    }

    public static final MapCodec<MasteryData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("surge", 0).forGetter(m -> m.level(Form.SUPER_SAIYAN)),
            Codec.INT.optionalFieldOf("ascent", 0).forGetter(m -> m.level(Form.SUPER_SAIYAN_2)),
            Codec.INT.optionalFieldOf("zenith", 0).forGetter(m -> m.level(Form.SUPER_SAIYAN_3))
    ).apply(instance, MasteryCodecs::build));

    public static final Codec<MasteryData> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, MasteryData> STREAM_CODEC = StreamCodec.of(
            (buf, m) -> {
                buf.writeVarInt(m.level(Form.SUPER_SAIYAN));
                buf.writeVarInt(m.level(Form.SUPER_SAIYAN_2));
                buf.writeVarInt(m.level(Form.SUPER_SAIYAN_3));
            },
            buf -> build(buf.readVarInt(), buf.readVarInt(), buf.readVarInt())
    );

    private static MasteryData build(int surge, int ascent, int zenith) {
        Map<Form, Integer> levels = new EnumMap<>(Form.class);
        levels.put(Form.SUPER_SAIYAN, surge);
        levels.put(Form.SUPER_SAIYAN_2, ascent);
        levels.put(Form.SUPER_SAIYAN_3, zenith);
        return MasteryData.of(levels);
    }
}
