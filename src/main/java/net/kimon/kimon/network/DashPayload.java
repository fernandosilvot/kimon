package net.kimon.kimon.network;

import io.netty.buffer.ByteBuf;
import net.kimon.kimon.Kimon;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Serverbound: the dash key was pressed, with the direction the player is steering. The server checks
 * the skill, the cooldown and the Ki, and computes the actual push.
 *
 * @param direction 0 = back, 1 = left, 2 = right (a dash never goes forward)
 */
public record DashPayload(int direction) implements CustomPacketPayload {

    public static final int BACK = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    public static final Type<DashPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Kimon.MODID, "dash"));

    public static final StreamCodec<ByteBuf, DashPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DashPayload::direction,
            DashPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
