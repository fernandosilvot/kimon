package net.kimon.kimon.network;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.skill.SkillCatalog;
import net.kimon.kimon.skill.SkillDef;
import net.kimon.kimon.skill.SkillEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Clientbound: the whole skill catalog, sent on login and whenever datapacks reload. */
public record SkillCatalogPayload(SkillCatalog catalog) implements CustomPacketPayload {

    public static final Type<SkillCatalogPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Kimon.MODID, "skill_catalog"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillCatalogPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.catalog().skills().size());
                payload.catalog().skills().forEach((id, def) -> {
                    buf.writeIdentifier(id);
                    buf.writeVarInt(def.maxLevel());
                    buf.writeVarLong(def.tpBase());
                    buf.writeVarLong(def.tpPerLevel());
                    buf.writeVarInt(def.mind());
                    buf.writeVarInt(def.effects().size());
                    for (SkillEffect e : def.effects()) {
                        buf.writeUtf(e.type());
                        buf.writeDouble(e.base());
                        buf.writeDouble(e.perLevel());
                    }
                });
            },
            buf -> {
                Map<Identifier, SkillDef> skills = new LinkedHashMap<>();
                int n = buf.readVarInt();
                for (int i = 0; i < n; i++) {
                    Identifier id = buf.readIdentifier();
                    int max = buf.readVarInt();
                    long tpBase = buf.readVarLong();
                    long tpPerLevel = buf.readVarLong();
                    int mind = buf.readVarInt();
                    int effectCount = buf.readVarInt();
                    List<SkillEffect> effects = new java.util.ArrayList<>();
                    for (int j = 0; j < effectCount; j++) {
                        effects.add(new SkillEffect(buf.readUtf(), buf.readDouble(), buf.readDouble()));
                    }
                    skills.put(id, new SkillDef(max, tpBase, tpPerLevel, mind, effects));
                }
                return new SkillCatalogPayload(new SkillCatalog(skills));
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
