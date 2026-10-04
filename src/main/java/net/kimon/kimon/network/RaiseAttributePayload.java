package net.kimon.kimon.network;

import io.netty.buffer.ByteBuf;
import net.kimon.kimon.Kimon;
import net.kimon.kimon.stats.Attribute;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Serverbound: the client requests raising one {@link Attribute} by a point. The server is the
 * authority — it checks TP affordability via {@code StatBlock.raise} and only applies if valid, so
 * a malicious client cannot grant itself free attribute points.
 *
 * @param attributeOrdinal the ordinal of the {@link Attribute} to raise
 * @param count            how many points to try to buy (1, 10, 100, 1000 from the sheet's buttons);
 *                         the server clamps it and stops at the first unaffordable point
 */
public record RaiseAttributePayload(int attributeOrdinal, int count) implements CustomPacketPayload {

    /** The most points one request may buy. */
    public static final int MAX_COUNT = 1000;

    public static final Type<RaiseAttributePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Kimon.MODID, "raise_attribute"));

    public static final StreamCodec<ByteBuf, RaiseAttributePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RaiseAttributePayload::attributeOrdinal,
            ByteBufCodecs.VAR_INT, RaiseAttributePayload::count,
            RaiseAttributePayload::new
    );

    /** Convenience constructor from an {@link Attribute}. */
    public RaiseAttributePayload(Attribute attribute, int count) {
        this(attribute.ordinal(), count);
    }

    /** Resolves the ordinal back to an {@link Attribute}, or null if out of range. */
    public Attribute attribute() {
        Attribute[] all = Attribute.VALUES;
        if (attributeOrdinal < 0 || attributeOrdinal >= all.length) {
            return null;
        }
        return all[attributeOrdinal];
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
