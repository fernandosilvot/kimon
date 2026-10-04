package net.kimon.kimon.power;

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
 * Datapack reload listener: reads every {@code data/<ns>/forms/*.json} into a {@link FormCatalog} and
 * makes it the current one. Bad files are reported and skipped; with nothing valid the built-in forms
 * stay in use.
 */
public final class FormDataLoader extends SimplePreparableReloadListener<FormCatalog> {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    protected FormCatalog prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Identifier, FormDef> forms = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, FileToIdConverter.json("forms"),
                JsonOps.INSTANCE, FormDef.CODEC, forms);
        return new FormCatalog(forms);
    }

    @Override
    protected void apply(FormCatalog loaded, ResourceManager resourceManager, ProfilerFiller profiler) {
        FormCatalog catalog = loaded.forms().isEmpty() ? FormCatalog.builtin() : loaded;
        if (loaded.forms().isEmpty()) {
            LOGGER.warn("No valid forms found in datapacks; keeping the built-in ones");
        }
        FormCatalog.set(catalog);
        LOGGER.info("Loaded {} forms", catalog.forms().size());
    }
}
