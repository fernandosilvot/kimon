package net.kimon.kimon.training;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.block.ModBlocks;
import net.kimon.kimon.config.KimonConfig;
import net.kimon.kimon.stats.ModStatAttachments;
import net.kimon.kimon.stats.StatEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Server-side: once a second, works out what each player is training under — the weights in their
 * inventory and whether they stand inside a Gravity Device's field — stores it in the synced
 * {@code LOAD} attachment, and applies the gravity to the real gravity attribute. The effects of the
 * load on damage, STR/DEX and TP chance are computed by {@link TrainingEffects} where they apply.
 */
@EventBusSubscriber(modid = Kimon.MODID)
public final class TrainingHandler {

    private static final Identifier GRAVITY_ID = Identifier.fromNamespaceAndPath(Kimon.MODID, "training_gravity");
    private static final int REFRESH_TICKS = 20;

    private TrainingHandler() {
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player
                && (player.tickCount + player.getId()) % REFRESH_TICKS == 0) {
            refresh(player);
        }
    }

    /** Recomputes the player's load now and applies it if it changed. */
    public static void refresh(ServerPlayer player) {
        TrainingParams params = KimonConfig.trainingParams();

        double carried = 0.0;
        int size = player.getInventory().getContainerSize();
        double[] weights = new double[size];
        int[] counts = new int[size];
        for (int i = 0; i < size; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof WeightItem item) {
                weights[i] = item.weight();
                counts[i] = stack.getCount();
            }
        }
        carried = TrainingEffects.totalWeight(weights, counts, params);

        double gravity = TrainingEffects.gravityFor(insideDeviceField(player, params.scanRadius()), params);
        TrainingLoad load = new TrainingLoad(carried, gravity);

        if (!load.equals(player.getData(ModStatAttachments.LOAD.get()))) {
            player.setData(ModStatAttachments.LOAD.get(), load);
            applyGravity(player, TrainingEffects.gravityAttributeModifier(load, params));
            StatEffects.apply(player); // DEX-driven speed depends on gravity
        }
    }

    /** Whether a Gravity Device is within {@code radius} blocks of the player (loaded chunks only). */
    private static boolean insideDeviceField(ServerPlayer player, int radius) {
        Level level = player.level();
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            if (level.hasChunkAt(pos) && level.getBlockState(pos).is(ModBlocks.GRAVITY_DEVICE.get())) {
                return true;
            }
        }
        return false;
    }

    private static void applyGravity(ServerPlayer player, double amount) {
        AttributeInstance gravity = player.getAttribute(Attributes.GRAVITY);
        if (gravity == null) {
            return;
        }
        gravity.removeModifier(GRAVITY_ID);
        if (amount != 0.0) {
            gravity.addOrUpdateTransientModifier(new AttributeModifier(
                    GRAVITY_ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
}
