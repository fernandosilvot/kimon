package net.kimon.kimon.training;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * What a player is training under right now: the Weight they carry and the Gravity they stand in.
 * Pure data. Weight is multiplied by Gravity (10 weight under 10G counts as 100), as in the research.
 *
 * @param weight  total carried weight (already capped)
 * @param gravity gravity multiplier, 1.0 = normal
 */
public record TrainingLoad(double weight, double gravity) {

    public static final TrainingLoad NONE = new TrainingLoad(0.0, 1.0);

    public static final StreamCodec<RegistryFriendlyByteBuf, TrainingLoad> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, TrainingLoad::weight,
                    ByteBufCodecs.DOUBLE, TrainingLoad::gravity,
                    TrainingLoad::new);

    public TrainingLoad {
        weight = Math.max(0.0, weight);
        gravity = Math.max(1.0, gravity);
    }

    /** Weight as felt under the current gravity. */
    public double effectiveWeight() {
        return weight * gravity;
    }

    public boolean isNone() {
        return weight <= 0.0 && gravity <= 1.0;
    }
}
