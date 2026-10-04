package net.kimon.kimon.stats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A player's chosen {@link Race} and {@link PlayerClass}. Immutable; serialized + synced via an
 * attachment. The combined per-attribute modifier (race + class) feeds the stat calculator.
 *
 * @param race  the chosen race
 * @param clazz the chosen class
 */
public record CharacterProfile(Race race, PlayerClass clazz) {

    /** Default starting profile for a brand-new player. */
    public static final CharacterProfile DEFAULT = new CharacterProfile(Race.HUMAN, PlayerClass.BRAWLER);

    public static final MapCodec<CharacterProfile> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("race").forGetter(p -> p.race().key()),
            Codec.STRING.fieldOf("class").forGetter(p -> p.clazz().key())
    ).apply(instance, CharacterProfile::fromKeys));

    public static final Codec<CharacterProfile> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, CharacterProfile> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, p.race().key());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.clazz().key());
            },
            buf -> fromKeys(ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.STRING_UTF8.decode(buf))
    );

    private static CharacterProfile fromKeys(String raceKey, String classKey) {
        Race race = Race.byKey(raceKey);
        PlayerClass clazz = PlayerClass.byKey(classKey);
        return new CharacterProfile(
                race != null ? race : Race.HUMAN,
                clazz != null ? clazz : PlayerClass.BRAWLER);
    }

    /** Combined race + class percent modifier for an attribute (e.g. 0.4 = +40%). */
    public double totalModifier(Attribute attribute) {
        return race.modifier(attribute) + clazz.modifier(attribute);
    }

    public CharacterProfile withRace(Race newRace) {
        return new CharacterProfile(newRace, clazz);
    }

    public CharacterProfile withClass(PlayerClass newClass) {
        return new CharacterProfile(race, newClass);
    }
}
