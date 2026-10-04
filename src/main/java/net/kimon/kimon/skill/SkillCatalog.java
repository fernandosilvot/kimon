package net.kimon.kimon.skill;

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
 * Every skill currently known, keyed by id. Loaded from datapacks by {@code SkillDataLoader} and synced
 * to clients; the mod's own skills are plain JSON under {@code data/kimon/skills/}, also readable from
 * the classpath so everything works before the first load and in unit tests. Immutable; the "current"
 * catalog is one volatile reference swapped whole on reload.
 */
public final class SkillCatalog {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String NAMESPACE = "kimon";

    public static final Identifier JUMP = id("jump");
    public static final Identifier DASH = id("dash");
    public static final Identifier FLY = id("fly");
    public static final Identifier ENDURANCE = id("endurance");
    public static final Identifier POTENTIAL_UNLOCK = id("potential_unlock");
    public static final Identifier KI_SENSE = id("ki_sense");

    /** Names of the data files the mod ships (the JSON is the data; this list is only the index). */
    public static final Identifier SUPER_FORM = id("super_form");
    public static final Identifier HUMAN_POTENTIAL = id("human_potential");
    public static final Identifier POWER_BOOST = id("power_boost");
    public static final Identifier TRANSFORMATIONS = id("transformations");
    public static final Identifier ABILITIES = id("abilities");

    private static final Identifier[] BUILTIN = {JUMP, DASH, FLY, ENDURANCE, POTENTIAL_UNLOCK, KI_SENSE,
        SUPER_FORM, HUMAN_POTENTIAL, POWER_BOOST, TRANSFORMATIONS, ABILITIES};

    private final Map<Identifier, SkillDef> skills;

    public SkillCatalog(Map<Identifier, SkillDef> skills) {
        this.skills = Collections.unmodifiableMap(new TreeMap<>(skills));
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, path);
    }

    /** Bare names are taken to be in the {@code kimon} namespace. */
    public static Identifier parseId(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String trimmed = text.trim().toLowerCase(java.util.Locale.ROOT).replace(' ', '_');
        return trimmed.contains(":") ? Identifier.tryParse(trimmed) : Identifier.tryParse(NAMESPACE + ":" + trimmed);
    }

    public Map<Identifier, SkillDef> skills() {
        return skills;
    }

    /** The skill, or null if unknown. */
    public SkillDef get(Identifier id) {
        return skills.get(id);
    }

    public boolean has(Identifier id) {
        return skills.containsKey(id);
    }

    // ------------------------------------------------------------------ current + builtin
    private static volatile SkillCatalog current = builtin();

    public static SkillCatalog current() {
        return current;
    }

    public static void set(SkillCatalog catalog) {
        current = catalog;
    }

    /** The catalog made of the JSON files the mod ships, read from the classpath. */
    public static SkillCatalog builtin() {
        Map<Identifier, SkillDef> map = new TreeMap<>();
        for (Identifier id : BUILTIN) {
            String path = "/data/" + id.getNamespace() + "/skills/" + id.getPath() + ".json";
            try (InputStream in = SkillCatalog.class.getResourceAsStream(path)) {
                if (in == null) {
                    LOGGER.warn("Built-in skill file missing: {}", path);
                    continue;
                }
                SkillDef def = SkillDef.CODEC.parse(JsonOps.INSTANCE,
                                JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
                        .resultOrPartial(err -> LOGGER.error("Bad built-in skill file {}: {}", path, err))
                        .orElse(null);
                if (def != null) {
                    map.put(id, def);
                }
            } catch (IOException | RuntimeException e) {
                LOGGER.error("Could not read built-in skill file {}", path, e);
            }
        }
        return new SkillCatalog(map);
    }
}
