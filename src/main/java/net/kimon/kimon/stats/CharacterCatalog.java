package net.kimon.kimon.stats;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.Identifier;

/**
 * Every race and class currently known, keyed by id ({@code namespace:name}). Loaded from datapacks
 * by {@code CharacterDataLoader} and synced to clients; the mod ships its defaults as plain JSON
 * ({@code data/kimon/races/*.json}, {@code data/kimon/classes/*.json}), which are also read from the
 * classpath as a fallback so everything works before the first load (and in unit tests).
 *
 * <p>Immutable once built. The "current" catalog is a single volatile reference that is swapped
 * whole on reload.</p>
 */
public final class CharacterCatalog {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String NAMESPACE = "kimon";
    public static final Identifier DEFAULT_RACE = id("human");
    public static final Identifier DEFAULT_CLASS = id("brawler");

    /** Names of the data files the mod ships (the JSON is the data; this list is only the index). */
    private static final String[] BUILTIN_RACES = {"human", "titan", "sage", "frost", "mystic", "hybrid"};
    private static final String[] BUILTIN_CLASSES = {"warrior", "brawler", "channeler"};

    private final Map<Identifier, RaceDef> races;
    private final Map<Identifier, ClassDef> classes;

    public CharacterCatalog(Map<Identifier, RaceDef> races, Map<Identifier, ClassDef> classes) {
        this.races = Collections.unmodifiableMap(new TreeMap<>(races));
        this.classes = Collections.unmodifiableMap(new TreeMap<>(classes));
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, path);
    }

    /**
     * Turns user input into an id: a full {@code namespace:name} is kept, a bare name (also what old
     * saves stored) is taken to be in the {@code kimon} namespace. Returns null if it isn't valid.
     */
    public static Identifier parseId(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String trimmed = text.trim().toLowerCase(java.util.Locale.ROOT);
        return trimmed.contains(":") ? Identifier.tryParse(trimmed) : Identifier.tryParse(NAMESPACE + ":" + trimmed);
    }

    // ----------------------------------------------------------------------- lookup
    public Set<Identifier> raceIds() {
        return races.keySet();
    }

    public Set<Identifier> classIds() {
        return classes.keySet();
    }

    public Map<Identifier, RaceDef> races() {
        return races;
    }

    public Map<Identifier, ClassDef> classes() {
        return classes;
    }

    public boolean hasRace(Identifier id) {
        return races.containsKey(id);
    }

    public boolean hasClass(Identifier id) {
        return classes.containsKey(id);
    }

    /** The race, or the default race if the id is unknown (e.g. a datapack removed it). */
    public RaceDef race(Identifier id) {
        RaceDef def = races.get(id);
        if (def == null) {
            def = races.get(DEFAULT_RACE);
        }
        return def != null ? def : EMERGENCY_RACE;
    }

    /** The class, or the default class if the id is unknown. */
    public ClassDef clazz(Identifier id) {
        ClassDef def = classes.get(id);
        if (def == null) {
            def = classes.get(DEFAULT_CLASS);
        }
        return def != null ? def : new ClassDef(StatMods.NONE);
    }

    /** Race and class modifiers added together. */
    public StatMods modsFor(Identifier raceId, Identifier classId) {
        return race(raceId).modifiers().plus(clazz(classId).modifiers());
    }

    // ------------------------------------------------------------------ current + builtin
    private static final RaceDef EMERGENCY_RACE = emergencyRace();

    private static volatile CharacterCatalog current = builtin();

    /** The catalog in use right now. */
    public static CharacterCatalog current() {
        return current;
    }

    /** Replaces the catalog in use (called on datapack reload and when the server syncs it). */
    public static void set(CharacterCatalog catalog) {
        current = catalog;
    }

    /** The catalog made of the JSON files the mod ships, read from the classpath. */
    public static CharacterCatalog builtin() {
        Map<Identifier, RaceDef> races = new TreeMap<>();
        for (String name : BUILTIN_RACES) {
            RaceDef def = readResource("races/" + name, RaceDef.CODEC);
            if (def != null) {
                races.put(id(name), def);
            }
        }
        Map<Identifier, ClassDef> classes = new TreeMap<>();
        for (String name : BUILTIN_CLASSES) {
            ClassDef def = readResource("classes/" + name, ClassDef.CODEC);
            if (def != null) {
                classes.put(id(name), def);
            }
        }
        return new CharacterCatalog(races, classes);
    }

    private static <T> T readResource(String path, com.mojang.serialization.Codec<T> codec) {
        String full = "/data/" + NAMESPACE + "/" + path + ".json";
        try (InputStream in = CharacterCatalog.class.getResourceAsStream(full)) {
            if (in == null) {
                LOGGER.warn("Built-in data file missing: {}", full);
                return null;
            }
            JsonElement json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            return codec.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(err -> LOGGER.error("Bad built-in data file {}: {}", full, err))
                    .orElse(null);
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Could not read built-in data file {}", full, e);
            return null;
        }
    }

    private static RaceDef emergencyRace() {
        EnumMap<Attribute, Integer> map = new EnumMap<>(Attribute.class);
        for (Attribute a : Attribute.VALUES) {
            map.put(a, 10);
        }
        return new RaceDef(map, StatMods.NONE);
    }
}
