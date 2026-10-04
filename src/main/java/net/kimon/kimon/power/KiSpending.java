package net.kimon.kimon.power;

import net.kimon.kimon.config.KimonConfig;
import net.kimon.kimon.stats.CharacterProfile;
import net.kimon.kimon.stats.ModStatAttachments;
import net.kimon.kimon.stats.StatBlock;
import net.kimon.kimon.stats.StatCalculator;
import net.minecraft.server.level.ServerPlayer;

/** Server-side helpers to read and spend a player's Ki. */
public final class KiSpending {

    private KiSpending() {
    }

    /** The player's maximum Ki. */
    public static double maxKi(ServerPlayer player) {
        StatBlock stats = player.getData(ModStatAttachments.STATS.get());
        CharacterProfile profile = player.getData(ModStatAttachments.PROFILE.get());
        return StatCalculator.maxEnergy(stats, profile, KimonConfig.params().kiPerSpirit());
    }

    /** Whether the player has at least {@code amount} Ki. */
    public static boolean has(ServerPlayer player, double amount) {
        return player.getData(ModAttachments.STATE.get()).energy() >= amount;
    }

    /** Spends {@code amount} Ki if available; returns whether it was spent. */
    public static boolean trySpend(ServerPlayer player, double amount) {
        PowerState state = player.getData(ModAttachments.STATE.get());
        if (state.energy() < amount) {
            return false;
        }
        StatBlock stats = player.getData(ModStatAttachments.STATS.get());
        CharacterProfile profile = player.getData(ModStatAttachments.PROFILE.get());
        player.setData(ModAttachments.STATE.get(), state.withResources(
                state.release(), state.energy() - amount, state.stamina(),
                ReleaseCeiling.of(player), maxKi(player),
                StatCalculator.maxStamina(stats, profile)));
        return true;
    }
}
