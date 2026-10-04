package net.kimon.kimon.stats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * A player's chosen race and class, as ids into the {@link CharacterCatalog} (so datapacks can add
 * more). Immutable; serialized + synced via an attachment. The combined modifiers (race + class)
 * feed the stat calculator.
 *
 * <p>Old saves stored bare names ({@code "human"}); they are read as {@code kimon:human}.</p>
 *
 * @param raceId  id of the chosen race
 * @param classId id of the chosen class
 */
public record CharacterProfile(Identifier raceId, Identifier classId) {

    /** Default starting profile for a brand-new player. */
    public static final CharacterProfile DEFAULT =
            new CharacterProfile(CharacterCatalog.DEFAULT_RACE, CharacterCatalog.DEFAULT_CLASS);

    public static final MapCodec<CharacterProfile> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("race").forGetter(p -> p.raceId().toString()),
            Codec.STRING.fieldOf("class").forGetter(p -> p.classId().toString())
    ).apply(instance, CharacterProfile::fromKeys));

    public static final Codec<CharacterProfile> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, CharacterProfile> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, p.raceId().toString());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.classId().toString());
            },
            buf -> fromKeys(ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.STRING_UTF8.decode(buf))
    );

    /** Parses stored/synced text; anything unreadable falls back to the default race/class. */
    static CharacterProfile fromKeys(String raceKey, String classKey) {
        Identifier race = CharacterCatalog.parseId(raceKey);
        Identifier clazz = CharacterCatalog.parseId(classKey);
        return new CharacterProfile(
                race != null ? race : CharacterCatalog.DEFAULT_RACE,
                clazz != null ? clazz : CharacterCatalog.DEFAULT_CLASS);
    }

    /** Combined race + class modifiers, looked up in the catalog currently in use. */
    public StatMods mods() {
        return CharacterCatalog.current().modsFor(raceId, classId);
    }

    /** Translation key of the race name, e.g. {@code race.kimon.titan}. */
    public String raceKey() {
        return "race." + raceId.getNamespace() + "." + raceId.getPath();
    }

    /** Translation key of the class name, e.g. {@code class.kimon.warrior}. */
    public String classKey() {
        return "class." + classId.getNamespace() + "." + classId.getPath();
    }

    public CharacterProfile withRace(Identifier newRace) {
        return new CharacterProfile(newRace, classId);
    }

    public CharacterProfile withClass(Identifier newClass) {
        return new CharacterProfile(raceId, newClass);
    }
}
