package net.kimon.kimon.block;

import java.util.function.Supplier;

import net.kimon.kimon.Kimon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers Kimon's blocks, their block-items, and a creative tab.
 */
public final class ModBlocks {

    private ModBlocks() {
    }

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Kimon.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Kimon.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Kimon.MODID);

    /** The Training Altar block. */
    public static final DeferredBlock<TrainingAltarBlock> TRAINING_ALTAR = BLOCKS.registerBlock(
            "training_altar",
            TrainingAltarBlock::new,
            p -> p.mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .pushReaction(PushReaction.BLOCK));

    /** Block-item for the Training Altar. */
    public static final DeferredItem<BlockItem> TRAINING_ALTAR_ITEM =
            ITEMS.registerSimpleBlockItem("training_altar", TRAINING_ALTAR);

    /** Gravity Device: multiplies gravity in a field around it (training under G). */
    public static final DeferredBlock<Block> GRAVITY_DEVICE = BLOCKS.registerBlock(
            "gravity_device",
            Block::new,
            p -> p.mapColor(MapColor.COLOR_BLACK)
                    .strength(4.0f, 8.0f)
                    .pushReaction(PushReaction.BLOCK));

    public static final DeferredItem<BlockItem> GRAVITY_DEVICE_ITEM =
            ITEMS.registerSimpleBlockItem("gravity_device", GRAVITY_DEVICE);

    /** Training weights: carried in the inventory, they trade damage for easier TP. */
    public static final DeferredItem<net.kimon.kimon.training.WeightItem> WEIGHTED_WRAPS = ITEMS.registerItem(
            "weighted_wraps",
            p -> new net.kimon.kimon.training.WeightItem(p, net.kimon.kimon.training.WeightItem.WRAPS_WEIGHT),
            p -> p.stacksTo(1));

    public static final DeferredItem<net.kimon.kimon.training.WeightItem> WEIGHTED_VEST = ITEMS.registerItem(
            "weighted_vest",
            p -> new net.kimon.kimon.training.WeightItem(p, net.kimon.kimon.training.WeightItem.VEST_WEIGHT),
            p -> p.stacksTo(1));

    public static final DeferredItem<net.kimon.kimon.training.WeightItem> HEAVY_PLATES = ITEMS.registerItem(
            "heavy_plates",
            p -> new net.kimon.kimon.training.WeightItem(p, net.kimon.kimon.training.WeightItem.PLATES_WEIGHT),
            p -> p.stacksTo(1));

    /** Wish Orb: a consumable that grants a random progression wish. */
    public static final DeferredItem<net.kimon.kimon.wish.WishOrbItem> WISH_ORB = ITEMS.registerItem(
            "wish_orb",
            net.kimon.kimon.wish.WishOrbItem::new,
            p -> p.stacksTo(16));

    /** Kimon creative tab holding the mod's items. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KIMON_TAB = TABS.register(
            "kimon",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kimon"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> TRAINING_ALTAR_ITEM.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(TRAINING_ALTAR_ITEM.get());
                        output.accept(GRAVITY_DEVICE_ITEM.get());
                        output.accept(WEIGHTED_WRAPS.get());
                        output.accept(WEIGHTED_VEST.get());
                        output.accept(HEAVY_PLATES.get());
                        output.accept(WISH_ORB.get());
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
    }
}
