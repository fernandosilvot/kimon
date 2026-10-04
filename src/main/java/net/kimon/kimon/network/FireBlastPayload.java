package net.kimon.kimon.network;

import io.netty.buffer.ByteBuf;
import net.kimon.kimon.Kimon;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Serverbound: the client requests firing an Energy Blast. Carries no data — the server performs the
 * raycast from the player's own view and resolves everything authoritatively, so the client cannot
 * dictate target or damage.
 */
public record FireBlastPayload() implements CustomPacketPayload {

    public static final Type<FireBlastPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Kimon.MODID, "fire_blast"));

    public static final StreamCodec<ByteBuf, FireBlastPayload> STREAM_CODEC =
            StreamCodec.unit(new FireBlastPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
