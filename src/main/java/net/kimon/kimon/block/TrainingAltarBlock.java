package net.kimon.kimon.block;

import net.kimon.kimon.config.KimonConfig;
import net.kimon.kimon.power.ModAttachments;
import net.kimon.kimon.power.PowerState;
import net.kimon.kimon.stats.Attribute;
import net.kimon.kimon.stats.CharacterProfile;
import net.kimon.kimon.stats.ModStatAttachments;
import net.kimon.kimon.stats.StatBlock;
import net.kimon.kimon.stats.StatCalculator;
import net.kimon.kimon.stats.TpGain;
import net.kimon.kimon.stats.TpParams;
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
 * A training dummy in block form: right-clicking it counts as a hit on a dummy, so it follows the
 * same rules as fighting — it needs an active Release (at least 5%), costs Stamina, and only
 * sometimes grants Training Points (see {@link TpGain}). It grants no Power and has no free reward:
 * Stamina is the rate limit, and holding Release costs Energy.
 *
 * <p>Server-authoritative: all progression changes happen on the logical server.</p>
 */
public class TrainingAltarBlock extends Block {

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
            PowerState power = serverPlayer.getData(ModAttachments.STATE.get());
            if (!TpGain.eligible(power.release())) {
                serverPlayer.sendSystemMessage(Component.translatable("msg.kimon.altar_inactive"), true);
                return InteractionResult.SUCCESS_SERVER;
            }

            StatBlock stats = serverPlayer.getData(ModStatAttachments.STATS.get());
            CharacterProfile profile = serverPlayer.getData(ModStatAttachments.PROFILE.get());
            TpParams params = KimonConfig.tpParams();
            double maxStamina = StatCalculator.maxStamina(stats, profile);
            double cost = maxStamina * params.altarStaminaCost();
            if (power.stamina() < cost) {
                serverPlayer.sendSystemMessage(Component.translatable("msg.kimon.altar_tired"), true);
                return InteractionResult.SUCCESS_SERVER;
            }

            serverPlayer.setData(ModAttachments.STATE.get(), power.withResources(
                    power.release(), power.energy(), power.stamina() - cost,
                    StatCalculator.maxRelease(stats, KimonConfig.params().baseMaxRelease(),
                            KimonConfig.params().hardMaxRelease()),
                    StatCalculator.maxEnergy(stats, profile, KimonConfig.params().kiPerSpirit()),
                    maxStamina));

            long tp = TpGain.forHit(stats.get(Attribute.FOCUS), power.release(),
                    serverPlayer.getRandom().nextDouble(), params);
            if (tp > 0) {
                serverPlayer.setData(ModStatAttachments.STATS.get(), stats.addTrainingPoints(tp));
                serverPlayer.sendSystemMessage(Component.translatable("msg.kimon.altar_train", tp), true);
            } else {
                serverPlayer.sendSystemMessage(Component.translatable("msg.kimon.altar_nothing"), true);
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
