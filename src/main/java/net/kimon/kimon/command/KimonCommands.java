package net.kimon.kimon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.power.Form;
import net.kimon.kimon.power.MasteryData;
import net.kimon.kimon.power.ModAttachments;
import net.kimon.kimon.power.PowerData;
import net.kimon.kimon.power.PowerState;
import net.kimon.kimon.stats.Attribute;
import net.kimon.kimon.stats.CharacterProfile;
import net.kimon.kimon.stats.ModStatAttachments;
import net.kimon.kimon.stats.CharacterCatalog;
import net.minecraft.resources.Identifier;
import net.kimon.kimon.stats.StatBlock;
import net.kimon.kimon.stats.StatCalculator;
import net.kimon.kimon.stats.StatEffects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Debug/admin command tree: {@code /kimon ...}. Lets you inspect and set progression without
 * grinding. Requires permission level 2 (op) for mutating subcommands, matching vanilla cheats.
 */
@EventBusSubscriber(modid = Kimon.MODID)
public final class KimonCommands {

    private KimonCommands() {
    }

    @SubscribeEvent
    static void onRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();

        d.register(Commands.literal("kimon")
                .then(Commands.literal("info")
                        .executes(ctx -> info(ctx.getSource())))
                .then(Commands.literal("tp")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                .executes(ctx -> giveTp(ctx.getSource(), LongArgumentType.getLong(ctx, "amount")))))
                .then(Commands.literal("power")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                .executes(ctx -> setPower(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))
                .then(Commands.literal("attr")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                        java.util.Arrays.stream(Attribute.VALUES).map(Attribute::key), builder))
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                        .executes(ctx -> setAttr(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "name"),
                                                IntegerArgumentType.getInteger(ctx, "value"))))))
                .then(Commands.literal("race")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("race", StringArgumentType.word())
                                .suggests((ctx, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                        CharacterCatalog.current().raceIds().stream().map(KimonCommands::shortId), builder))
                                .executes(ctx -> setRace(ctx.getSource(), StringArgumentType.getString(ctx, "race")))))
                .then(Commands.literal("class")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("class", StringArgumentType.word())
                                .suggests((ctx, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                        CharacterCatalog.current().classIds().stream().map(KimonCommands::shortId), builder))
                                .executes(ctx -> setClass(ctx.getSource(), StringArgumentType.getString(ctx, "class")))))
                .then(Commands.literal("form")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("form", StringArgumentType.word())
                                .executes(ctx -> setForm(ctx.getSource(), StringArgumentType.getString(ctx, "form")))))
                .then(Commands.literal("mastery")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("form", StringArgumentType.word())
                                .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                        .executes(ctx -> setMastery(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "form"),
                                                IntegerArgumentType.getInteger(ctx, "level"))))))
                .then(Commands.literal("reset")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> reset(ctx.getSource()))));
    }

    private static ServerPlayer self(CommandSourceStack src) {
        return src.getPlayer();
    }

    private static int info(CommandSourceStack src) {
        ServerPlayer p = self(src);
        if (p == null) {
            return 0;
        }
        StatBlock stats = p.getData(ModStatAttachments.STATS.get());
        CharacterProfile profile = p.getData(ModStatAttachments.PROFILE.get());
        PowerData power = p.getData(ModAttachments.POWER.get());

        src.sendSystemMessage(Component.literal("§b=== Kimon ===§r"));
        src.sendSystemMessage(Component.literal(
                "Race: §e" + shortId(profile.raceId()) + "§r  Class: §e" + shortId(profile.classId())));
        src.sendSystemMessage(Component.literal("Power: §a" + power.power() + "§r  TP: §a" + stats.trainingPoints()));
        StringBuilder sb = new StringBuilder("Attrs: ");
        for (Attribute a : Attribute.VALUES) {
            sb.append("§7").append(a.key()).append("§r=").append(stats.get(a)).append("  ");
        }
        src.sendSystemMessage(Component.literal(sb.toString()));
        src.sendSystemMessage(Component.literal(String.format(
                "Derived: §c+%.1f dmg§r  §c+%.0f hp§r  maxEnergy §9%.0f§r  maxStamina §a%.0f",
                StatCalculator.bonusAttackDamage(stats, profile),
                StatCalculator.bonusHealth(stats, profile),
                StatCalculator.maxEnergy(stats, profile, net.kimon.kimon.config.KimonConfig.params().kiPerSpirit()),
                StatCalculator.maxStamina(stats, profile))));
        return 1;
    }

    private static int giveTp(CommandSourceStack src, long amount) {
        ServerPlayer p = self(src);
        if (p == null) {
            return 0;
        }
        StatBlock stats = p.getData(ModStatAttachments.STATS.get());
        p.setData(ModStatAttachments.STATS.get(), stats.addTrainingPoints(amount));
        src.sendSystemMessage(Component.literal("§aGranted " + amount + " TP."));
        return 1;
    }

    private static int setPower(CommandSourceStack src, int value) {
        ServerPlayer p = self(src);
        if (p == null) {
            return 0;
        }
        p.setData(ModAttachments.POWER.get(), new PowerData(value));
        src.sendSystemMessage(Component.literal("§aPower set to " + value + "."));
        return 1;
    }

    private static int setAttr(CommandSourceStack src, String name, int value) {
        ServerPlayer p = self(src);
        if (p == null) {
            return 0;
        }
        Attribute attribute = Attribute.byKey(name);
        if (attribute == null) {
            src.sendSystemMessage(Component.literal("§cUnknown attribute: " + name));
            return 0;
        }
        StatBlock stats = p.getData(ModStatAttachments.STATS.get());
        java.util.Map<Attribute, Integer> map = stats.asMap();
        map.put(attribute, value);
        p.setData(ModStatAttachments.STATS.get(), StatBlock.of(map, stats.trainingPoints()));
        StatEffects.apply(p);
        src.sendSystemMessage(Component.literal("§aSet " + attribute.key() + " to " + value + "."));
        return 1;
    }

    /** Ids in the mod's own namespace are shown without it ("saiyan"); others keep theirs. */
    private static String shortId(Identifier id) {
        return CharacterCatalog.NAMESPACE.equals(id.getNamespace()) ? id.getPath() : id.toString();
    }

    private static int setRace(CommandSourceStack src, String raceKey) {
        ServerPlayer p = self(src);
        if (p == null) {
            return 0;
        }
        CharacterCatalog catalog = CharacterCatalog.current();
        Identifier raceId = CharacterCatalog.parseId(raceKey);
        if (raceId == null || !catalog.hasRace(raceId)) {
            src.sendSystemMessage(Component.literal("§cUnknown race. Options: "
                    + String.join(", ", catalog.raceIds().stream().map(KimonCommands::shortId).toList())));
            return 0;
        }
        CharacterProfile profile = p.getData(ModStatAttachments.PROFILE.get());
        p.setData(ModStatAttachments.PROFILE.get(), profile.withRace(raceId));
        // Reseed attributes to the race's starting spread, keeping TP.
        StatBlock current = p.getData(ModStatAttachments.STATS.get());
        p.setData(ModStatAttachments.STATS.get(),
                catalog.race(raceId).newStatBlock().addTrainingPoints(current.trainingPoints()));
        StatEffects.apply(p);
        src.sendSystemMessage(Component.literal("§aRace set to " + shortId(raceId) + " (attributes reseeded)."));
        return 1;
    }

    private static int setClass(CommandSourceStack src, String classKey) {
        ServerPlayer p = self(src);
        if (p == null) {
            return 0;
        }
        CharacterCatalog catalog = CharacterCatalog.current();
        Identifier classId = CharacterCatalog.parseId(classKey);
        if (classId == null || !catalog.hasClass(classId)) {
            src.sendSystemMessage(Component.literal("§cUnknown class. Options: "
                    + String.join(", ", catalog.classIds().stream().map(KimonCommands::shortId).toList())));
            return 0;
        }
        CharacterProfile profile = p.getData(ModStatAttachments.PROFILE.get());
        p.setData(ModStatAttachments.PROFILE.get(), profile.withClass(classId));
        StatEffects.apply(p);
        src.sendSystemMessage(Component.literal("§aClass set to " + shortId(classId) + "."));
        return 1;
    }

    private static int setForm(CommandSourceStack src, String formKey) {
        ServerPlayer p = self(src);
        if (p == null) {
            return 0;
        }
        Form form = Form.byKey(formKey);
        if (form == null) {
            src.sendSystemMessage(Component.literal("§cUnknown form. Options: " + String.join(", ", java.util.Arrays.stream(Form.VALUES).map(Form::key).toList())));
            return 0;
        }
        PowerState state = p.getData(ModAttachments.STATE.get());
        p.setData(ModAttachments.STATE.get(), state.withForm(form));
        src.sendSystemMessage(Component.literal("§aForm set to " + form.key() + "."));
        return 1;
    }

    private static int setMastery(CommandSourceStack src, String formKey, int level) {
        ServerPlayer p = self(src);
        if (p == null) {
            return 0;
        }
        Form form = Form.byKey(formKey);
        if (form == null || form == Form.BASE) {
            src.sendSystemMessage(Component.literal("§cUnknown form. Options: surge, ascent, zenith"));
            return 0;
        }
        MasteryData mastery = p.getData(ModAttachments.MASTERY.get());
        java.util.Map<Form, Integer> levels = mastery.asLevelMap();
        levels.put(form, level);
        p.setData(ModAttachments.MASTERY.get(), MasteryData.of(levels));
        src.sendSystemMessage(Component.literal("§aMastery of " + form.key() + " set to " + level + "."));
        return 1;
    }

    private static int reset(CommandSourceStack src) {
        ServerPlayer p = self(src);
        if (p == null) {
            return 0;
        }
        p.setData(ModStatAttachments.PROFILE.get(), CharacterProfile.DEFAULT);
        p.setData(ModStatAttachments.STATS.get(), StatBlock.initial());
        p.setData(ModAttachments.POWER.get(), PowerData.INITIAL);
        p.setData(ModAttachments.MASTERY.get(), MasteryData.initial());
        StatEffects.apply(p);
        src.sendSystemMessage(Component.literal("§eCharacter reset to defaults."));
        return 1;
    }
}
