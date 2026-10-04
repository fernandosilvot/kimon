package net.kimon.kimon.network;

import io.netty.buffer.ByteBuf;
import net.kimon.kimon.Kimon;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Serverbound: the client requests a transformation step. {@code up = true} tries to ascend to the
 * next form; {@code up = false} reverts one step toward BASE. The server validates tier/Release
 * requirements and applies the change authoritatively.
 *
 * @param up whether to transform up (true) or down (false)
 */
public record TransformPayload(boolean up) implements CustomPacketPayload {

    public static final Type<TransformPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Kimon.MODID, "transform"));

    public static final StreamCodec<ByteBuf, TransformPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, TransformPayload::up,
            TransformPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
