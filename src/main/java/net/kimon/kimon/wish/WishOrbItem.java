package net.kimon.kimon.wish;

import net.kimon.kimon.power.ModAttachments;
import net.kimon.kimon.power.PowerData;
import net.kimon.kimon.stats.ModStatAttachments;
import net.kimon.kimon.stats.StatBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A consumable "Wish Orb": right-click to be granted a random {@link Wish} (Power and/or Training
 * Points), consuming the orb. Server-authoritative — the roll and reward happen on the server.
 */
public class WishOrbItem extends Item {

    public WishOrbItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            Wish wish = Wish.fromRoll(level.getRandom().nextInt(Integer.MAX_VALUE));

            if (wish.powerReward() > 0) {
                PowerData power = serverPlayer.getData(ModAttachments.POWER.get());
                serverPlayer.setData(ModAttachments.POWER.get(),
                        power.withPower(power.power() + wish.powerReward()));
            }
            if (wish.tpReward() > 0) {
                StatBlock stats = serverPlayer.getData(ModStatAttachments.STATS.get());
                serverPlayer.setData(ModStatAttachments.STATS.get(),
                        stats.addTrainingPoints(wish.tpReward()));
            }

            serverPlayer.sendSystemMessage(Component.translatable(
                    "msg.kimon.wish_granted",
                    Component.translatable("wish.kimon." + wish.key()),
                    wish.powerReward(), wish.tpReward()), false);

            if (!serverPlayer.getAbilities().instabuild) {
                stack.shrink(1); // consume the orb (kept in creative)
            }
        }

        return InteractionResult.SUCCESS;
    }
}
