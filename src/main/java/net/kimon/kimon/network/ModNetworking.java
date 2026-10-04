package net.kimon.kimon.network;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.power.CombatHandler;
import net.kimon.kimon.power.ModAttachments;
import net.kimon.kimon.power.PowerData;
import net.kimon.kimon.power.PowerEffects;
import net.kimon.kimon.power.PowerState;
import net.kimon.kimon.stats.Attribute;
import net.kimon.kimon.stats.ModStatAttachments;
import net.kimon.kimon.stats.StatBlock;
import net.kimon.kimon.stats.StatEffects;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers Kimon's network payloads and their server-side handlers. Registered on the mod event
 * bus (the {@code @EventBusSubscriber} defaults to the mod bus).
 */
@EventBusSubscriber(modid = Kimon.MODID)
public final class ModNetworking {

    /** Bump this string when the wire format changes incompatibly. */
    private static final String PROTOCOL_VERSION = "1";

    /** Training Points granted per training action. */
    public static final long TP_PER_TRAIN = 5L;

    private ModNetworking() {
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(
                TrainPowerPayload.TYPE,
                TrainPowerPayload.STREAM_CODEC,
                ModNetworking::handleTrain
        );

        registrar.playToServer(
                RaiseAttributePayload.TYPE,
                RaiseAttributePayload.STREAM_CODEC,
                ModNetworking::handleRaiseAttribute
        );

        registrar.playToServer(
                SetChargingPayload.TYPE,
                SetChargingPayload.STREAM_CODEC,
                ModNetworking::handleSetCharging
        );

        registrar.playToServer(
                FireBlastPayload.TYPE,
                FireBlastPayload.STREAM_CODEC,
                ModNetworking::handleFireBlast
        );

        registrar.playToServer(
                TransformPayload.TYPE,
                TransformPayload.STREAM_CODEC,
                ModNetworking::handleTransform
        );
    }

    /**
     * Handles a training request: raises Power (legacy stat + attribute scaling) and grants
     * Training Points to the stat block. Both attachments auto-sync to the owning client.
     */
    private static void handleTrain(final TrainPowerPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                // Legacy Power stat + its tiered attribute scaling.
                PowerData current = serverPlayer.getData(ModAttachments.POWER.get());
                serverPlayer.setData(ModAttachments.POWER.get(), current.train());
                PowerEffects.apply(serverPlayer);

                // New: training also earns Training Points to spend on attributes.
                StatBlock stats = serverPlayer.getData(ModStatAttachments.STATS.get());
                serverPlayer.setData(ModStatAttachments.STATS.get(), stats.addTrainingPoints(TP_PER_TRAIN));
            }
        });
    }

    /**
     * Handles a request to raise one attribute. The server validates affordability via
     * {@link StatBlock#raise} (a no-op if unaffordable), persists the result, and re-derives the
     * player's vanilla attributes.
     */
    private static void handleRaiseAttribute(final RaiseAttributePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            Attribute attribute = payload.attribute();
            if (attribute == null) {
                return;
            }
            StatBlock stats = serverPlayer.getData(ModStatAttachments.STATS.get());
            StatBlock raised = stats.raise(attribute); // returns same instance if unaffordable
            if (raised != stats) {
                serverPlayer.setData(ModStatAttachments.STATS.get(), raised);
                StatEffects.apply(serverPlayer);
            }
        });
    }

    /** Flips the charging flag on the player's PowerState; the tick loop does the rest. */
    private static void handleSetCharging(final SetChargingPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                PowerState state = serverPlayer.getData(ModAttachments.STATE.get());
                if (state.charging() != payload.charging()) {
                    serverPlayer.setData(ModAttachments.STATE.get(), state.withCharging(payload.charging()));
                }
            }
        });
    }

    /** Fires an Energy Blast from the player's view (raycast + damage), handled in CombatHandler. */
    private static void handleFireBlast(final FireBlastPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                CombatHandler.fireEnergyBlast(serverPlayer);
            }
        });
    }

    /** Transforms the player up or down one form, validated server-side. */
    private static void handleTransform(final TransformPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                CombatHandler.transform(serverPlayer, payload.up());
            }
        });
    }
}
