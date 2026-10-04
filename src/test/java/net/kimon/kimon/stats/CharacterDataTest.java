package net.kimon.kimon.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.Identifier;

@DisplayName("Races and classes as data (docs/02 and docs/03 tables as the oracle)")
class CharacterDataTest {

    private static final CharacterCatalog CATALOG = CharacterCatalog.builtin();

    private static Identifier id(String name) {
        return CharacterCatalog.id(name);
    }

    /** A row of the research tables: melee, defense, body, AT (stamina), ki power, max ki, run, fly. */
    private static StatMods row(double melee, double def, double body, double at, double kiP, double maxKi,
                                double run, double fly) {
        return new StatMods(melee, def, body, at, kiP, maxKi, run, fly);
    }

    // ----------------------------------------------------------------- shipped data
    @Test
    @DisplayName("the mod ships 6 races and 3 classes, all valid")
    void shipsAllData() {
        assertEquals(6, CATALOG.races().size());
        assertEquals(3, CATALOG.classes().size());
        for (String r : new String[] {"human", "saiyan", "namekian", "arcosian", "majin", "half_saiyan"}) {
            assertTrue(CATALOG.hasRace(id(r)), r);
        }
        for (String c : new String[] {"warrior", "martial_artist", "spiritualist"}) {
            assertTrue(CATALOG.hasClass(id(c)), c);
        }
    }

    @Test
    @DisplayName("every race starts with 60 attribute points, so everyone is level 1")
    void sixtyPointsEach() {
        for (Map.Entry<Identifier, RaceDef> e : CATALOG.races().entrySet()) {
            assertEquals(60, e.getValue().totalPoints(), e.getKey().toString());
            assertEquals(1, LevelCalculator.level(e.getValue().newStatBlock()), e.getKey().toString());
        }
    }

    @Test
    @DisplayName("starting spreads match the research table")
    void spreadsMatchTable() {
        // strength, agility, vitality, energy, focus, spirit  (= STR, DEX, CON, WIL, MND, SPI)
        int[][] expected = {
            {10, 10, 10, 10, 10, 10},   // human
            {15, 10, 10, 15, 5, 5},     // saiyan  (Saiyan)
            {10, 10, 10, 15, 10, 5},    // half-saiyan
            {5, 5, 7, 10, 15, 18},      // namekian
            {10, 15, 8, 7, 10, 10},     // majin
            {10, 5, 15, 10, 5, 15},     // arcosian (STR + WIL = 20 split evenly)
        };
        String[] names = {"human", "saiyan", "half_saiyan", "namekian", "majin", "arcosian"};
        for (int i = 0; i < names.length; i++) {
            RaceDef def = CATALOG.race(id(names[i]));
            for (int a = 0; a < Attribute.VALUES.length; a++) {
                assertEquals(expected[i][a], def.attributes().get(Attribute.VALUES[a]), names[i] + " " + Attribute.VALUES[a]);
            }
        }
        RaceDef frost = CATALOG.race(id("arcosian"));
        assertEquals(20, frost.attributes().get(Attribute.STRENGTH) + frost.attributes().get(Attribute.WILLPOWER));
    }

    @Test
    @DisplayName("race + class modifiers reproduce every row of the research tables")
    void modifiersMatchTables() {
        // [race][class] with classes in the order brawler (martial artist), channeler (spiritualist), warrior
        Object[][] table = {
            {"human",     row(0, 0, 0, 30, 10, 10, 10, 0),  row(-10, 10, -10, 20, 20, 20, 20, 10), row(10, -10, 10, 40, 0, 0, 0, -10)},
            {"saiyan",    row(30, 0, 0, 0, 20, 0, 0, 10),   row(20, 10, -10, -10, 30, 10, 10, 20),  row(40, -10, 10, 10, 10, -10, -10, 0)},
            {"half_saiyan", row(15, 0, 0, 15, 15, 5, 5, 5),   row(5, 10, -10, 5, 25, 15, 15, 15),     null},
            {"namekian", row(0, 0, 10, 0, 30, 20, 0, 0),   row(-10, 10, 0, -10, 40, 30, 10, 10),   row(10, -10, 20, 10, 20, 10, -10, -10)},
            {"majin",   row(10, 0, 0, 30, 10, 0, 10, 0),  row(0, 10, -10, 20, 20, 10, 20, 10),    row(20, -10, 10, 40, 0, -10, 0, -10)},
        };
        String[] classes = {"martial_artist", "spiritualist", "warrior"};
        for (Object[] r : table) {
            for (int c = 0; c < 3; c++) {
                StatMods expected = (StatMods) r[c + 1];
                if (expected == null) {
                    continue; // the research does not list this combination
                }
                assertEquals(expected, CATALOG.modsFor(id((String) r[0]), id(classes[c])), r[0] + " + " + classes[c]);
            }
        }
    }

