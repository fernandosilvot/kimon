package net.kimon.kimon.client;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.config.KimonConfig;
import net.kimon.kimon.power.ModAttachments;
import net.kimon.kimon.power.PowerData;
import net.kimon.kimon.power.PowerScaling;
import net.kimon.kimon.power.PowerState;
import net.kimon.kimon.stats.ModStatAttachments;
import net.kimon.kimon.stats.StatBlock;
import net.kimon.kimon.stats.StatCalculator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.gui.GuiLayer;

/**
 * HUD overlay (top-left) showing the local player's Power/Tier and live combat resources
 * (Release %, Energy, Stamina). All values come from synced data attachments, so they always
 * reflect the latest server-authoritative state.
 */
public final class PowerHudLayer implements GuiLayer {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Kimon.MODID, "power_hud");

    private static final int MARGIN_X = 6;
    private static final int MARGIN_Y = 6;
    private static final int LINE = 11;
    private static final int ACCENT = 0xFF55FFFF; // cyan
    private static final int RELEASE_COLOR = 0xFFFFD54A; // amber
    private static final int RELEASE_OVERCHARGE_COLOR = 0xFFFF5555; // red
    private static final int ENERGY_COLOR = 0xFF66CCFF; // light blue
    private static final int STAMINA_COLOR = 0xFF88DD88; // green

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getDebugOverlay().showDebugScreen()) {
            return;
        }

        PowerData data = minecraft.player.getData(ModAttachments.POWER.get());
        PowerState state = minecraft.player.getData(ModAttachments.STATE.get());
        StatBlock stats = minecraft.player.getData(ModStatAttachments.STATS.get());
        net.kimon.kimon.stats.CharacterProfile profile =
                minecraft.player.getData(ModStatAttachments.PROFILE.get());

        int tier = PowerScaling.tiers(data.power());
        double maxEnergy = StatCalculator.maxEnergy(stats, profile, KimonConfig.params().kiPerSpirit());
        double maxStamina = StatCalculator.maxStamina(stats, profile);

        int y = MARGIN_Y;
        guiGraphics.text(minecraft.font,
                Component.translatable("hud.kimon.profile",
                        Component.translatable("race.kimon." + profile.race().key()),
                        Component.translatable("class.kimon." + profile.clazz().key())),
                MARGIN_X, y, 0xFFFFFFFF);
        y += LINE;
        // Active form (only when transformed, in an eye-catching color).
        if (state.form() != net.kimon.kimon.power.Form.BASE) {
            net.kimon.kimon.power.MasteryData mastery =
                    minecraft.player.getData(ModAttachments.MASTERY.get());
            guiGraphics.text(minecraft.font,
                    Component.translatable("hud.kimon.form",
                            Component.translatable("form.kimon." + state.form().key()),
                            mastery.level(state.form())),
                    MARGIN_X, y, 0xFFFF66CC);
            y += LINE;
        }
        guiGraphics.text(minecraft.font, Component.translatable("hud.kimon.power", data.power()), MARGIN_X, y, ACCENT);
        y += LINE;
        guiGraphics.text(minecraft.font, Component.translatable("hud.kimon.level",
                net.kimon.kimon.stats.LevelCalculator.level(stats)), MARGIN_X, y, ACCENT);
        y += LINE;
        net.kimon.kimon.training.TrainingLoad load = minecraft.player.getData(ModStatAttachments.LOAD.get());
        if (!load.isNone()) {
            guiGraphics.text(minecraft.font, Component.translatable("hud.kimon.load",
                    (int) Math.round(load.weight()), String.format("%.0f", load.gravity())),
                    MARGIN_X, y, 0xFFFFAA55);
            y += LINE;
        }
        guiGraphics.text(minecraft.font, Component.translatable("hud.kimon.tier", tier), MARGIN_X, y, ACCENT);
        y += LINE;
        // Release: shown in display steps; white up to 50%, amber to 100%, red when overcharged.
        int step = KimonConfig.displayStep();
        int shown = (int) (Math.floor(state.release() / step) * step);
        int releaseColor = state.release() > 100.0 ? RELEASE_OVERCHARGE_COLOR
                : state.release() > 50.0 ? RELEASE_COLOR : 0xFFFFFFFF;
        guiGraphics.text(minecraft.font,
                Component.translatable("hud.kimon.release", shown,
                        Component.translatable("hud.kimon.release_state." + state.releaseState().name().toLowerCase(java.util.Locale.ROOT))
                                .append(state.turbo() ? Component.translatable("hud.kimon.turbo") : Component.empty())),
                MARGIN_X, y, state.releaseState() == net.kimon.kimon.power.ReleaseState.EXHAUSTED
                        ? RELEASE_OVERCHARGE_COLOR : releaseColor);
        y += LINE;
        guiGraphics.text(minecraft.font,
                Component.translatable("hud.kimon.energy",
                        (int) Math.round(state.energy()), (int) Math.round(maxEnergy)),
                MARGIN_X, y, ENERGY_COLOR);
        y += LINE;
        guiGraphics.text(minecraft.font,
                Component.translatable("hud.kimon.stamina",
                        (int) Math.round(state.stamina()), (int) Math.round(maxStamina)),
                MARGIN_X, y, STAMINA_COLOR);
    }
}
