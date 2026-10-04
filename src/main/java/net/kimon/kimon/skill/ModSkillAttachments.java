package net.kimon.kimon.skill;

import java.util.function.Supplier;

import net.kimon.kimon.Kimon;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Data attachments for skills. */
public final class ModSkillAttachments {

    private ModSkillAttachments() {
    }

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Kimon.MODID);

    /** The player's skill levels. Serialized, synced to the owner, kept across death. */
    public static final Supplier<AttachmentType<SkillData>> SKILLS = ATTACHMENT_TYPES.register(
            "skills",
            () -> AttachmentType.builder(() -> SkillData.EMPTY)
                    .serialize(SkillData.CODEC.fieldOf("levels"))
                    .sync((holder, to) -> holder == to, SkillData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
