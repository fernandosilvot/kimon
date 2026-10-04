package net.kimon.kimon.client;

import net.kimon.kimon.config.KimonConfig;
import net.kimon.kimon.network.RaiseAttributePayload;
import net.kimon.kimon.stats.Attribute;
import net.kimon.kimon.stats.CostParams;
import net.kimon.kimon.stats.LevelCalculator;
import net.kimon.kimon.stats.ModStatAttachments;
import net.kimon.kimon.stats.StatBlock;
import net.kimon.kimon.training.TrainingLoad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Character sheet GUI: shows the six attributes, the player's unspent Training Points, and the TP
 * cost to raise each attribute, with a "+" button per attribute.
 *
 * <p>Pressing "+" sends a {@link RaiseAttributePayload} to the server, which validates affordability
 * and applies the change. The resulting {@link StatBlock} is synced back automatically, so the next
 * frame reflects the new values. The screen is a thin view: it never mutates stats locally.</p>
 *
 * <p>Uses the 26.2 GUI pipeline: {@code extractRenderState(GuiGraphicsExtractor)} instead of the old
 * {@code render(GuiGraphics)}.</p>
 */
public final class StatsScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int LABEL_COLOR = 0xFFFFFFFF;
    private static final int VALUE_COLOR = 0xFF55FFFF;
    private static final int DIM_COLOR = 0xFFAAAAAA;

    private static final int[] BULK = {1, 10, 100, 1000};

    private int topY;

    public StatsScreen() {
        super(Component.translatable("screen.kimon.stats.title"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        this.topY = 62;

        Attribute[] attrs = Attribute.VALUES;
        for (int i = 0; i < attrs.length; i++) {
            final Attribute attribute = attrs[i];
            int rowY = topY + i * ROW_HEIGHT;
            // One button per bulk size: +1, +10, +100, +1000 (the server buys as many as it can afford).
            for (int j = 0; j < BULK.length; j++) {
                final int amount = BULK[j];
                addRenderableWidget(Button.builder(Component.literal("+" + amount),
                                b -> onRaise(attribute, amount))
                        .bounds(cx + 20 + j * 34, rowY, 32, 20)
                        .build());
            }
        }

        addRenderableWidget(Button.builder(Component.translatable("screen.kimon.skills.open"),
                        b -> Minecraft.getInstance().setScreenAndShow(new SkillsScreen()))
                .bounds(cx - 75, topY + attrs.length * ROW_HEIGHT + 10, 150, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(cx - 75, topY + attrs.length * ROW_HEIGHT + 34, 150, 20)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);

        StatBlock stats = currentStats();
        CostParams costs = KimonConfig.costParams();
        int cx = this.width / 2;

        g.centeredText(this.font, this.title, cx, 14, LABEL_COLOR);

        Component tp = Component.translatable("screen.kimon.stats.tp", stats.trainingPoints());
        g.centeredText(this.font, tp, cx, 28, VALUE_COLOR);

        Component level = Component.translatable("screen.kimon.stats.level",
                LevelCalculator.level(stats), LevelCalculator.pointsToNextLevel(stats));
        g.centeredText(this.font, level, cx, 40, LABEL_COLOR);

        TrainingLoad load = currentLoad();
        if (!load.isNone()) {
            g.centeredText(this.font, Component.translatable("hud.kimon.load",
                    (int) Math.round(load.weight()), String.format("%.0f", load.gravity())), cx, 51, DIM_COLOR);
        }

        Attribute[] attrs = Attribute.VALUES;
        for (int i = 0; i < attrs.length; i++) {
            Attribute a = attrs[i];
            int rowY = topY + i * ROW_HEIGHT + 6;

            Component name = Component.translatable("attribute.kimon." + a.key());
            g.text(this.font, name, cx - 150, rowY, LABEL_COLOR);

            Component value = Component.literal(Integer.toString(stats.get(a)));
            g.text(this.font, value, cx - 70, rowY, VALUE_COLOR);

            Component cost = Component.translatable("screen.kimon.stats.cost", stats.costToRaise(a, costs));
            int costColor = stats.canRaise(a, costs) ? DIM_COLOR : 0xFFFF5555;
            g.text(this.font, cost, cx - 40, rowY, costColor);
        }
    }

    private void onRaise(Attribute attribute, int count) {
        ClientPacketDistributor.sendToServer(new RaiseAttributePayload(attribute, count));
    }

    private TrainingLoad currentLoad() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? TrainingLoad.NONE : mc.player.getData(ModStatAttachments.LOAD.get());
    }

    private StatBlock currentStats() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return StatBlock.initial();
        }
        return mc.player.getData(ModStatAttachments.STATS.get());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
