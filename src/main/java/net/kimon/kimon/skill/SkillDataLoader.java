package net.kimon.kimon.skill;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * Datapack reload listener: reads every {@code data/<ns>/skills/*.json} into a {@link SkillCatalog}
 * and makes it the current one. Bad files are reported and skipped; with nothing valid the built-in
 * skills stay in use.
 */
public final class SkillDataLoader extends SimplePreparableReloadListener<SkillCatalog> {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    protected SkillCatalog prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Identifier, SkillDef> skills = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, FileToIdConverter.json("skills"),
                JsonOps.INSTANCE, SkillDef.CODEC, skills);
        return new SkillCatalog(skills);
    }

    @Override
    protected void apply(SkillCatalog loaded, ResourceManager resourceManager, ProfilerFiller profiler) {
        SkillCatalog catalog = loaded.skills().isEmpty() ? SkillCatalog.builtin() : loaded;
        if (loaded.skills().isEmpty()) {
            LOGGER.warn("No valid skills found in datapacks; keeping the built-in ones");
        }
        SkillCatalog.set(catalog);
        LOGGER.info("Loaded {} skills", catalog.skills().size());
    }
}
