package net.kimon.kimon.block;

import net.kimon.kimon.power.ModAttachments;
import net.kimon.kimon.power.PowerData;
import net.kimon.kimon.stats.ModStatAttachments;
import net.kimon.kimon.stats.StatBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A world block the player interacts with to train: right-clicking grants Power and Training Points,
 * like pressing the train key but tied to a physical structure (the start of giving Kimon a world
 * presence — Phase 7).
 *
 * <p>Server-authoritative: all progression changes happen on the logical server. A larger reward
 * than the free keybind, to make building/finding an altar worthwhile.</p>
 */
public class TrainingAltarBlock extends Block {

    /** Power granted per interaction (bigger than the keybind's +5). */
    public static final int POWER_PER_USE = 15;
    /** Training Points granted per interaction. */
    public static final long TP_PER_USE = 15L;

    public TrainingAltarBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            // Let the client swing/animate; the server does the real work.
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            PowerData power = serverPlayer.getData(ModAttachments.POWER.get());
            serverPlayer.setData(ModAttachments.POWER.get(), power.withPower(power.power() + POWER_PER_USE));

            StatBlock stats = serverPlayer.getData(ModStatAttachments.STATS.get());
            serverPlayer.setData(ModStatAttachments.STATS.get(), stats.addTrainingPoints(TP_PER_USE));

            serverPlayer.sendSystemMessage(
                    Component.translatable("msg.kimon.altar_train", POWER_PER_USE, TP_PER_USE), true);
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
