package net.kimon.kimon.skill;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.network.SkillCatalogPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Registers the skill datapack loader and syncs the catalog to players after every load or login. */
@EventBusSubscriber(modid = Kimon.MODID)
public final class SkillDataHandler {

    private SkillDataHandler() {
    }

    @SubscribeEvent
    static void onAddListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(Kimon.MODID, "skill_data"), new SkillDataLoader());
    }

    @SubscribeEvent
    static void onDatapackSync(OnDatapackSyncEvent event) {
        event.getRelevantPlayers().forEach(player -> {
            PacketDistributor.sendToPlayer(player, new SkillCatalogPayload(SkillCatalog.current()));
            SkillHandler.applyEffects(player);
        });
    }
}
