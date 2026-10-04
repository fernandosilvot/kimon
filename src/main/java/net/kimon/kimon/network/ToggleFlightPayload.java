package net.kimon.kimon.network;

import io.netty.buffer.ByteBuf;
import net.kimon.kimon.Kimon;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Serverbound one-shot: the fly key was pressed. The server decides whether flight can start or stop. */
public record ToggleFlightPayload() implements CustomPacketPayload {

    public static final Type<ToggleFlightPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Kimon.MODID, "toggle_flight"));

    public static final StreamCodec<ByteBuf, ToggleFlightPayload> STREAM_CODEC =
            StreamCodec.unit(new ToggleFlightPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
