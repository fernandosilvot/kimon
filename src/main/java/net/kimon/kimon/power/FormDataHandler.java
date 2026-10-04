package net.kimon.kimon.power;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.network.FormCatalogPayload;
import net.kimon.kimon.stats.StatEffects;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Registers the form datapack loader and syncs the catalog to players after every load or login. */
@EventBusSubscriber(modid = Kimon.MODID)
public final class FormDataHandler {

    private FormDataHandler() {
    }

    @SubscribeEvent
    static void onAddListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(Kimon.MODID, "form_data"), new FormDataLoader());
    }

    @SubscribeEvent
    static void onDatapackSync(OnDatapackSyncEvent event) {
        event.getRelevantPlayers().forEach(player -> {
            PacketDistributor.sendToPlayer(player, new FormCatalogPayload(FormCatalog.current()));
            StatEffects.apply(player);
        });
    }
}
