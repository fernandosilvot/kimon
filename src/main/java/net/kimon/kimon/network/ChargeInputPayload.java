package net.kimon.kimon.network;

import io.netty.buffer.ByteBuf;
import net.kimon.kimon.Kimon;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Serverbound: the held-key state of the Release controls. Sent only when one of the three flags
 * changes. It carries intents, never power values: the server tick loop decides what they do.
 *
 * @param charge    charge key held
 * @param discharge discharge key held (wins over charge)
 * @param turbo     turbo key held
 */
public record ChargeInputPayload(boolean charge, boolean discharge, boolean turbo) implements CustomPacketPayload {

    public static final Type<ChargeInputPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Kimon.MODID, "charge_input"));

    public static final StreamCodec<ByteBuf, ChargeInputPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ChargeInputPayload::charge,
            ByteBufCodecs.BOOL, ChargeInputPayload::discharge,
            ByteBufCodecs.BOOL, ChargeInputPayload::turbo,
            ChargeInputPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
