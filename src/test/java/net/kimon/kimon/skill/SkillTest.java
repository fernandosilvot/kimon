package net.kimon.kimon.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

@DisplayName("Skills as data")
class SkillTest {

    private static final SkillCatalog CATALOG = SkillCatalog.builtin();

    private static DataResult<SkillDef> parse(String json) {
        return SkillDef.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
    }

    private static RegistryFriendlyByteBuf buffer() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
    }

    // --------------------------------------------------------------- shipped data
    @Test
    @DisplayName("the mod ships the six skills of step 4")
    void shipsSkills() {
        assertEquals(6, CATALOG.skills().size());
        for (Identifier id : new Identifier[] {SkillCatalog.JUMP, SkillCatalog.DASH, SkillCatalog.FLY,
                SkillCatalog.ENDURANCE, SkillCatalog.POTENTIAL_UNLOCK, SkillCatalog.KI_SENSE}) {
            assertTrue(CATALOG.has(id), id.toString());
            assertEquals(10, CATALOG.get(id).maxLevel(), id + " max level");
        }
    }

    @Test
    @DisplayName("costs match the research table (TP / Mind)")
    void costsMatchTable() {
        Object[][] expected = {
            {SkillCatalog.JUMP, 40L, 5}, {SkillCatalog.FLY, 60L, 10}, {SkillCatalog.ENDURANCE, 150L, 10},
            {SkillCatalog.POTENTIAL_UNLOCK, 400L, 10}, {SkillCatalog.KI_SENSE, 300L, 10},
        };
        for (Object[] row : expected) {
            SkillDef def = CATALOG.get((Identifier) row[0]);
            assertEquals((Long) row[1], def.tpCostOfLevel(1), row[0] + " TP");
            assertEquals((Integer) row[2], def.mindCostOfLevel(), row[0] + " Mind");
        }
    }

    @Test
    @DisplayName("each level costs the base again, cumulatively: base * n")
    void incrementalCost() {
        SkillDef fly = CATALOG.get(SkillCatalog.FLY);
        assertEquals(60L, fly.tpCostOfLevel(1));
        assertEquals(120L, fly.tpCostOfLevel(2));
        assertEquals(600L, fly.tpCostOfLevel(10));
        long total = 0;
        for (int n = 1; n <= 10; n++) {
            total += fly.tpCostOfLevel(n);
        }
        assertEquals(60L * 55, total, "60 * (1+2+...+10)");
    }

    @Test
    @DisplayName("Potential Unlock: +5% Release per level, 50% -> 100% at level 10")
    void potentialUnlock() {
        SkillData lv = new SkillData(Map.of(SkillCatalog.POTENTIAL_UNLOCK, 1));
        assertEquals(5.0, SkillEffects.releaseCap(lv, CATALOG), 1e-9);
        assertEquals(50.0, SkillEffects.releaseCap(lv.with(SkillCatalog.POTENTIAL_UNLOCK, 10), CATALOG), 1e-9);
        assertEquals(0.0, SkillEffects.releaseCap(SkillData.EMPTY, CATALOG), 1e-9);
    }

    @Test
    @DisplayName("Endurance: -3% damage per level (30% at level 10), multiplicative with nothing else here")
    void endurance() {
        assertEquals(1.0, SkillEffects.damageTakenFactor(SkillData.EMPTY, CATALOG), 1e-9);
        assertEquals(0.97, SkillEffects.damageTakenFactor(new SkillData(Map.of(SkillCatalog.ENDURANCE, 1)), CATALOG), 1e-9);
        assertEquals(0.70, SkillEffects.damageTakenFactor(new SkillData(Map.of(SkillCatalog.ENDURANCE, 10)), CATALOG), 1e-9);
    }

    @Test
    @DisplayName("Jump gives +10% jump and +1 safe-fall block per level; Fly +10% speed per level")
    void jumpAndFly() {
        SkillData data = new SkillData(Map.of(SkillCatalog.JUMP, 5, SkillCatalog.FLY, 3));
        assertEquals(50.0, SkillEffects.total(data, CATALOG, SkillEffect.JUMP_BOOST), 1e-9);
        assertEquals(5.0, SkillEffects.total(data, CATALOG, SkillEffect.SAFE_FALL), 1e-9);
        assertEquals(30.0, SkillEffects.total(data, CATALOG, SkillEffect.FLIGHT), 1e-9);
        assertTrue(SkillEffects.has(data, CATALOG, SkillEffect.FLIGHT));
        assertFalse(SkillEffects.has(data, CATALOG, SkillEffect.DASH));
    }

    @Test
    @DisplayName("flight speed is vanilla's plus the Fly bonus")
    void flightSpeed() {
        assertEquals(0.05f, FlightHandler.flyingSpeed(SkillData.EMPTY, CATALOG), 1e-6f);
        assertEquals(0.10f, FlightHandler.flyingSpeed(new SkillData(Map.of(SkillCatalog.FLY, 10)), CATALOG), 1e-6f);
    }

    @Test
    @DisplayName("Dash strength and Ki Sense range grow with the level")
    void dashAndSense() {
        SkillData d1 = new SkillData(Map.of(SkillCatalog.DASH, 1, SkillCatalog.KI_SENSE, 1));
        SkillData d10 = new SkillData(Map.of(SkillCatalog.DASH, 10, SkillCatalog.KI_SENSE, 10));
        assertTrue(SkillEffects.total(d10, CATALOG, SkillEffect.DASH) > SkillEffects.total(d1, CATALOG, SkillEffect.DASH));
        assertEquals(10.0, SkillEffects.total(d1, CATALOG, SkillEffect.KI_SENSE), 1e-9);
        assertEquals(100.0, SkillEffects.total(d10, CATALOG, SkillEffect.KI_SENSE), 1e-9);
    }

    // ------------------------------------------------------------------- JSON
    @Test
    @DisplayName("a minimal skill parses with sensible defaults")
    void minimalSkill() {
        SkillDef def = parse("{\"cost\":{\"tp_base\":25}}").getOrThrow();
        assertEquals(10, def.maxLevel());
        assertEquals(25L, def.tpBase());
        assertEquals(25L, def.tpPerLevel(), "tp_per_level defaults to tp_base");
        assertEquals(0, def.mind());
        assertTrue(def.effects().isEmpty());
    }

    @Test
    @DisplayName("an explicit tp_per_level is honoured")
    void explicitPerLevel() {
        SkillDef def = parse("{\"cost\":{\"tp_base\":150,\"tp_per_level\":15,\"mind\":10}}").getOrThrow();
        assertEquals(150L, def.tpCostOfLevel(1));
        assertEquals(165L, def.tpCostOfLevel(2));
        assertEquals(150L + 15L * 9, def.tpCostOfLevel(10));
    }

    @Test
    @DisplayName("invalid skills are rejected")
    void invalid() {
        assertTrue(parse("{}").error().isPresent(), "no cost");
        assertTrue(parse("{\"max_level\":0,\"cost\":{\"tp_base\":1}}").error().isPresent(), "max level 0");
        assertTrue(parse("{\"max_level\":5000,\"cost\":{\"tp_base\":1}}").error().isPresent(), "max level too big");
        assertTrue(parse("{\"cost\":{\"tp_base\":-1}}").error().isPresent(), "negative TP");
        assertTrue(parse("{\"cost\":{\"tp_base\":1,\"mind\":-2}}").error().isPresent(), "negative mind");
        assertTrue(parse("{\"cost\":{\"tp_base\":1},\"effects\":[{\"type\":\"\"}]}").error().isPresent(), "blank effect type");
        assertTrue(parse("{\"cost\":{\"tp_base\":1},\"effects\":[{}]}").error().isPresent(), "effect without type");
        assertTrue(parse("[]").error().isPresent(), "wrong json type");
    }

    @Test
    @DisplayName("a skill survives an encode/decode round trip")
    void roundTrip() {
        SkillDef original = CATALOG.get(SkillCatalog.JUMP);
        var json = SkillDef.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        assertEquals(original, SkillDef.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    // ----------------------------------------------------------------- skill data
    @Test
    @DisplayName("levels: absent means 0, setting 0 removes, negatives are ignored")
    void levels() {
        SkillData data = SkillData.EMPTY.with(SkillCatalog.FLY, 3);
        assertEquals(3, data.level(SkillCatalog.FLY));
        assertEquals(0, data.level(SkillCatalog.JUMP));
        assertEquals(0, data.with(SkillCatalog.FLY, 0).level(SkillCatalog.FLY));
        assertTrue(data.with(SkillCatalog.FLY, 0).levels().isEmpty());
        assertTrue(new SkillData(Map.of(SkillCatalog.FLY, -4)).levels().isEmpty());
    }

    @Test
    @DisplayName("Mind used is the sum of every learned level's Mind cost")
    void mindUsed() {
        SkillData data = new SkillData(Map.of(SkillCatalog.JUMP, 3, SkillCatalog.FLY, 2)); // 3*5 + 2*10
        assertEquals(35, data.mindUsed(CATALOG));
        assertEquals(0, SkillData.EMPTY.mindUsed(CATALOG));
        Identifier ghost = Identifier.fromNamespaceAndPath("gone", "skill");
        assertEquals(35, data.with(ghost, 9).mindUsed(CATALOG), "unknown skills use no Mind");
    }

    @Test
    @DisplayName("skill data round-trips through the codec and the network")
    void dataRoundTrips() {
        SkillData data = new SkillData(Map.of(SkillCatalog.JUMP, 3, SkillCatalog.POTENTIAL_UNLOCK, 7));
        assertEquals(data, SkillData.CODEC.parse(JsonOps.INSTANCE,
                SkillData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow()).getOrThrow());
        RegistryFriendlyByteBuf buf = buffer();
        SkillData.STREAM_CODEC.encode(buf, data);
        assertEquals(data, SkillData.STREAM_CODEC.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    // ----------------------------------------------------------------- learning
    @Test
    @DisplayName("learning spends the exact TP and adds a level")
    void learnSpends() {
        SkillRules.Result r = SkillRules.learn(SkillData.EMPTY, CATALOG, SkillCatalog.FLY, 1000, 100);
        assertTrue(r.learned());
        assertEquals(60L, r.tpSpent());
        assertEquals(1, r.data().level(SkillCatalog.FLY));
        SkillRules.Result second = SkillRules.learn(r.data(), CATALOG, SkillCatalog.FLY, 1000, 100);
        assertEquals(120L, second.tpSpent(), "level 2 costs base * 2");
        assertEquals(2, second.data().level(SkillCatalog.FLY));
    }

    @Test
    @DisplayName("each failure leaves everything untouched and says why")
    void learnFailures() {
        SkillData data = new SkillData(Map.of(SkillCatalog.FLY, 1));
        SkillRules.Result noTp = SkillRules.learn(data, CATALOG, SkillCatalog.FLY, 119, 100);
        assertEquals(SkillRules.Outcome.NOT_ENOUGH_TP, noTp.outcome());
        assertSame(data, noTp.data());
        assertEquals(0L, noTp.tpSpent());

        SkillRules.Result noMind = SkillRules.learn(data, CATALOG, SkillCatalog.FLY, 10_000, 19);
        assertEquals(SkillRules.Outcome.NOT_ENOUGH_MIND, noMind.outcome(), "10 used + 10 needed > 19");
        assertSame(data, noMind.data());

        Identifier ghost = Identifier.fromNamespaceAndPath("gone", "skill");
        assertEquals(SkillRules.Outcome.UNKNOWN_SKILL, SkillRules.learn(data, CATALOG, ghost, 10_000, 100).outcome());

        SkillData maxed = new SkillData(Map.of(SkillCatalog.FLY, 10));
        SkillRules.Result atMax = SkillRules.learn(maxed, CATALOG, SkillCatalog.FLY, 10_000, 1000);
        assertEquals(SkillRules.Outcome.MAX_LEVEL, atMax.outcome());
        assertSame(maxed, atMax.data());
    }

    @Test
    @DisplayName("exactly enough TP and Mind is enough")
    void exactlyEnough() {
        assertTrue(SkillRules.learn(SkillData.EMPTY, CATALOG, SkillCatalog.JUMP, 40, 5).learned());
        assertFalse(SkillRules.learn(SkillData.EMPTY, CATALOG, SkillCatalog.JUMP, 39, 5).learned());
        assertFalse(SkillRules.learn(SkillData.EMPTY, CATALOG, SkillCatalog.JUMP, 40, 4).learned());
    }

    @Test
    @DisplayName("Mind limits how many skill levels you can hold at once")
    void mindIsABudget() {
        int budget = SkillRules.mindBudget(25, 1.0); // 25 Mind
        SkillData data = SkillData.EMPTY;
        int learned = 0;
        for (int i = 0; i < 20; i++) {
            SkillRules.Result r = SkillRules.learn(data, CATALOG, SkillCatalog.JUMP, 1_000_000, budget); // 5 Mind each
            if (!r.learned()) {
                assertEquals(SkillRules.Outcome.NOT_ENOUGH_MIND, r.outcome());
                break;
            }
            data = r.data();
            learned++;
        }
        assertEquals(5, learned, "25 Mind / 5 per level");
        assertTrue(data.mindUsed(CATALOG) <= budget);
    }

    @Test
    @DisplayName("Mind budget is attribute * perPoint, rounded down and never negative")
    void budget() {
        assertEquals(10, SkillRules.mindBudget(10, 1.0));
        assertEquals(15, SkillRules.mindBudget(10, 1.5));
        assertEquals(3, SkillRules.mindBudget(7, 0.5));
        assertEquals(0, SkillRules.mindBudget(-3, 1.0));
        assertEquals(0, SkillRules.mindBudget(10, -1.0));
    }

    @Test
    @DisplayName("nextCost reports the price, or -1 when unknown or maxed")
    void nextCost() {
        assertEquals(40L, SkillRules.nextCost(SkillData.EMPTY, CATALOG, SkillCatalog.JUMP));
        assertEquals(80L, SkillRules.nextCost(new SkillData(Map.of(SkillCatalog.JUMP, 1)), CATALOG, SkillCatalog.JUMP));
        assertEquals(-1L, SkillRules.nextCost(new SkillData(Map.of(SkillCatalog.JUMP, 10)), CATALOG, SkillCatalog.JUMP));
        assertEquals(-1L, SkillRules.nextCost(SkillData.EMPTY, CATALOG, Identifier.fromNamespaceAndPath("gone", "x")));
    }

    // -------------------------------------------------------------- datapack skills
    @Test
    @DisplayName("a datapack skill can add to an existing effect type")
    void customSkillStacks() {
        Identifier tough = Identifier.fromNamespaceAndPath("mypack", "iron_skin");
        Map<Identifier, SkillDef> skills = new TreeMap<>(CATALOG.skills());
        skills.put(tough, parse("{\"max_level\":3,\"cost\":{\"tp_base\":10},\"effects\":[{\"type\":\"damage_reduction\",\"per_level\":5}]}").getOrThrow());
        SkillCatalog custom = new SkillCatalog(skills);
        SkillData data = new SkillData(Map.of(SkillCatalog.ENDURANCE, 10, tough, 3));
        assertEquals(1.0 - 0.30 - 0.15, SkillEffects.damageTakenFactor(data, custom), 1e-9);
        assertEquals(0.55, SkillEffects.damageTakenFactor(data, custom), 1e-9);
    }

    @Test
    @DisplayName("damage reduction can never push damage below zero; levels are capped at the skill's max")
    void clamps() {
        Identifier god = Identifier.fromNamespaceAndPath("mypack", "god_skin");
        SkillCatalog custom = new SkillCatalog(Map.of(god,
                parse("{\"max_level\":2,\"cost\":{\"tp_base\":1},\"effects\":[{\"type\":\"damage_reduction\",\"per_level\":80}]}").getOrThrow()));
        assertEquals(0.0, SkillEffects.damageTakenFactor(new SkillData(Map.of(god, 2)), custom), 1e-9);
        assertEquals(0.2, SkillEffects.damageTakenFactor(new SkillData(Map.of(god, 1)), custom), 1e-9);
        assertEquals(0.0, SkillEffects.damageTakenFactor(new SkillData(Map.of(god, 99)), custom), 1e-9,
                "a level above max counts as max, not more");
    }

    @Test
    @DisplayName("ids: bare names are kimon:, full ids kept, junk rejected")
    void ids() {
        assertEquals(SkillCatalog.FLY, SkillCatalog.parseId("fly"));
        assertEquals(SkillCatalog.POTENTIAL_UNLOCK, SkillCatalog.parseId("Potential Unlock"));
        assertEquals(Identifier.fromNamespaceAndPath("mypack", "x"), SkillCatalog.parseId("mypack:x"));
        assertNull(SkillCatalog.parseId(""));
        assertNull(SkillCatalog.parseId(null));
        assertNotNull(SkillCatalog.current());
    }

    // ------------------------------------------------------------------- dash
    @Test
    @DisplayName("dash directions are relative to where you look, and never forward")
    void dashDirections() {
        // yaw 0 faces +Z (south): forward = (0,1), left = (1,0), right = (-1,0), back = (0,-1)
        assertArrayEquals(new double[] {0, -1}, DashHandler.direction(0, DashHandler.BACK));
        assertArrayEquals(new double[] {1, 0}, DashHandler.direction(0, DashHandler.LEFT));
        assertArrayEquals(new double[] {-1, 0}, DashHandler.direction(0, DashHandler.RIGHT));
        // yaw 90 faces -X (west): back = +X, left = +Z... (left of west is south)
        assertArrayEquals(new double[] {1, 0}, DashHandler.direction(90, DashHandler.BACK));
        assertArrayEquals(new double[] {0, 1}, DashHandler.direction(90, DashHandler.LEFT));
        assertNull(DashHandler.direction(0, 3), "forward is not allowed");
        assertNull(DashHandler.direction(0, -1));
        for (int yaw = -180; yaw <= 180; yaw += 15) {
            for (int d = 0; d <= 2; d++) {
                double[] v = DashHandler.direction(yaw, d);
                assertEquals(1.0, Math.hypot(v[0], v[1]), 1e-9, "unit length at yaw " + yaw);
            }
        }
    }

    private static void assertArrayEquals(double[] expected, double[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i], 1e-9, "component " + i);
        }
    }

    // ----------------------------------------------------------- catalog payload
    @Test
    @DisplayName("the skill catalog survives the wire exactly")
    void catalogPayload() {
        RegistryFriendlyByteBuf buf = buffer();
        net.kimon.kimon.network.SkillCatalogPayload.STREAM_CODEC.encode(buf,
                new net.kimon.kimon.network.SkillCatalogPayload(CATALOG));
        SkillCatalog decoded = net.kimon.kimon.network.SkillCatalogPayload.STREAM_CODEC.decode(buf).catalog();
        assertEquals(CATALOG.skills(), decoded.skills());
        assertEquals(0, buf.readableBytes());
        List<SkillEffect> effects = decoded.get(SkillCatalog.JUMP).effects();
        assertEquals(2, effects.size());
    }

    @Test
    @DisplayName("an effect's value is 0 at level 0 and base + perLevel * level above")
    void effectAt() {
        SkillEffect e = new SkillEffect(SkillEffect.DASH, 0.6, 0.08);
        assertEquals(0.0, e.at(0), 1e-9);
        assertEquals(0.68, e.at(1), 1e-9);
        assertEquals(1.4, e.at(10), 1e-9);
        assertEquals(0.0, e.at(-3), 1e-9);
    }
}
