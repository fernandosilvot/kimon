package net.kimon.kimon.network;

import io.netty.buffer.ByteBuf;
import net.kimon.kimon.Kimon;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Serverbound one-shot: the reset key was pressed. The server drops Release to 0 and de-transforms
 * the player. No fields: the client cannot dictate any value.
 */
public record ResetReleasePayload() implements CustomPacketPayload {

    public static final Type<ResetReleasePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Kimon.MODID, "reset_release"));

    public static final StreamCodec<ByteBuf, ResetReleasePayload> STREAM_CODEC =
            StreamCodec.unit(new ResetReleasePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
