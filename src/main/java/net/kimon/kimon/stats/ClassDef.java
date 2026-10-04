package net.kimon.kimon.stats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A playable class as loaded from {@code data/<namespace>/classes/<name>.json}. A class only adds
 * percent modifiers on top of the race's (it never changes the starting attributes).
 *
 * <pre>{ "modifiers": { "melee": 10, "defense": -10, "body": 10 } }</pre>
 *
 * @param modifiers percent modifiers on the derived stats
 */
public record ClassDef(StatMods modifiers) {

    public static final Codec<ClassDef> CODEC = RecordCodecBuilder.create(i -> i.group(
            StatMods.CODEC.optionalFieldOf("modifiers", StatMods.NONE).forGetter(ClassDef::modifiers)
    ).apply(i, ClassDef::new));
}
