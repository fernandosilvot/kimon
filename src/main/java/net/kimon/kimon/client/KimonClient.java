package net.kimon.kimon.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.network.FireBlastPayload;
import net.kimon.kimon.network.ChargeInputPayload;
import net.kimon.kimon.network.DashPayload;
import net.kimon.kimon.network.ToggleFlightPayload;
import net.kimon.kimon.network.ResetReleasePayload;
import net.kimon.kimon.network.TransformPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.kimon.kimon.power.AuraCache;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.Lazy;

/**
 * Client-only wiring for Kimon's vertical slice.
 *
 * <p>Loaded only on the physical client ({@code dist = Dist.CLIENT}). Registers:</p>
 * <ul>
 *     <li>the key mappings (charge <kbd>C</kbd>, discharge <kbd>Ctrl+C</kbd>, reset <kbd>H</kbd>,
 *     turbo <kbd>R</kbd>, transform <kbd>G</kbd>, blast <kbd>B</kbd>, sheet <kbd>K</kbd>);</li>
 *     <li>a client tick handler that turns key state into intent payloads for the server;</li>
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

    /** Hold to lower Release. Default: <kbd>Ctrl+C</kbd>. Wins over charge if both are down. */
    public static final Lazy<KeyMapping> DISCHARGE_KEY = Lazy.of(() -> new KeyMapping(
            "key.kimon.discharge",
            KeyConflictContext.IN_GAME,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            KeyMapping.Category.MISC
    ));

    /** Hold for Turbo: faster Release charge at a higher Energy cost. Default: <kbd>R</kbd>. */
    public static final Lazy<KeyMapping> TURBO_KEY = Lazy.of(() -> new KeyMapping(
            "key.kimon.turbo",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KeyMapping.Category.MISC
    ));

    /** Resets Release to 0 and de-transforms. Default: <kbd>H</kbd>. */
    public static final Lazy<KeyMapping> RESET_KEY = Lazy.of(() -> new KeyMapping(
            "key.kimon.reset",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            KeyMapping.Category.MISC
    ));

    /** Opens the skills screen. Default: <kbd>J</kbd>. */
    public static final Lazy<KeyMapping> SKILLS_KEY = Lazy.of(() -> new KeyMapping(
            "key.kimon.skills",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            KeyMapping.Category.MISC
    ));

    /** Toggles flight (needs the Fly skill). Default: <kbd>Y</kbd> (F is taken by vanilla). */
    public static final Lazy<KeyMapping> FLY_KEY = Lazy.of(() -> new KeyMapping(
            "key.kimon.fly",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            KeyMapping.Category.MISC
    ));

    /** Dash sideways/backwards in the direction you steer (needs the Dash skill). Default: <kbd>V</kbd>. */
    public static final Lazy<KeyMapping> DASH_KEY = Lazy.of(() -> new KeyMapping(
            "key.kimon.dash",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KeyMapping.Category.MISC
    ));

    /** Fires an Energy Blast along your view. Default: <kbd>B</kbd>. */
    public static final Lazy<KeyMapping> BLAST_KEY = Lazy.of(() -> new KeyMapping(
            "key.kimon.blast",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            KeyMapping.Category.MISC
    ));

    /** Transform up to the next form. Default: <kbd>G</kbd>. */
    public static final Lazy<KeyMapping> FORM_UP_KEY = Lazy.of(() -> new KeyMapping(
            "key.kimon.transform",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            KeyMapping.Category.MISC
    ));

    /** Last input flags we told the server (charge, discharge, turbo), so we only send on change. */
    private static ChargeInputPayload lastInputSent = new ChargeInputPayload(false, false, false);

    public KimonClient() {
        // No instance wiring needed; everything is handled by the static @SubscribeEvent methods.
    }

    /** The way the player is steering for a dash: left or right if a strafe key is held, else back. */
    private static int dashDirection() {
        net.minecraft.client.Options options = Minecraft.getInstance().options;
        if (options.keyLeft.isDown() && !options.keyRight.isDown()) {
            return DashPayload.LEFT;
        }
        if (options.keyRight.isDown() && !options.keyLeft.isDown()) {
            return DashPayload.RIGHT;
        }
        return DashPayload.BACK;
    }

    @SubscribeEvent
    static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(STATS_KEY.get());
        event.register(SKILLS_KEY.get());
        event.register(FLY_KEY.get());
        event.register(DASH_KEY.get());
        event.register(CHARGE_KEY.get());
        event.register(DISCHARGE_KEY.get());
        event.register(TURBO_KEY.get());
        event.register(RESET_KEY.get());
        event.register(BLAST_KEY.get());
        event.register(FORM_UP_KEY.get());
    }

    /** Forget neighbours' auras when leaving a world, so nothing stale carries into the next one. */
    @SubscribeEvent
    static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        AuraCache.clear();
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(PowerHudLayer.ID, new PowerHudLayer());
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        // consumeClick() drains the queued presses: one packet per press for one-shot actions.
        while (STATS_KEY.get().consumeClick()) {
            Minecraft.getInstance().setScreenAndShow(new StatsScreen());
        }
        while (SKILLS_KEY.get().consumeClick()) {
            Minecraft.getInstance().setScreenAndShow(new SkillsScreen());
        }
        while (FLY_KEY.get().consumeClick()) {
            ClientPacketDistributor.sendToServer(new ToggleFlightPayload());
        }
        while (DASH_KEY.get().consumeClick()) {
            ClientPacketDistributor.sendToServer(new DashPayload(dashDirection()));
        }
        while (BLAST_KEY.get().consumeClick()) {
            ClientPacketDistributor.sendToServer(new FireBlastPayload());
        }
        while (FORM_UP_KEY.get().consumeClick()) {
            ClientPacketDistributor.sendToServer(new TransformPayload(true));
        }
        while (RESET_KEY.get().consumeClick()) {
            ClientPacketDistributor.sendToServer(new ResetReleasePayload());
        }

        // Charge/discharge/turbo are held states: send the server a packet only when they change.
        // Discharge wins over charge (the server enforces it too; this keeps the HUD honest).
        boolean discharge = DISCHARGE_KEY.get().isDown();
        boolean charge = CHARGE_KEY.get().isDown() && !discharge;
        ChargeInputPayload now = new ChargeInputPayload(charge, discharge, TURBO_KEY.get().isDown());
        if (!now.equals(lastInputSent)) {
            lastInputSent = now;
            ClientPacketDistributor.sendToServer(now);
        }
    }
}
