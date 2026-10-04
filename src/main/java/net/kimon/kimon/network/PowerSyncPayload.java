package net.kimon.kimon.network;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.power.PowerState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Clientbound: the owning player's live resources (Release, Energy, Stamina, state, form). Sent by
 * {@code SyncHandler} at most every 2 ticks and only when something changed.
 *
 * @param state the authoritative state from the server
 */
public record PowerSyncPayload(PowerState state) implements CustomPacketPayload {

    public static final Type<PowerSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Kimon.MODID, "power_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PowerSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    PowerState.STREAM_CODEC, PowerSyncPayload::state,
                    PowerSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
