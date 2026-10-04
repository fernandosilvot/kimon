package net.kimon.kimon.client;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.power.ModAttachments;
import net.kimon.kimon.power.PowerData;
import net.kimon.kimon.power.PowerScaling;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.gui.GuiLayer;

/**
 * HUD overlay that renders the local player's current Power in the top-left corner.
 *
 * <p>The value is read straight from the synced {@code POWER} data attachment on the client-side
 * player entity, so it always reflects the latest server-authoritative value.</p>
 */
public final class PowerHudLayer implements GuiLayer {

    /** Registry id for this HUD layer. */
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Kimon.MODID, "power_hud");

    private static final int MARGIN_X = 6;
    private static final int MARGIN_Y = 6;
    private static final int TEXT_COLOR = 0xFF55FFFF; // ARGB: opaque cyan ("aura" accent)

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();

        // Don't draw before the player exists or while the F3 debug screen is up.
        if (minecraft.player == null || minecraft.getDebugOverlay().showDebugScreen()) {
            return;
        }

        PowerData data = minecraft.player.getData(ModAttachments.POWER.get());
        int tier = PowerScaling.tiers(data.power());
        Component label = Component.translatable("hud.kimon.power", data.power());
        Component tierLabel = Component.translatable("hud.kimon.tier", tier);

        guiGraphics.text(minecraft.font, label, MARGIN_X, MARGIN_Y, TEXT_COLOR);
        guiGraphics.text(minecraft.font, tierLabel, MARGIN_X, MARGIN_Y + 11, TEXT_COLOR);
    }
}
