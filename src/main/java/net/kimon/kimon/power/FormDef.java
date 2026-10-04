package net.kimon.kimon.power;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;

/**
 * A transformation as loaded from {@code data/<namespace>/forms/<name>.json}.
 *
 * <pre>{
 *   "races": ["kimon:saiyan", "kimon:half_saiyan"],
 *   "order": 1,
 *   "requires": { "skill": "kimon:super_form", "level": 1 },
 *   "multipliers": { "str": 1.5, "dex": 1.5, "wil": 1.5 },
 *   "flat_bonus": 10,
 *   "damage_taken_divisor": 1.5,
 *   "ki_per_second": 2.0,
 *   "min_release": 10.0
 * }</pre>
 *
 * <p>The attribute pipeline is {@code effective = max(attribute × multiplier, attribute + flat_bonus)}:
 * the flat bonus makes a form always worth something to a low-level character. The damage taken is divided
 * by {@code damage_taken_divisor}.</p>
 *
 * @param races              races that can use the form; empty means every race
 * @param order              position in the race's ladder (1 = first form above Base)
 * @param skill              skill whose level unlocks the form, or null for none
 * @param skillLevel         level of that skill needed
 * @param strMult            multiplier on STRENGTH (melee)
 * @param dexMult            multiplier on DEXTERITY (speed)
 * @param wilMult            multiplier on WILLPOWER (Ki attacks)
 * @param flatBonus          attribute points added if that beats the multiplier
 * @param damageTakenDivisor divides damage received (1 = no reduction)
 * @param kiPerSecond        Ki drained per second to stay in the form
 * @param minRelease         Release % needed to enter and to stay in the form
 */
public record FormDef(List<Identifier> races, int order, Identifier skill, int skillLevel,
                      double strMult, double dexMult, double wilMult, double flatBonus,
                      double damageTakenDivisor, double kiPerSecond, double minRelease) {

    /** The "no form" definition: everything neutral. */
    public static final FormDef BASE =
            new FormDef(List.of(), 0, null, 0, 1.0, 1.0, 1.0, 0.0, 1.0, 0.0, 0.0);

    public FormDef {
        races = List.copyOf(races);
    }

    /** Raw shapes of the nested JSON objects. */
    private record Requires(Identifier skill, int level) {
    }

    private record Multipliers(double str, double dex, double wil) {
    }

    private static final Codec<Requires> REQUIRES = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("skill").forGetter(Requires::skill),
            Codec.INT.optionalFieldOf("level", 1).forGetter(Requires::level)
    ).apply(i, Requires::new));

    private static final Codec<Multipliers> MULTIPLIERS = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("str", 1.0).forGetter(Multipliers::str),
            Codec.DOUBLE.optionalFieldOf("dex", 1.0).forGetter(Multipliers::dex),
            Codec.DOUBLE.optionalFieldOf("wil", 1.0).forGetter(Multipliers::wil)
    ).apply(i, Multipliers::new));

    public static final Codec<FormDef> CODEC = RecordCodecBuilder.<FormDef>create(i -> i.group(
            Identifier.CODEC.listOf().optionalFieldOf("races", List.of()).forGetter(FormDef::races),
            Codec.INT.optionalFieldOf("order", 1).forGetter(FormDef::order),
            REQUIRES.optionalFieldOf("requires").forGetter(d -> d.skill() == null ? Optional.empty()
                    : Optional.of(new Requires(d.skill(), d.skillLevel()))),
            MULTIPLIERS.optionalFieldOf("multipliers", new Multipliers(1.0, 1.0, 1.0))
                    .forGetter(d -> new Multipliers(d.strMult(), d.dexMult(), d.wilMult())),
            Codec.DOUBLE.optionalFieldOf("flat_bonus", 0.0).forGetter(FormDef::flatBonus),
            Codec.DOUBLE.optionalFieldOf("damage_taken_divisor", 1.0).forGetter(FormDef::damageTakenDivisor),
            Codec.DOUBLE.optionalFieldOf("ki_per_second", 0.0).forGetter(FormDef::kiPerSecond),
            Codec.DOUBLE.optionalFieldOf("min_release", 0.0).forGetter(FormDef::minRelease)
    ).apply(i, (races, order, requires, mult, flat, divisor, ki, release) -> new FormDef(races, order,
            requires.map(Requires::skill).orElse(null), requires.map(Requires::level).orElse(0),
            mult.str(), mult.dex(), mult.wil(), flat, divisor, ki, release))).validate(FormDef::validate);

    private static DataResult<FormDef> validate(FormDef d) {
        if (d.order() < 1) {
            return DataResult.error(() -> "order must be at least 1");
        }
        if (d.strMult() < 0 || d.dexMult() < 0 || d.wilMult() < 0) {
            return DataResult.error(() -> "multipliers cannot be negative");
        }
        if (d.damageTakenDivisor() <= 0) {
            return DataResult.error(() -> "damage_taken_divisor must be above 0");
        }
        if (d.kiPerSecond() < 0 || d.minRelease() < 0) {
            return DataResult.error(() -> "ki_per_second and min_release cannot be negative");
        }
        if (d.skill() != null && d.skillLevel() < 1) {
            return DataResult.error(() -> "requires.level must be at least 1");
        }
        return DataResult.success(d);
    }

    /** Whether the race can use this form (an empty race list means every race). */
    public boolean allowsRace(Identifier raceId) {
        return races.isEmpty() || races.contains(raceId);
    }

    /** The largest of the three attribute multipliers (a rough "how strong is this form"). */
    public double headlineMultiplier() {
        return Math.max(strMult, Math.max(dexMult, wilMult));
    }
}
