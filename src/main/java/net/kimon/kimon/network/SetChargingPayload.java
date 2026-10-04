package net.kimon.kimon.network;

import io.netty.buffer.ByteBuf;
import net.kimon.kimon.Kimon;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Serverbound: the client tells the server whether the player is currently charging (holding the
 * charge key). The server flips the {@code charging} flag on the player's {@code PowerState}; the
 * actual Release change happens authoritatively in the server tick loop.
 *
 * @param charging true while the charge key is held
 */
public record SetChargingPayload(boolean charging) implements CustomPacketPayload {

    public static final Type<SetChargingPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Kimon.MODID, "set_charging"));

    public static final StreamCodec<ByteBuf, SetChargingPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SetChargingPayload::charging,
            SetChargingPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
