package net.kimon.kimon.network;

import io.netty.buffer.ByteBuf;
import net.kimon.kimon.Kimon;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Serverbound: the player asks to raise a skill by one level. The server checks TP, Mind budget and
 * the level cap; the client only names the skill.
 *
 * @param skillId the skill's id, e.g. {@code kimon:fly}
 */
public record LearnSkillPayload(String skillId) implements CustomPacketPayload {

    public static final Type<LearnSkillPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Kimon.MODID, "learn_skill"));

    public static final StreamCodec<ByteBuf, LearnSkillPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256), LearnSkillPayload::skillId,
            LearnSkillPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
