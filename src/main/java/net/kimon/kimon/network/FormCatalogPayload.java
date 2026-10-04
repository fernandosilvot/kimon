package net.kimon.kimon.network;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.power.FormCatalog;
import net.kimon.kimon.power.FormDef;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Clientbound: the whole form catalog, sent on login and whenever datapacks reload. */
public record FormCatalogPayload(FormCatalog catalog) implements CustomPacketPayload {

    public static final Type<FormCatalogPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Kimon.MODID, "form_catalog"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FormCatalogPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.catalog().forms().size());
                payload.catalog().forms().forEach((id, def) -> {
                    buf.writeIdentifier(id);
                    buf.writeVarInt(def.races().size());
                    for (Identifier race : def.races()) {
                        buf.writeIdentifier(race);
                    }
                    buf.writeVarInt(def.order());
                    buf.writeBoolean(def.skill() != null);
                    if (def.skill() != null) {
                        buf.writeIdentifier(def.skill());
                    }
                    buf.writeVarInt(def.skillLevel());
                    buf.writeDouble(def.strMult());
                    buf.writeDouble(def.dexMult());
                    buf.writeDouble(def.wilMult());
                    buf.writeDouble(def.flatBonus());
                    buf.writeDouble(def.damageTakenDivisor());
                    buf.writeDouble(def.kiPerSecond());
                    buf.writeDouble(def.minRelease());
                });
            },
            buf -> {
                Map<Identifier, FormDef> forms = new LinkedHashMap<>();
                int n = buf.readVarInt();
                for (int i = 0; i < n; i++) {
                    Identifier id = buf.readIdentifier();
                    List<Identifier> races = new ArrayList<>();
                    int raceCount = buf.readVarInt();
                    for (int j = 0; j < raceCount; j++) {
                        races.add(buf.readIdentifier());
                    }
                    int order = buf.readVarInt();
                    Identifier skill = buf.readBoolean() ? buf.readIdentifier() : null;
                    int skillLevel = buf.readVarInt();
                    forms.put(id, new FormDef(races, order, skill, skillLevel,
                            buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                            buf.readDouble(), buf.readDouble(), buf.readDouble()));
                }
                return new FormCatalogPayload(new FormCatalog(forms));
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
