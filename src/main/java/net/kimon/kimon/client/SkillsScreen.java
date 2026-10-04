package net.kimon.kimon.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.kimon.kimon.config.KimonConfig;
import net.kimon.kimon.network.LearnSkillPayload;
import net.kimon.kimon.skill.ModSkillAttachments;
import net.kimon.kimon.skill.SkillCatalog;
import net.kimon.kimon.skill.SkillData;
import net.kimon.kimon.skill.SkillDef;
import net.kimon.kimon.skill.SkillHandler;
import net.kimon.kimon.skill.SkillRules;
import net.kimon.kimon.stats.Attribute;
import net.kimon.kimon.stats.ModStatAttachments;
import net.kimon.kimon.stats.StatBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * The skills list: every skill with its level, what the next level costs in TP and Mind, and a button
 * to learn it. A thin view — the server checks everything and the synced data updates the screen.
 * (A better layout comes with the design pass.)
 */
public final class SkillsScreen extends Screen {

    private static final int ROW_HEIGHT = 22;
    private static final int LABEL = 0xFFFFFFFF;
    private static final int VALUE = 0xFF55FFFF;
    private static final int DIM = 0xFFAAAAAA;
    private static final int BAD = 0xFFFF5555;

    private final List<Identifier> ids = new ArrayList<>();
    private int topY;

    public SkillsScreen() {
        super(Component.translatable("screen.kimon.skills.title"));
        Minecraft mc = Minecraft.getInstance();
        net.minecraft.resources.Identifier race = mc.player == null
                ? net.kimon.kimon.stats.CharacterCatalog.DEFAULT_RACE
                : mc.player.getData(ModStatAttachments.PROFILE.get()).raceId();
        // Only the skills this character can learn: racial skills of other races stay hidden.
        SkillCatalog.current().skills().forEach((id, def) -> {
            if (def.allowsRace(race)) {
                ids.add(id);
            }
        });
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        this.topY = 52;
        for (int i = 0; i < ids.size(); i++) {
            final Identifier id = ids.get(i);
            addRenderableWidget(Button.builder(Component.translatable("screen.kimon.skills.learn"),
                            b -> ClientPacketDistributor.sendToServer(new LearnSkillPayload(id.toString())))
                    .bounds(cx + 95, topY + i * ROW_HEIGHT, 70, 20)
                    .build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(cx - 75, topY + ids.size() * ROW_HEIGHT + 12, 150, 20)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        int cx = this.width / 2;
        Minecraft mc = Minecraft.getInstance();
        StatBlock stats = mc.player == null ? StatBlock.initial() : mc.player.getData(ModStatAttachments.STATS.get());
        SkillData data = mc.player == null ? SkillData.EMPTY : mc.player.getData(ModSkillAttachments.SKILLS.get());
        SkillCatalog catalog = SkillCatalog.current();

        g.centeredText(this.font, this.title, cx, 14, LABEL);
        g.centeredText(this.font, Component.translatable("screen.kimon.stats.tp", stats.trainingPoints()), cx, 28, VALUE);
        int budget = SkillRules.mindBudget(stats.get(Attribute.MIND), KimonConfig.skillParams().mindPerPoint());
        g.centeredText(this.font, Component.translatable("screen.kimon.skills.mind", data.mindUsed(catalog), budget),
                cx, 40, DIM);

        for (int i = 0; i < ids.size(); i++) {
            Identifier id = ids.get(i);
            SkillDef def = catalog.get(id);
            if (def == null) {
                continue;
            }
            int y = topY + i * ROW_HEIGHT + 6;
            g.text(this.font, SkillHandler.skillName(id), cx - 160, y, LABEL);
            g.text(this.font, Component.translatable("screen.kimon.skills.level", data.level(id), def.maxLevel()),
                    cx - 60, y, VALUE);
            long cost = SkillRules.nextCost(data, catalog, id);
            Component price = cost < 0
                    ? Component.translatable("screen.kimon.skills.maxed")
                    : Component.translatable("screen.kimon.skills.cost", cost, def.mindCostOfLevel());
            boolean affordable = cost >= 0 && stats.trainingPoints() >= cost
                    && data.mindUsed(catalog) + def.mindCostOfLevel() <= budget;
            g.text(this.font, price, cx - 10, y, affordable || cost < 0 ? DIM : BAD);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
