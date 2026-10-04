package net.kimon.kimon.power;

import java.util.function.Supplier;

import net.kimon.kimon.Kimon;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Registry of Kimon's {@link AttachmentType data attachments}.
 *
 * <p>Attachments are NeoForge's modern replacement for the old capability system. The {@link #POWER}
 * attachment stores a {@link PowerData} on each player entity. It is:</p>
 * <ul>
 *     <li><b>serialized</b> via {@link PowerData#CODEC}, so Power survives relog and world reload;</li>
 *     <li><b>synced</b> via {@link PowerData#STREAM_CODEC}, so the owning client always has the current
 *     value for the HUD (NeoForge pushes it automatically on {@code setData});</li>
 *     <li><b>copied on death</b>, so players don't lose their progression when they die.</li>
 * </ul>
 */
public final class ModAttachments {

    private ModAttachments() {
    }

    /** Deferred register bound to the NeoForge attachment-type registry under the "kimon" namespace. */
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Kimon.MODID);

    /** Per-player Power progression state. */
    public static final Supplier<AttachmentType<PowerData>> POWER = ATTACHMENT_TYPES.register(
            "power",
            () -> AttachmentType.builder(() -> PowerData.INITIAL)
                    .serialize(PowerData.MAP_CODEC)
                    // Sync the whole value to the owning player's client whenever it changes.
                    .sync((holder, to) -> holder == to, PowerData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    /**
     * Registers the attachment types to the given mod event bus.
     *
     * @param modEventBus the mod-specific event bus from the mod constructor
     */
    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
