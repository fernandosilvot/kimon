package net.kimon.kimon.stats;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.network.CatalogPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Wires the race/class data into the game: registers the datapack loader, and after every load or
 * login sends the catalog to the player(s) and re-derives their stats (a datapack may have changed
 * the modifiers under them).
 */
@EventBusSubscriber(modid = Kimon.MODID)
public final class CharacterDataHandler {

    private CharacterDataHandler() {
    }

    @SubscribeEvent
    static void onAddListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(Kimon.MODID, "character_data"),
                new CharacterDataLoader());
    }

    @SubscribeEvent
    static void onDatapackSync(OnDatapackSyncEvent event) {
        event.getRelevantPlayers().forEach(player -> {
            PacketDistributor.sendToPlayer(player, new CatalogPayload(CharacterCatalog.current()));
            StatEffects.apply(player);
        });
    }
}
