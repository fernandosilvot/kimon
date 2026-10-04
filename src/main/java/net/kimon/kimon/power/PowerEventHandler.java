package net.kimon.kimon.power;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.stats.StatEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Game-bus handlers that keep Power-derived attribute bonuses in sync with the stored Power across
 * the player lifecycle.
 *
 * <p>Power is persisted and copied on death (see {@link ModAttachments}), but the attribute
 * modifiers themselves are transient and must be re-applied whenever the player entity is (re)created
 * — i.e. on login and on respawn. Re-applying from the single source of truth (the Power attachment)
 * avoids any drift or double-application.</p>
 */
@EventBusSubscriber(modid = Kimon.MODID)
public final class PowerEventHandler {

    private PowerEventHandler() {
    }

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            PowerEffects.apply(player);
            StatEffects.apply(player);
        }
    }

    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            PowerEffects.apply(player);
            StatEffects.apply(player);
        }
    }
}
