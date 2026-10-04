package net.kimon.kimon.stats;

import java.util.function.Supplier;

import net.kimon.kimon.Kimon;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Registers the {@link StatBlock} data attachment: the player's six attributes + Training Points.
 *
 * <p>Serialized (persists across relog), synced to the owning client (so the stats GUI/HUD can read
 * it), and copied on death (progression is not lost).</p>
 */
public final class ModStatAttachments {

    private ModStatAttachments() {
    }

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Kimon.MODID);

    public static final Supplier<AttachmentType<StatBlock>> STATS = ATTACHMENT_TYPES.register(
            "stats",
            () -> AttachmentType.builder(StatBlock::initial)
                    .serialize(StatCodecs.MAP_CODEC)
                    .sync((holder, to) -> holder == to, StatCodecs.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    /** The player's chosen race + class. Serialized, synced, kept across death. */
    public static final Supplier<AttachmentType<CharacterProfile>> PROFILE = ATTACHMENT_TYPES.register(
            "profile",
            () -> AttachmentType.builder(() -> CharacterProfile.DEFAULT)
                    .serialize(CharacterProfile.MAP_CODEC)
                    .sync((holder, to) -> holder == to, CharacterProfile.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    /**
     * What the player is training under (carried weight and gravity). Not serialized (recomputed every
     * second by {@code TrainingHandler}); synced to the owner for the HUD, and it changes rarely.
     */
    public static final Supplier<AttachmentType<net.kimon.kimon.training.TrainingLoad>> LOAD =
            ATTACHMENT_TYPES.register(
                    "load",
                    () -> AttachmentType.builder(() -> net.kimon.kimon.training.TrainingLoad.NONE)
                            .sync((holder, to) -> holder == to,
                                    net.kimon.kimon.training.TrainingLoad.STREAM_CODEC)
                            .build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