    @Test
    @DisplayName("frost (the 'all classes' race) has +20% defense, +10% stamina and +30% speed")
    void frostBase() {
        StatMods base = CATALOG.race(id("arcosian")).modifiers();
        assertEquals(20.0, base.defense(), 1e-9);
        assertEquals(10.0, base.stamina(), 1e-9);
        assertEquals(30.0, base.run(), 1e-9);
    }

    @Test
    @DisplayName("the class offsets are the same on every race (data stays decomposable)")
    void classesAreRaceIndependent() {
        StatMods spirit = CATALOG.clazz(id("spiritualist")).modifiers();
        StatMods warrior = CATALOG.clazz(id("warrior")).modifiers();
        assertEquals(new StatMods(-10, 10, -10, -10, 10, 10, 10, 10), spirit);
        assertEquals(new StatMods(10, -10, 10, 10, -10, -10, -10, -10), warrior);
        assertEquals(StatMods.NONE, CATALOG.clazz(id("martial_artist")).modifiers());
    }

    // ------------------------------------------------------------------ JSON codecs
    private static com.mojang.serialization.DataResult<RaceDef> parseRace(String json) {
        return RaceDef.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
    }

    private static final String ALL_SIX =
            "\"attributes\":{\"strength\":10,\"dexterity\":10,\"constitution\":10,\"willpower\":10,\"mind\":10,\"spirit\":10}";

    @Test
    @DisplayName("a minimal race parses and its modifiers default to zero")
    void minimalRace() {
        RaceDef def = parseRace("{" + ALL_SIX + "}").getOrThrow();
        assertEquals(StatMods.NONE, def.modifiers());
        assertEquals(60, def.totalPoints());
    }

    @Test
    @DisplayName("partial modifiers parse, unspecified ones stay at zero")
    void partialModifiers() {
        RaceDef def = parseRace("{" + ALL_SIX + ",\"modifiers\":{\"ki_power\":25.5,\"run\":-5}}").getOrThrow();
        assertEquals(25.5, def.modifiers().kiPower(), 1e-9);
        assertEquals(-5.0, def.modifiers().run(), 1e-9);
        assertEquals(0.0, def.modifiers().melee(), 1e-9);
    }

    @Test
    @DisplayName("invalid races are rejected with a clear error")
    void invalidRaces() {
        assertTrue(parseRace("{\"attributes\":{\"strength\":10}}").error().isPresent(), "missing attributes");
        assertTrue(parseRace("{" + ALL_SIX.replace("\"spirit\":10", "\"spirit\":-1") + "}").error().isPresent(), "negative");
        assertTrue(parseRace("{" + ALL_SIX.replace("\"spirit\":10", "\"spirit\":999999") + "}").error().isPresent(), "too big");
        assertTrue(parseRace("{" + ALL_SIX.replace("}", ",\"luck\":5}") + "}").error().isPresent(), "unknown attribute");
        assertTrue(parseRace("{}").error().isPresent(), "no attributes at all");
        assertTrue(parseRace("[]").error().isPresent(), "wrong json type");
    }

