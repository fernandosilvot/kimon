package net.kimon.kimon;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.kimon.kimon.power.ModAttachments;
import net.kimon.kimon.stats.ModStatAttachments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * Kimon — an original anime-style power/energy progression mod.
 *
 * <p>Entry point. The value passed to {@link Mod} must match {@code mod_id} in
 * {@code gradle.properties} and the {@code modId} in {@code neoforge.mods.toml}.</p>
 *
 * <p>This class wires up the vertical slice: a per-player Power stat (data attachment) that the
 * client can grow by sending a serverbound training packet, with the result rendered on the HUD.
 * The client-only wiring (keybind + HUD) lives in {@code net.kimon.kimon.client.KimonClient}.</p>
 */
@Mod(Kimon.MODID)
public class Kimon {

    /** The single source of truth for the mod id across the codebase. */
    public static final String MODID = "kimon";

    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * FML injects the mod event bus and container. We keep the constructor lean: all feature
     * registration is delegated to the relevant registry holder classes.
     */
    public Kimon(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Kimon initializing");

        // Register the Power data attachment (serialized + synced + copy-on-death).
        ModAttachments.register(modEventBus);

        // Register the StatBlock data attachment (six attributes + Training Points).
        ModStatAttachments.register(modEventBus);

        // Networking (payloads) is wired via @EventBusSubscriber in ModNetworking.
        // Client-only features (keybind, HUD) are wired in KimonClient.
    }
}
