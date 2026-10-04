package net.kimon.kimon.power;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.network.AuraPayload;
import net.kimon.kimon.network.PowerSyncPayload;
import net.kimon.kimon.stats.ModStatAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Server-side state sync. The live resources ({@code ModAttachments.STATE}) change almost every
 * tick, so instead of the attachment auto-sync they are sent by hand: to the owner at most every
 * {@link SyncPolicy#INTERVAL_TICKS} ticks and only when changed, and as a coarse {@link AuraState}
 * to the players who can see them. A full sync is forced on login, respawn and dimension change
 * (NeoForge does not always resend synced attachments when changing dimension — issue #2510).
 */
@EventBusSubscriber(modid = Kimon.MODID)
public final class SyncHandler {

    private static final Map<UUID, PowerState> LAST_STATE = new ConcurrentHashMap<>();
    private static final Map<UUID, AuraState> LAST_AURA = new ConcurrentHashMap<>();

    private SyncHandler() {
    }

    /** Called once per server tick for each player, after the resource loop has run. */
    public static void tick(ServerPlayer player) {
        PowerState now = player.getData(ModAttachments.STATE.get());
        if (SyncPolicy.shouldSendResources(player.tickCount, LAST_STATE.get(player.getUUID()), now)) {
            LAST_STATE.put(player.getUUID(), now);
            PacketDistributor.sendToPlayer(player, new PowerSyncPayload(now));
        }
        AuraState aura = AuraState.of(now);
        if (SyncPolicy.shouldSendAura(LAST_AURA.get(player.getUUID()), aura)) {
            LAST_AURA.put(player.getUUID(), aura);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    new AuraPayload(player.getId(), aura));
        }
    }

    /** Forces everything to be resent to the player: resources, aura and the auto-synced data. */
    public static void fullSync(ServerPlayer player) {
        PowerState now = player.getData(ModAttachments.STATE.get());
        LAST_STATE.put(player.getUUID(), now);
        PacketDistributor.sendToPlayer(player, new PowerSyncPayload(now));

        AuraState aura = AuraState.of(now);
        LAST_AURA.put(player.getUUID(), aura);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new AuraPayload(player.getId(), aura));

        // Re-send the attachments NeoForge syncs on its own, in case the dimension change dropped them.
        player.syncData(ModAttachments.POWER.get());
        player.syncData(ModAttachments.MASTERY.get());
        player.syncData(ModStatAttachments.STATS.get());
        player.syncData(ModStatAttachments.PROFILE.get());
    }

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            fullSync(player);
        }
    }

    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            fullSync(player);
        }
    }

    @SubscribeEvent
    static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            fullSync(player);
        }
    }

    /** A player that starts seeing another one needs that player's current aura. */
    @SubscribeEvent
    static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer viewer) {
            AuraState aura = AuraState.of(target.getData(ModAttachments.STATE.get()));
            PacketDistributor.sendToPlayer(viewer, new AuraPayload(target.getId(), aura));
        }
    }

    @SubscribeEvent
    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_STATE.remove(event.getEntity().getUUID());
        LAST_AURA.remove(event.getEntity().getUUID());
    }
}
