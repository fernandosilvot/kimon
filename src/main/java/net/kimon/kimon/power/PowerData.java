package net.kimon.kimon.power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Immutable per-player power progression state.
 *
 * <p>This record is intentionally free of any Minecraft world/entity dependency so that its
 * progression rules can be unit-tested on a plain JVM without bootstrapping the game. All of the
 * balancing math lives here; the attachment, networking, and HUD layers only move this value
 * around.</p>
 *
 * @param power the player's current Power level. Always clamped to {@code [0, MAX_POWER]}.
 */
public record PowerData(int power) {

    /** A fresh player starts with zero Power. */
    public static final PowerData INITIAL = new PowerData(0);

    /** Hard ceiling so Power can never overflow or grow without bound. */
    public static final int MAX_POWER = 1_000_000;

    /** How much Power a single training action grants. */
    public static final int TRAIN_GAIN = 5;

    /**
     * Canonical constructor clamps the value so no other layer has to defend against
     * out-of-range input.
     */
    public PowerData {
        power = clamp(power);
    }

    /** Map codec used to persist this attachment to disk (NBT) via NeoForge. */
    public static final MapCodec<PowerData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("power").forGetter(PowerData::power)
    ).apply(instance, PowerData::new));

    /** Plain codec, handy for other serialization contexts. */
    public static final Codec<PowerData> CODEC = MAP_CODEC.codec();

    /** Stream codec used to sync this value over the network. */
    public static final StreamCodec<RegistryFriendlyByteBuf, PowerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PowerData::power,
            PowerData::new
    );

    /**
     * Returns a new state representing one training action.
     *
     * @return a copy with {@link #TRAIN_GAIN} more Power, clamped to {@link #MAX_POWER}.
     */
    public PowerData train() {
        return withPower(power + TRAIN_GAIN);
    }

    /**
     * Returns a copy with an explicit Power value (clamped).
     *
     * @param newPower the desired value, before clamping
     * @return a new {@link PowerData}
     */
    public PowerData withPower(int newPower) {
        return new PowerData(newPower);
    }

    /** @return true once the player has reached the maximum Power. */
    public boolean isMaxed() {
        return power >= MAX_POWER;
    }

    private static int clamp(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, MAX_POWER);
    }
}