    @Test
    @DisplayName("a race survives an encode/decode round trip")
    void raceRoundTrip() {
        RaceDef original = CATALOG.race(id("saiyan"));
        var json = RaceDef.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        assertEquals(original, RaceDef.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    @DisplayName("classes parse; missing modifiers mean none")
    void classCodec() {
        ClassDef def = ClassDef.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"modifiers\":{\"melee\":10}}")).getOrThrow();
        assertEquals(10.0, def.modifiers().melee(), 1e-9);
        assertEquals(StatMods.NONE,
                ClassDef.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow().modifiers());
    }

    // ------------------------------------------------------------------- StatMods
    @Test
    @DisplayName("mods add field by field; the factor is 1 + percent/100 and never negative")
    void modsMath() {
        assertEquals(new StatMods(30, 0, 0, 0, 0, 0, 0, 0),
                new StatMods(20, 0, 0, 0, 0, 0, 0, 0).plus(new StatMods(10, 0, 0, 0, 0, 0, 0, 0)));
        assertEquals(1.4, StatMods.factor(40), 1e-9);
        assertEquals(0.9, StatMods.factor(-10), 1e-9);
        assertEquals(0.0, StatMods.factor(-250), 1e-9);
        assertEquals(1.0, StatMods.factor(0), 1e-9);
    }

    // ----------------------------------------------------------------- catalog lookup
    @Test
    @DisplayName("ids: bare names are kimon:, full ids kept, junk rejected")
    void parseIds() {
        assertEquals(id("saiyan"), CharacterCatalog.parseId("titan"));
        assertEquals(id("saiyan"), CharacterCatalog.parseId("  TITAN "));
        assertEquals(Identifier.fromNamespaceAndPath("mypack", "orc"), CharacterCatalog.parseId("mypack:orc"));
        assertNull(CharacterCatalog.parseId(""));
        assertNull(CharacterCatalog.parseId(null));
        assertNull(CharacterCatalog.parseId("bad name!"));
    }

    @Test
    @DisplayName("unknown ids fall back to the default race/class instead of failing")
    void unknownFallsBack() {
        Identifier ghost = Identifier.fromNamespaceAndPath("gone", "race");
        assertEquals(CATALOG.race(CharacterCatalog.DEFAULT_RACE), CATALOG.race(ghost));
        assertEquals(CATALOG.clazz(CharacterCatalog.DEFAULT_CLASS), CATALOG.clazz(ghost));
        assertFalse(CATALOG.hasRace(ghost));
        assertNotNull(new CharacterCatalog(Map.of(), Map.of()).race(ghost), "even an empty catalog never returns null");
    }

    @Test
    @DisplayName("a datapack can add its own race and it is used")
    void customRace() {
        Map<Identifier, RaceDef> races = new TreeMap<>(CATALOG.races());
        Identifier orc = Identifier.fromNamespaceAndPath("mypack", "orc");
        races.put(orc, parseRace("{" + ALL_SIX + ",\"modifiers\":{\"melee\":50}}").getOrThrow());
        CharacterCatalog custom = new CharacterCatalog(races, CATALOG.classes());
        assertTrue(custom.hasRace(orc));
        assertEquals(50.0, custom.modsFor(orc, id("martial_artist")).melee(), 1e-9);
    }

    // ------------------------------------------------------------------------ profile
    @Test
    @DisplayName("names from the first versions still resolve to the current ids")
    void legacyNamesMigrate() {
        assertEquals(id("saiyan"), CharacterCatalog.parseId("titan"));
        assertEquals(id("namekian"), CharacterCatalog.parseId("sage"));
        assertEquals(id("arcosian"), CharacterCatalog.parseId("kimon:frost"));
        assertEquals(id("majin"), CharacterCatalog.parseId("MYSTIC"));
        assertEquals(id("half_saiyan"), CharacterCatalog.parseId("hybrid"));
        assertEquals(id("martial_artist"), CharacterCatalog.parseId("brawler"));
        assertEquals(id("spiritualist"), CharacterCatalog.parseId("channeler"));
        assertEquals(id("martial_artist"), CharacterCatalog.parseId("Martial Artist"), "spaces become underscores");
        assertEquals(Identifier.fromNamespaceAndPath("mypack", "titan"), CharacterCatalog.parseId("mypack:titan"),
                "other namespaces are never remapped");
        CharacterProfile oldSave = CharacterProfile.fromKeys("titan", "brawler");
        assertEquals(new CharacterProfile(id("saiyan"), id("martial_artist")), oldSave);
    }

