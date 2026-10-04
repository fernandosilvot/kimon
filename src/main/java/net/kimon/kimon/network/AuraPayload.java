package net.kimon.kimon.network;

import io.netty.buffer.ByteBuf;
import net.kimon.kimon.Kimon;
import net.kimon.kimon.power.AuraState;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Clientbound: what neighbours need to draw a player's aura. Carries a coarse bucket, never the
 * player's real Release/Energy numbers.
 *
 * @param entityId the player the aura belongs to
 * @param aura     charging / turbo / Release bucket
 */
public record AuraPayload(int entityId, AuraState aura) implements CustomPacketPayload {

    public static final Type<AuraPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Kimon.MODID, "aura"));

    public static final StreamCodec<ByteBuf, AuraPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.VAR_INT.encode(buf, p.entityId());
                ByteBufCodecs.BOOL.encode(buf, p.aura().charging());
                ByteBufCodecs.BOOL.encode(buf, p.aura().turbo());
                ByteBufCodecs.VAR_INT.encode(buf, p.aura().tier());
            },
            buf -> new AuraPayload(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    new AuraState(ByteBufCodecs.BOOL.decode(buf), ByteBufCodecs.BOOL.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf))));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
