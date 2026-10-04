package net.kimon.kimon.skill;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;

/**
 * A learnable skill as loaded from {@code data/<namespace>/skills/<name>.json}.
 *
 * <pre>{
 *   "max_level": 10,
 *   "cost": { "tp_base": 150, "tp_per_level": 150, "mind": 10 },
 *   "effects": [ { "type": "damage_reduction", "per_level": 3 } ]
 * }</pre>
 *
 * <p>Reaching level {@code n} costs {@code tp_base + tp_per_level * (n - 1)} TP; with the default
 * {@code tp_per_level = tp_base} that is {@code tp_base * n}, the "each level costs the base again,
 * cumulatively" of the research. Every level also uses {@code mind} points of the player's Mind budget.</p>
 *
 * @param maxLevel   highest level (at least 1)
 * @param tpBase     TP cost of level 1
 * @param tpPerLevel TP added to the cost of each further level
 * @param mind       Mind points each level uses
 * @param effects    what the skill does
 * @param races      races that can learn it; empty means every race (racial skills list theirs)
 */
public record SkillDef(int maxLevel, long tpBase, long tpPerLevel, int mind, List<SkillEffect> effects,
                       List<Identifier> races) {

    public SkillDef {
        effects = List.copyOf(effects);
        races = List.copyOf(races);
    }

    /** A skill any race can learn. */
    public SkillDef(int maxLevel, long tpBase, long tpPerLevel, int mind, List<SkillEffect> effects) {
        this(maxLevel, tpBase, tpPerLevel, mind, effects, List.of());
    }

    /** Whether a character of this race can learn the skill. */
    public boolean allowsRace(Identifier raceId) {
        return races.isEmpty() || races.contains(raceId);
    }

    /** The raw shape of the {@code cost} object. */
    private record Cost(long tpBase, long tpPerLevel, int mind) {
    }

    private static final Codec<Cost> COST_CODEC = RecordCodecBuilder.<Cost>create(i -> i.group(
            Codec.LONG.fieldOf("tp_base").forGetter(Cost::tpBase),
            Codec.LONG.optionalFieldOf("tp_per_level", -1L).forGetter(Cost::tpPerLevel),
            Codec.INT.optionalFieldOf("mind", 0).forGetter(Cost::mind)
    ).apply(i, Cost::new));

    public static final Codec<SkillDef> CODEC = RecordCodecBuilder.<SkillDef>create(i -> i.group(
            Codec.INT.optionalFieldOf("max_level", 10).forGetter(SkillDef::maxLevel),
            COST_CODEC.fieldOf("cost").forGetter(d -> new Cost(d.tpBase(), d.tpPerLevel(), d.mind())),
            SkillEffect.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(SkillDef::effects),
            Identifier.CODEC.listOf().optionalFieldOf("races", List.of()).forGetter(SkillDef::races)
    ).apply(i, (max, cost, effects, races) -> new SkillDef(max, cost.tpBase(),
            cost.tpPerLevel() < 0 ? cost.tpBase() : cost.tpPerLevel(), cost.mind(), effects, races)))
            .validate(SkillDef::validate);

    private static DataResult<SkillDef> validate(SkillDef d) {
        if (d.maxLevel() < 1 || d.maxLevel() > 1000) {
            return DataResult.error(() -> "max_level must be 1..1000");
        }
        if (d.tpBase() < 0 || d.tpPerLevel() < 0) {
            return DataResult.error(() -> "TP costs cannot be negative");
        }
        if (d.mind() < 0) {
            return DataResult.error(() -> "mind cannot be negative");
        }
        for (SkillEffect e : d.effects()) {
            if (e.type().isBlank()) {
                return DataResult.error(() -> "an effect needs a type");
            }
        }
        return DataResult.success(d);
    }

    /** TP to buy {@code level} (1-based): {@code tpBase + tpPerLevel * (level - 1)}. */
    public long tpCostOfLevel(int level) {
        return tpBase + tpPerLevel * Math.max(0, level - 1);
    }

    /** Mind points one level uses. */
    public int mindCostOfLevel() {
        return mind;
    }

    /** Total value of all effects of a type at the given level. */
    public double effect(String type, int level) {
        double total = 0.0;
        for (SkillEffect e : effects) {
            if (e.type().equals(type)) {
                total += e.at(level);
            }
        }
        return total;
    }
}
