package net.kimon.kimon.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.netty.buffer.Unpooled;
import net.kimon.kimon.stats.Attribute;
import net.kimon.kimon.stats.CharacterCatalog;
import net.kimon.kimon.stats.ClassDef;
import net.kimon.kimon.stats.RaceDef;
import net.kimon.kimon.stats.StatMods;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

@DisplayName("Catalog sync payload")
class CatalogPayloadTest {

    private static RegistryFriendlyByteBuf buffer() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
    }

    @Test
    @DisplayName("the shipped catalog survives the wire exactly")
    void roundTripBuiltin() {
        CharacterCatalog original = CharacterCatalog.builtin();
        RegistryFriendlyByteBuf buf = buffer();
        CatalogPayload.STREAM_CODEC.encode(buf, new CatalogPayload(original));
        CharacterCatalog decoded = CatalogPayload.STREAM_CODEC.decode(buf).catalog();

        assertEquals(original.races(), decoded.races());
        assertEquals(original.classes(), decoded.classes());
        assertEquals(0, buf.readableBytes(), "nothing left over");
    }

    @Test
    @DisplayName("custom races and fractional modifiers keep their exact values")
    void roundTripCustom() {
        Identifier orc = Identifier.fromNamespaceAndPath("mypack", "orc");
        EnumMap<Attribute, Integer> spread = new EnumMap<>(Attribute.class);
        spread.put(Attribute.STRENGTH, 25);
        spread.put(Attribute.DEXTERITY, 1);
        spread.put(Attribute.CONSTITUTION, 20);
        spread.put(Attribute.WILLPOWER, 3);
        spread.put(Attribute.MIND, 3);
        spread.put(Attribute.SPIRIT, 8);
        RaceDef race = new RaceDef(spread, new StatMods(12.5, -3.25, 0, 7, 0, 0, -1, 99));
        CharacterCatalog custom = new CharacterCatalog(Map.of(orc, race),
                Map.of(Identifier.fromNamespaceAndPath("mypack", "berserker"),
                        new ClassDef(new StatMods(50, 0, 0, 0, 0, 0, 0, 0))));

        RegistryFriendlyByteBuf buf = buffer();
        CatalogPayload.STREAM_CODEC.encode(buf, new CatalogPayload(custom));
        CharacterCatalog decoded = CatalogPayload.STREAM_CODEC.decode(buf).catalog();

        assertEquals(race, decoded.race(orc));
        assertEquals(custom.classes(), decoded.classes());
    }

    @Test
    @DisplayName("an empty catalog is valid on the wire")
    void roundTripEmpty() {
        RegistryFriendlyByteBuf buf = buffer();
        CatalogPayload.STREAM_CODEC.encode(buf, new CatalogPayload(new CharacterCatalog(Map.of(), Map.of())));
        CharacterCatalog decoded = CatalogPayload.STREAM_CODEC.decode(buf).catalog();
        assertEquals(0, decoded.races().size());
        assertEquals(0, decoded.classes().size());
    }
}
