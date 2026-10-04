package net.kimon.kimon.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.network.FireBlastPayload;
import net.kimon.kimon.network.SetChargingPayload;
import net.kimon.kimon.network.TrainPowerPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.util.Lazy;

/**
 * Client-only wiring for Kimon's vertical slice.
 *
 * <p>Loaded only on the physical client ({@code dist = Dist.CLIENT}). Registers:</p>
 * <ul>
 *     <li>the "Train Power" key mapping (default: <kbd>G</kbd>);</li>
 *     <li>a client tick handler that, on each key press, sends one {@link TrainPowerPayload} to
 *     the server;</li>
 *     <li>the {@link PowerHudLayer} HUD overlay that renders the current Power.</li>
 * </ul>
 *
 * <p>In NeoForge 26.2 the {@code @EventBusSubscriber} annotation routes each handler to the
 * correct bus automatically based on the event type: {@link RegisterKeyMappingsEvent} and
 * {@link RegisterGuiLayersEvent} are mod-bus (registration) events, while
 * {@link ClientTickEvent.Post} is a game-bus event.</p>
 */
@Mod(value = Kimon.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Kimon.MODID, value = Dist.CLIENT)
public final class KimonClient {

    /**
     * The "train power" binding. Lazily created so it does not exist until
     * {@link RegisterKeyMappingsEvent} fires.
     */
    public static final Lazy<KeyMapping> TRAIN_KEY = Lazy.of(() -> new KeyMapping(
            "key.kimon.train",            // translation key
            InputConstants.Type.KEYSYM,   // keyboard
            GLFW.GLFW_KEY_G,              // default: G
            KeyMapping.Category.MISC
    ));

    /** Opens the character sheet / stats screen. Default: <kbd>K</kbd>. */
    public static final Lazy<KeyMapping> STATS_KEY = Lazy.of(() -> new KeyMapping(
            "key.kimon.stats",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            KeyMapping.Category.MISC
    ));

    /** Hold to charge Release (power up). Default: <kbd>C</kbd>. */
    public static final Lazy<KeyMapping> CHARGE_KEY = Lazy.of(() -> new KeyMapping(
            "key.kimon.charge",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            KeyMapping.Category.MISC
    ));

    /** Fires an Energy Blast along your view. Default: <kbd>B</kbd>. */
    public static final Lazy<KeyMapping> BLAST_KEY = Lazy.of(() -> new KeyMapping(
            "key.kimon.blast",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            KeyMapping.Category.MISC
    ));

    /** Tracks the last charge state we told the server, so we only send on change. */
    private static boolean lastChargingSent = false;

    public KimonClient() {
        // No instance wiring needed; everything is handled by the static @SubscribeEvent methods.
    }

    @SubscribeEvent
    static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(TRAIN_KEY.get());
        event.register(STATS_KEY.get());
        event.register(CHARGE_KEY.get());
        event.register(BLAST_KEY.get());
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(PowerHudLayer.ID, new PowerHudLayer());
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        // consumeClick() drains the queued presses, so holding the key sends one packet per press.
        while (TRAIN_KEY.get().consumeClick()) {
            ClientPacketDistributor.sendToServer(new TrainPowerPayload());
        }
        while (STATS_KEY.get().consumeClick()) {
            Minecraft.getInstance().setScreenAndShow(new StatsScreen());
        }
        while (BLAST_KEY.get().consumeClick()) {
            ClientPacketDistributor.sendToServer(new FireBlastPayload());
        }

        // Charge key is a held state, not a click: send the server a packet only when it changes.
        boolean chargingNow = CHARGE_KEY.get().isDown();
        if (chargingNow != lastChargingSent) {
            lastChargingSent = chargingNow;
            ClientPacketDistributor.sendToServer(new SetChargingPayload(chargingNow));
        }
    }
}
