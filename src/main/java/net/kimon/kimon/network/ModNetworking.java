package net.kimon.kimon.network;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.config.KimonConfig;
import net.kimon.kimon.power.AuraCache;
import net.kimon.kimon.power.CombatHandler;
import net.kimon.kimon.power.ModAttachments;
import net.kimon.kimon.power.PowerData;
import net.kimon.kimon.power.PowerEffects;
import net.kimon.kimon.power.PowerState;
import net.kimon.kimon.stats.Attribute;
import net.kimon.kimon.stats.CharacterCatalog;
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
    private static final String PROTOCOL_VERSION = "5";

    private ModNetworking() {
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(
                RaiseAttributePayload.TYPE,
                RaiseAttributePayload.STREAM_CODEC,
                ModNetworking::handleRaiseAttribute
        );

        // Clientbound: owner resources (throttled) and neighbours' aura buckets.
        registrar.playToClient(
                PowerSyncPayload.TYPE,
                PowerSyncPayload.STREAM_CODEC,
                ModNetworking::handlePowerSync
        );

        registrar.playToClient(
                CatalogPayload.TYPE,
                CatalogPayload.STREAM_CODEC,
                ModNetworking::handleCatalog
        );

        registrar.playToClient(
                AuraPayload.TYPE,
                AuraPayload.STREAM_CODEC,
                ModNetworking::handleAura
        );

        registrar.playToServer(
                ChargeInputPayload.TYPE,
                ChargeInputPayload.STREAM_CODEC,
                ModNetworking::handleChargeInput
        );

        registrar.playToServer(
                ResetReleasePayload.TYPE,
                ResetReleasePayload.STREAM_CODEC,
                ModNetworking::handleResetRelease
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
            int count = Math.max(1, Math.min(RaiseAttributePayload.MAX_COUNT, payload.count()));
            // Buys point by point at the rising price, stopping at the first one it can't afford.
            StatBlock.RaiseResult result = stats.raiseMany(attribute, count, KimonConfig.costParams());
            if (result.raised() > 0) {
                serverPlayer.setData(ModStatAttachments.STATS.get(), result.block());
                StatEffects.apply(serverPlayer);

                // Spending TP on attributes raises Power (tier bonuses, form gating): progression is
                // earned by fighting for TP, then investing it — there is no free Power source.
                PowerData power = serverPlayer.getData(ModAttachments.POWER.get());
                serverPlayer.setData(ModAttachments.POWER.get(), power.withPower(
                        power.power() + result.raised() * KimonConfig.tpParams().powerPerPoint()));
                PowerEffects.apply(serverPlayer);
            }
        });
    }

    /** Client: stores the server's resource state on the local player (the HUD reads it from there). */
    private static void handlePowerSync(final PowerSyncPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> context.player().setData(ModAttachments.STATE.get(), payload.state()));
    }

    /** Client: adopts the server's race/class catalog so derived stats match the server's. */
    private static void handleCatalog(final CatalogPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> CharacterCatalog.set(payload.catalog()));
    }

    /** Client: remembers how to draw a nearby player's aura. */
    private static void handleAura(final AuraPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> AuraCache.put(payload.entityId(), payload.aura()));
    }

    /** Stores the held-key inputs on the player's PowerState; the tick loop acts on them. */
    private static void handleChargeInput(final ChargeInputPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                PowerState state = serverPlayer.getData(ModAttachments.STATE.get());
                PowerState next = state.withInput(payload.charge(), payload.discharge(), payload.turbo());
                if (!next.equals(state)) {
                    serverPlayer.setData(ModAttachments.STATE.get(), next);
                }
            }
        });
    }

    /** Drops Release to 0 and de-transforms the player. */
    private static void handleResetRelease(final ResetReleasePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                PowerState state = serverPlayer.getData(ModAttachments.STATE.get());
                serverPlayer.setData(ModAttachments.STATE.get(), state.reset());
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
