package net.kimon.kimon.power;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.stats.ModStatAttachments;
import net.kimon.kimon.stats.StatBlock;
import net.kimon.kimon.stats.StatCalculator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Server-side combat + resource loop for Phase 3.
 *
 * <ul>
 *   <li>{@link PlayerTickEvent.Post}: advances each player's {@link PowerState} (Release charge/decay,
 *       Energy/Stamina regen) from their attribute-derived maxima.</li>
 *   <li>{@link LivingIncomingDamageEvent}: when a Kimon player lands a melee hit, adds a Strength×
 *       Release damage bonus on top of the vanilla hit.</li>
 *   <li>{@link LivingDamageEvent.Post}: tells the attacking player how much damage they actually
 *       dealt (the "see how hard you hit" feedback).</li>
 * </ul>
 */
@EventBusSubscriber(modid = Kimon.MODID)
public final class CombatHandler {

    private static final double DT = 0.05; // one tick = 1/20 s

    private CombatHandler() {
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        StatBlock stats = player.getData(ModStatAttachments.STATS.get());
        PowerState state = player.getData(ModAttachments.STATE.get());

        PowerState next = state.tick(
                DT,
                StatCalculator.maxRelease(stats),
                StatCalculator.maxEnergy(stats),
                StatCalculator.maxStamina(stats));

        // Only write (and thus sync) when something actually changed, to avoid packet spam.
        if (!approxEqual(next, state)) {
            player.setData(ModAttachments.STATE.get(), next);
        }
    }

    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker) || attacker.level().isClientSide()) {
            return;
        }
        // Only amplify direct melee (the attacker is the direct damage dealer).
        if (event.getSource().getDirectEntity() != attacker) {
            return;
        }

        StatBlock stats = attacker.getData(ModStatAttachments.STATS.get());
        PowerState state = attacker.getData(ModAttachments.STATE.get());

        double bonus = StatCalculator.meleeDamageBonus(stats, state.releaseMultiplier());
        if (bonus > 0) {
            event.setAmount(event.getAmount() + (float) bonus);
        }
    }

    @SubscribeEvent
    static void onDamagePost(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            float dealt = event.getInflictedDamage();
            if (dealt > 0) {
                // Action-bar feedback: "Hit for 12.5".
                attacker.sendSystemMessage(
                        Component.translatable("msg.kimon.hit", String.format("%.1f", dealt)), true);
            }
        }
    }

    private static boolean approxEqual(PowerState a, PowerState b) {
        return a.charging() == b.charging()
                && Math.abs(a.release() - b.release()) < 1e-4
                && Math.abs(a.energy() - b.energy()) < 1e-4
                && Math.abs(a.stamina() - b.stamina()) < 1e-4;
    }
}
