package net.kimon.kimon.stats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Percent modifiers a race or class applies to the derived stats (the research's table columns:
 * Melee, Defense, Body, AT/stamina, Ki Power, Max Ki, Run, Fly). Values are percents, so {@code 30}
 * means +30% and {@code -10} means -10%; they add up across race and class and are turned into a
 * multiplier with {@link #factor(double)}.
 *
 * <p>Defense and Fly are carried in the data already but only used once those stats exist.</p>
 */
public record StatMods(double melee, double defense, double body, double stamina,
                       double kiPower, double maxKi, double run, double fly) {

    public static final StatMods NONE = new StatMods(0, 0, 0, 0, 0, 0, 0, 0);

    /** JSON codec: every field is optional and defaults to 0. */
    public static final Codec<StatMods> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("melee", 0.0).forGetter(StatMods::melee),
            Codec.DOUBLE.optionalFieldOf("defense", 0.0).forGetter(StatMods::defense),
            Codec.DOUBLE.optionalFieldOf("body", 0.0).forGetter(StatMods::body),
            Codec.DOUBLE.optionalFieldOf("stamina", 0.0).forGetter(StatMods::stamina),
            Codec.DOUBLE.optionalFieldOf("ki_power", 0.0).forGetter(StatMods::kiPower),
            Codec.DOUBLE.optionalFieldOf("max_ki", 0.0).forGetter(StatMods::maxKi),
            Codec.DOUBLE.optionalFieldOf("run", 0.0).forGetter(StatMods::run),
            Codec.DOUBLE.optionalFieldOf("fly", 0.0).forGetter(StatMods::fly)
    ).apply(i, StatMods::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StatMods> STREAM_CODEC = StreamCodec.of(
            (buf, m) -> {
                buf.writeDouble(m.melee());
                buf.writeDouble(m.defense());
                buf.writeDouble(m.body());
                buf.writeDouble(m.stamina());
                buf.writeDouble(m.kiPower());
                buf.writeDouble(m.maxKi());
                buf.writeDouble(m.run());
                buf.writeDouble(m.fly());
            },
            buf -> new StatMods(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble()));

    /** Field-wise sum (race + class). */
    public StatMods plus(StatMods o) {
        return new StatMods(melee + o.melee, defense + o.defense, body + o.body, stamina + o.stamina,
                kiPower + o.kiPower, maxKi + o.maxKi, run + o.run, fly + o.fly);
    }

    /** The multiplier for a percent modifier: {@code 1 + percent/100}, never below 0. */
    public static double factor(double percent) {
        return Math.max(0.0, 1.0 + percent / 100.0);
    }
}
