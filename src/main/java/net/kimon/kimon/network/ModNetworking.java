package net.kimon.kimon.network;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.power.ModAttachments;
import net.kimon.kimon.power.PowerData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers Kimon's network payloads and their server-side handlers.
 *
 * <p>Registered on the mod event bus (that is why {@code @EventBusSubscriber} has no
 * {@code bus} / {@code value} dist filter: it defaults to the mod bus on both sides).</p>
 */
@EventBusSubscriber(modid = Kimon.MODID)
public final class ModNetworking {

    /** Bump this string when the wire format changes incompatibly. */
    private static final String PROTOCOL_VERSION = "1";

    private ModNetworking() {
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // Serverbound only: the client requests a training action; the server authoritatively applies it.
        registrar.playToServer(
                TrainPowerPayload.TYPE,
                TrainPowerPayload.STREAM_CODEC,
                ModNetworking::handleTrain
        );
    }

    /**
     * Handles a {@link TrainPowerPayload} on the server's main thread.
     *
     * <p>Reads the player's current {@link PowerData}, applies {@link PowerData#train()}, and writes
     * it back with {@code setData} — which both persists it and triggers the synced attachment to
     * push the new value to the owning client.</p>
     */
    private static void handleTrain(final TrainPowerPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                PowerData current = serverPlayer.getData(ModAttachments.POWER.get());
                serverPlayer.setData(ModAttachments.POWER.get(), current.train());
            }
        });
    }
}
