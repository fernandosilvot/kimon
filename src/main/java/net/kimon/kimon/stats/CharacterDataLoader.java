package net.kimon.kimon.stats;

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
 * Datapack reload listener: reads every {@code data/<ns>/races/*.json} and
 * {@code data/<ns>/classes/*.json} into a {@link CharacterCatalog} and makes it the current one.
 * Files that fail to parse are reported in the log and skipped; if nothing valid is found the
 * built-in defaults stay in use, so a broken datapack can never leave the game without races.
 */
public final class CharacterDataLoader extends SimplePreparableReloadListener<CharacterCatalog> {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    protected CharacterCatalog prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Identifier, RaceDef> races = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, FileToIdConverter.json("races"),
                JsonOps.INSTANCE, RaceDef.CODEC, races);
        Map<Identifier, ClassDef> classes = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, FileToIdConverter.json("classes"),
                JsonOps.INSTANCE, ClassDef.CODEC, classes);
        return new CharacterCatalog(races, classes);
    }

    @Override
    protected void apply(CharacterCatalog loaded, ResourceManager resourceManager, ProfilerFiller profiler) {
        CharacterCatalog catalog = loaded;
        if (loaded.races().isEmpty() || loaded.classes().isEmpty()) {
            LOGGER.warn("No valid races/classes found in datapacks; keeping the built-in ones");
            catalog = CharacterCatalog.builtin();
        }
        CharacterCatalog.set(catalog);
        LOGGER.info("Loaded {} races and {} classes", catalog.races().size(), catalog.classes().size());
    }
}
