package net.kimon.kimon.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * One effect of a skill, as data: a {@code type} the game knows how to apply, and a value that grows
 * with the skill level: {@code base + perLevel * level} (the base only counts once the skill has at
 * least one level). What the number means depends on the type (see the constants).
 *
 * @param type     one of the {@code TYPE_*} constants
 * @param base     value at level 1 and above, before the per-level part
 * @param perLevel value added per level
 */
public record SkillEffect(String type, double base, double perLevel) {

    /** +% to the Release ceiling (Potential Unlock). */
    public static final String RELEASE_CAP = "release_cap";
    /** % less damage taken (Endurance). */
    public static final String DAMAGE_REDUCTION = "damage_reduction";
    /** +% jump strength (Jump). */
    public static final String JUMP_BOOST = "jump_boost";
    /** Extra blocks you can fall without damage (Jump). */
    public static final String SAFE_FALL = "safe_fall";
    /** Enables flight; the value is the +% flying speed (Fly). */
    public static final String FLIGHT = "flight";
    /** Dash strength, as horizontal speed (Dash). */
    public static final String DASH = "dash";
    /** Range in blocks of the target readout (Ki Sense). */
    public static final String KI_SENSE = "ki_sense";

    public static final Codec<SkillEffect> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("type").forGetter(SkillEffect::type),
            Codec.DOUBLE.optionalFieldOf("base", 0.0).forGetter(SkillEffect::base),
            Codec.DOUBLE.optionalFieldOf("per_level", 0.0).forGetter(SkillEffect::perLevel)
    ).apply(i, SkillEffect::new));

    /** The value of this effect at the given skill level (0 when the skill isn't learned). */
    public double at(int level) {
        return level <= 0 ? 0.0 : base + perLevel * level;
    }
}
