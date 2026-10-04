package net.kimon.kimon.power;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.Identifier;

/**
 * Every transformation currently known, keyed by id (Base is implicit and not in here). Loaded from
 * datapacks by {@code FormDataLoader} and synced to clients; the mod ships its forms as JSON under
 * {@code data/kimon/forms/}, also readable from the classpath as a fallback and for unit tests.
 */
public final class FormCatalog {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<String, String> LEGACY_NAMES = Map.of(
            "surge", "super_saiyan", "ascent", "super_saiyan_2", "zenith", "super_saiyan_3");

    /** Names of the data files the mod ships (the JSON is the data; this is only the index). */
    private static final String[] BUILTIN = {
        "super_saiyan", "super_saiyan_grade_2", "super_saiyan_grade_3", "super_saiyan_2", "super_saiyan_3",
        "super_saiyan_4", "human_full_released", "human_buffed", "namekian_full_released", "giant_form",
        "fifth_form", "golden_form", "evil_majin", "super_majin", "pure_majin"
    };

    private final Map<Identifier, FormDef> forms;

    public FormCatalog(Map<Identifier, FormDef> forms) {
        this.forms = Collections.unmodifiableMap(new TreeMap<>(forms));
    }

    public Map<Identifier, FormDef> forms() {
        return forms;
    }

    public boolean has(Identifier id) {
        return forms.containsKey(id);
    }

    public FormDef get(Identifier id) {
        return forms.get(id);
    }

    /** The form's definition, or the neutral Base one if unknown. */
    public FormDef formOrBase(Identifier id) {
        FormDef def = forms.get(id);
        return def != null ? def : FormDef.BASE;
    }

    /**
     * Turns text into a form id: a full id is kept, a bare name is {@code kimon:}, spaces become
     * underscores and old names are mapped. Null if it isn't a valid id.
     */
    public static Identifier parseId(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String normalized = Form.normalize(text);
        if (!normalized.contains(":")) {
            normalized = Form.NAMESPACE + ":" + LEGACY_NAMES.getOrDefault(normalized, normalized);
        }
        return Identifier.tryParse(normalized);
    }

    // ------------------------------------------------------------------ current + builtin
    private static volatile FormCatalog current = builtin();

    public static FormCatalog current() {
        return current;
    }

    public static void set(FormCatalog catalog) {
        current = catalog;
    }

    /** The catalog made of the JSON files the mod ships, read from the classpath. */
    public static FormCatalog builtin() {
        Map<Identifier, FormDef> map = new TreeMap<>();
        for (String name : BUILTIN) {
            String path = "/data/" + Form.NAMESPACE + "/forms/" + name + ".json";
            try (InputStream in = FormCatalog.class.getResourceAsStream(path)) {
                if (in == null) {
                    LOGGER.warn("Built-in form file missing: {}", path);
                    continue;
                }
                FormDef def = FormDef.CODEC.parse(JsonOps.INSTANCE,
                                JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
                        .resultOrPartial(err -> LOGGER.error("Bad built-in form file {}: {}", path, err))
                        .orElse(null);
                if (def != null) {
                    map.put(Identifier.fromNamespaceAndPath(Form.NAMESPACE, name), def);
                }
            } catch (IOException | RuntimeException e) {
                LOGGER.error("Could not read built-in form file {}", path, e);
            }
        }
        return new FormCatalog(map);
    }
}