    @Test
    @DisplayName("old saves with bare names load as kimon ids; garbage falls back to the default")
    void legacyProfiles() {
        assertEquals(new CharacterProfile(id("saiyan"), id("warrior")), CharacterProfile.fromKeys("titan", "warrior"));
        assertEquals(new CharacterProfile(id("namekian"), id("spiritualist")), CharacterProfile.fromKeys("kimon:sage", "kimon:channeler"));
        assertEquals(CharacterProfile.DEFAULT, CharacterProfile.fromKeys("???", ""));
    }

    @Test
    @DisplayName("a profile survives a codec round trip and builds translation keys")
    void profileRoundTrip() {
        CharacterProfile p = new CharacterProfile(id("arcosian"), id("warrior"));
        var nbt = CharacterProfile.CODEC.encodeStart(JsonOps.INSTANCE, p).getOrThrow();
        assertEquals(p, CharacterProfile.CODEC.parse(JsonOps.INSTANCE, nbt).getOrThrow());
        assertEquals("race.kimon.arcosian", p.raceKey());
        assertEquals("class.kimon.warrior", p.classKey());
        assertEquals(id("namekian"), p.withRace(id("namekian")).raceId());
        assertEquals(id("martial_artist"), p.withClass(id("martial_artist")).classId());
        assertEquals(CharacterProfile.DEFAULT.raceId(), id("human"));
    }

    // -------------------------------------------------- the calculator reads each stat's own column
    @Test
    @DisplayName("each derived stat uses its own modifier column")
    void calculatorUsesOwnColumns() {
        CharacterCatalog previous = CharacterCatalog.current();
        try {
            Identifier probe = Identifier.fromNamespaceAndPath("test", "probe");
            Map<Identifier, RaceDef> races = new TreeMap<>(CATALOG.races());
            races.put(probe, parseRace("{" + ALL_SIX
                    + ",\"modifiers\":{\"body\":100,\"max_ki\":50,\"stamina\":-50,\"melee\":10,\"run\":20}}").getOrThrow());
            CharacterCatalog.set(new CharacterCatalog(races, CATALOG.classes()));

            StatBlock stats = StatBlock.of(Map.of(Attribute.CONSTITUTION, 50, Attribute.SPIRIT, 20, Attribute.STRENGTH, 50,
                    Attribute.DEXTERITY, 50), 0);
            CharacterProfile probeProfile = new CharacterProfile(probe, id("martial_artist"));
            CharacterProfile plain = new CharacterProfile(id("human"), id("martial_artist"));
            StatMods humanMods = plain.mods();

            assertEquals(StatCalculator.bonusHealth(stats) * 2.0, StatCalculator.bonusHealth(stats, probeProfile), 1e-9);
            assertEquals(StatCalculator.maxEnergy(stats) * 1.5, StatCalculator.maxEnergy(stats, probeProfile), 1e-9);
            assertEquals(StatCalculator.maxStamina(stats) * 0.5, StatCalculator.maxStamina(stats, probeProfile), 1e-9);
            assertEquals(StatCalculator.bonusAttackDamage(stats) * 1.1, StatCalculator.bonusAttackDamage(stats, probeProfile), 1e-9);
            assertEquals(StatCalculator.bonusMovementSpeed(stats) * 1.2, StatCalculator.bonusMovementSpeed(stats, probeProfile), 1e-9);
            // Human: +30% stamina only affects stamina, not health.
            assertEquals(StatCalculator.bonusHealth(stats), StatCalculator.bonusHealth(stats, plain), 1e-9);
            assertEquals(StatCalculator.maxStamina(stats) * StatMods.factor(humanMods.stamina()),
                    StatCalculator.maxStamina(stats, plain), 1e-9);
            assertEquals(StatCalculator.maxEnergy(stats), StatCalculator.maxEnergy(stats, (CharacterProfile) null), 1e-9);
        } finally {
            CharacterCatalog.set(previous);
        }
    }
}
