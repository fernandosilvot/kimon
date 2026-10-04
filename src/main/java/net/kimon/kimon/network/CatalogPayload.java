package net.kimon.kimon.network;

import java.util.LinkedHashMap;
import java.util.Map;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.stats.Attribute;
import net.kimon.kimon.stats.CharacterCatalog;
import net.kimon.kimon.stats.ClassDef;
import net.kimon.kimon.stats.RaceDef;
import net.kimon.kimon.stats.StatMods;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Clientbound: the whole race/class catalog, sent on login and whenever datapacks reload, so the
 * client computes the same derived stats (HUD) as the server.
 *
 * @param catalog the server's catalog
 */
public record CatalogPayload(CharacterCatalog catalog) implements CustomPacketPayload {

    public static final Type<CatalogPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Kimon.MODID, "catalog"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CatalogPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                CharacterCatalog c = payload.catalog();
                buf.writeVarInt(c.races().size());
                c.races().forEach((id, def) -> {
                    buf.writeIdentifier(id);
                    for (Attribute a : Attribute.VALUES) {
                        buf.writeVarInt(def.attributes().get(a));
                    }
                    StatMods.STREAM_CODEC.encode(buf, def.modifiers());
                });
                buf.writeVarInt(c.classes().size());
                c.classes().forEach((id, def) -> {
                    buf.writeIdentifier(id);
                    StatMods.STREAM_CODEC.encode(buf, def.modifiers());
                });
            },
            buf -> {
                Map<Identifier, RaceDef> races = new LinkedHashMap<>();
                int raceCount = buf.readVarInt();
                for (int i = 0; i < raceCount; i++) {
                    Identifier id = buf.readIdentifier();
                    Map<Attribute, Integer> attributes = new java.util.EnumMap<>(Attribute.class);
                    for (Attribute a : Attribute.VALUES) {
                        attributes.put(a, buf.readVarInt());
                    }
                    races.put(id, new RaceDef(attributes, StatMods.STREAM_CODEC.decode(buf)));
                }
                Map<Identifier, ClassDef> classes = new LinkedHashMap<>();
                int classCount = buf.readVarInt();
                for (int i = 0; i < classCount; i++) {
                    Identifier id = buf.readIdentifier();
                    classes.put(id, new ClassDef(StatMods.STREAM_CODEC.decode(buf)));
                }
                return new CatalogPayload(new CharacterCatalog(races, classes));
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
