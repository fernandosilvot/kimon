package net.kimon.kimon.stats;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Serialization for {@link StatBlock}: a {@link MapCodec} for NBT persistence and a
 * {@link StreamCodec} for network sync. Kept separate from {@link StatBlock} so the economy core
 * stays free of Minecraft imports and fully unit-testable.
 */
public final class StatCodecs {

    private StatCodecs() {
    }

    /** Map codec: six attribute ints + the TP long. */
    public static final MapCodec<StatBlock> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf(Attribute.STRENGTH.key()).forGetter(b -> b.get(Attribute.STRENGTH)),
            Codec.INT.fieldOf(Attribute.AGILITY.key()).forGetter(b -> b.get(Attribute.AGILITY)),
            Codec.INT.fieldOf(Attribute.VITALITY.key()).forGetter(b -> b.get(Attribute.VITALITY)),
            Codec.INT.fieldOf(Attribute.ENERGY.key()).forGetter(b -> b.get(Attribute.ENERGY)),
            Codec.INT.fieldOf(Attribute.FOCUS.key()).forGetter(b -> b.get(Attribute.FOCUS)),
            Codec.INT.fieldOf(Attribute.SPIRIT.key()).forGetter(b -> b.get(Attribute.SPIRIT)),
            Codec.LONG.fieldOf("training_points").forGetter(StatBlock::trainingPoints)
    ).apply(instance, StatCodecs::build));

    /** Plain codec derived from the map codec. */
    public static final Codec<StatBlock> CODEC = MAP_CODEC.codec();

    /** Stream codec for full-value network sync. */
    public static final StreamCodec<RegistryFriendlyByteBuf, StatBlock> STREAM_CODEC = StreamCodec.of(
            StatCodecs::encode,
            StatCodecs::decode
    );

    private static StatBlock build(int str, int agi, int vit, int ene, int foc, int spi, long tp) {
        Map<Attribute, Integer> map = new EnumMap<>(Attribute.class);
        map.put(Attribute.STRENGTH, str);
        map.put(Attribute.AGILITY, agi);
        map.put(Attribute.VITALITY, vit);
        map.put(Attribute.ENERGY, ene);
        map.put(Attribute.FOCUS, foc);
        map.put(Attribute.SPIRIT, spi);
        return StatBlock.of(map, tp);
    }

    private static void encode(RegistryFriendlyByteBuf buf, StatBlock block) {
        for (Attribute a : Attribute.VALUES) {
            buf.writeVarInt(block.get(a));
        }
        buf.writeVarLong(block.trainingPoints());
    }

    private static StatBlock decode(RegistryFriendlyByteBuf buf) {
        Map<Attribute, Integer> map = new EnumMap<>(Attribute.class);
        for (Attribute a : Attribute.VALUES) {
            map.put(a, buf.readVarInt());
        }
        long tp = buf.readVarLong();
        return StatBlock.of(map, tp);
    }
}
