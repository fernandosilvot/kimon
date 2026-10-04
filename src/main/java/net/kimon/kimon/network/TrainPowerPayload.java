package net.kimon.kimon.network;

import io.netty.buffer.ByteBuf;
import net.kimon.kimon.Kimon;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Serverbound payload: the client asks the server to perform one "train" action for the sending
 * player. It intentionally carries no fields — the server is the authority and decides what a
 * training action does (how much Power is gained), so the client cannot inject a value.
 *
 * <p>This is the only hand-written packet in the vertical slice. The resulting Power change is
 * propagated back to the client automatically by the synced {@code POWER} data attachment, so no
 * clientbound packet is needed.</p>
 */
public record TrainPowerPayload() implements CustomPacketPayload {

    public static final Type<TrainPowerPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Kimon.MODID, "train_power"));

    /** No fields to encode, so this is a trivial "unit" codec. */
    public static final StreamCodec<ByteBuf, TrainPowerPayload> STREAM_CODEC =
            StreamCodec.unit(new TrainPowerPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
