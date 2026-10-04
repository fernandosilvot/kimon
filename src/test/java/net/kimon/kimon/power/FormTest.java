package net.kimon.kimon.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import net.kimon.kimon.network.FormCatalogPayload;
import net.kimon.kimon.skill.SkillCatalog;
import net.kimon.kimon.skill.SkillData;
import net.kimon.kimon.skill.SkillDef;
import net.kimon.kimon.skill.SkillRules;
import net.kimon.kimon.stats.CharacterCatalog;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

@DisplayName("Forms as data")
class FormTest {

    private static final FormCatalog CATALOG = FormCatalog.builtin();
    private static final Identifier SAIYAN = CharacterCatalog.id("saiyan");
    private static final Identifier HALF = CharacterCatalog.id("half_saiyan");
    private static final Identifier HUMAN = CharacterCatalog.id("human");
    private static final Identifier NAMEKIAN = CharacterCatalog.id("namekian");
    private static final Identifier ARCOSIAN = CharacterCatalog.id("arcosian");
    private static final Identifier MAJIN = CharacterCatalog.id("majin");

    private static Form form(String name) {
        return Form.of(name);
    }

    private static SkillData skill(Identifier id, int level) {
        return new SkillData(Map.of(id, level));
    }

    private static DataResult<FormDef> parse(String json) {
        return FormDef.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
    }

    // ---------------------------------------------------------------- shipped data
    @Test
    @DisplayName("the mod ships 15 forms, all valid")
    void shipsForms() {
        assertEquals(15, CATALOG.forms().size());
        assertFalse(CATALOG.has(Form.BASE.id()), "Base is implicit, not data");
    }

    @Test
    @DisplayName("Saiyan multipliers follow the modern config of the research (150..400%)")
    void saiyanMultipliers() {
        double[][] expected = {
            {1.5}, {2.0}, {2.5}, {3.0}, {3.5}, {4.0},
        };
        String[] names = {"super_saiyan", "super_saiyan_grade_2", "super_saiyan_grade_3",
            "super_saiyan_2", "super_saiyan_3", "super_saiyan_4"};
        for (int i = 0; i < names.length; i++) {
            FormDef def = CATALOG.get(form(names[i]).id());
            assertEquals(expected[i][0], def.strMult(), 1e-9, names[i]);
            assertEquals(expected[i][0], def.dexMult(), 1e-9, names[i]);
            assertEquals(expected[i][0], def.wilMult(), 1e-9, names[i]);
            assertEquals(expected[i][0], def.damageTakenDivisor(), 1e-9, names[i] + " divides damage by the multiplier");
        }
    }

    @Test
    @DisplayName("other races' forms match the research (Human/Namekian 150-200, Arcosian 200-250, Evil Majin 2.2)")
    void otherRaces() {
        assertEquals(1.5, CATALOG.get(form("human_full_released").id()).strMult(), 1e-9);
        assertEquals(2.0, CATALOG.get(form("human_buffed").id()).strMult(), 1e-9);
        assertEquals(1.5, CATALOG.get(form("namekian_full_released").id()).strMult(), 1e-9);
        assertEquals(2.0, CATALOG.get(form("giant_form").id()).strMult(), 1e-9);
        assertEquals(2.0, CATALOG.get(form("fifth_form").id()).strMult(), 1e-9);
        assertEquals(2.5, CATALOG.get(form("golden_form").id()).strMult(), 1e-9);
        assertEquals(2.2, CATALOG.get(form("evil_majin").id()).strMult(), 1e-9);
    }

    @Test
    @DisplayName("each form is unlocked by the racial skill levels of the research")
    void unlockLevels() {
        Object[][] expected = {
            {"super_saiyan", SkillCatalog.SUPER_FORM, 1}, {"super_saiyan_grade_2", SkillCatalog.SUPER_FORM, 2},
            {"super_saiyan_grade_3", SkillCatalog.SUPER_FORM, 3}, {"super_saiyan_2", SkillCatalog.SUPER_FORM, 5},
            {"super_saiyan_3", SkillCatalog.SUPER_FORM, 6}, {"super_saiyan_4", SkillCatalog.SUPER_FORM, 7},
            {"fifth_form", SkillCatalog.TRANSFORMATIONS, 3}, {"golden_form", SkillCatalog.TRANSFORMATIONS, 6},
            {"evil_majin", SkillCatalog.ABILITIES, 2}, {"super_majin", SkillCatalog.ABILITIES, 3},
            {"pure_majin", SkillCatalog.ABILITIES, 5},
        };
        for (Object[] row : expected) {
            FormDef def = CATALOG.get(form((String) row[0]).id());
            assertEquals(row[1], def.skill(), row[0] + " skill");
            assertEquals((Integer) row[2], def.skillLevel(), row[0] + " level");
        }
    }

    @Test
    @DisplayName("every form's racial skill exists, and only its races can learn it")
    void racialSkillsExistAndMatch() {
        SkillCatalog skills = SkillCatalog.builtin();
        for (Map.Entry<Identifier, FormDef> e : CATALOG.forms().entrySet()) {
            FormDef def = e.getValue();
            SkillDef skillDef = skills.get(def.skill());
            assertNotNull(skillDef, e.getKey() + " needs " + def.skill());
            assertTrue(def.skillLevel() <= skillDef.maxLevel(), e.getKey() + " level within the skill's max");
            for (Identifier race : def.races()) {
                assertTrue(skillDef.allowsRace(race), e.getKey() + ": " + race + " can learn " + def.skill());
            }
        }
    }

    // -------------------------------------------------------------------- JSON
    @Test
    @DisplayName("a minimal form parses with neutral defaults")
    void minimal() {
        FormDef def = parse("{}").getOrThrow();
        assertTrue(def.races().isEmpty());
        assertEquals(1, def.order());
        assertNull(def.skill());
        assertEquals(1.0, def.strMult(), 1e-9);
        assertEquals(1.0, def.damageTakenDivisor(), 1e-9);
        assertEquals(0.0, def.kiPerSecond(), 1e-9);
        assertTrue(def.allowsRace(HUMAN), "no races listed means every race");
    }

    @Test
    @DisplayName("invalid forms are rejected")
    void invalid() {
        assertTrue(parse("{\"order\":0}").error().isPresent());
        assertTrue(parse("{\"multipliers\":{\"str\":-1}}").error().isPresent());
        assertTrue(parse("{\"damage_taken_divisor\":0}").error().isPresent());
        assertTrue(parse("{\"ki_per_second\":-2}").error().isPresent());
        assertTrue(parse("{\"min_release\":-1}").error().isPresent());
        assertTrue(parse("{\"requires\":{\"skill\":\"kimon:fly\",\"level\":0}}").error().isPresent());
        assertTrue(parse("{\"requires\":{}}").error().isPresent(), "requirement without a skill");
        assertTrue(parse("[]").error().isPresent());
    }

    @Test
    @DisplayName("a form survives an encode/decode round trip")
    void roundTrip() {
        FormDef original = CATALOG.get(form("super_saiyan_3").id());
        var json = FormDef.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        assertEquals(original, FormDef.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    @DisplayName("the form catalog survives the wire exactly")
    void payloadRoundTrip() {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        FormCatalogPayload.STREAM_CODEC.encode(buf, new FormCatalogPayload(CATALOG));
        FormCatalog decoded = FormCatalogPayload.STREAM_CODEC.decode(buf).catalog();
        assertEquals(CATALOG.forms(), decoded.forms());
        assertEquals(0, buf.readableBytes());
    }

    // ------------------------------------------------------------------ pipeline
    @Test
    @DisplayName("effective = max(value * multiplier, value + flat): the flat bonus helps low levels")
    void effectivePipeline() {
        assertEquals(15.0, FormEffect.effective(10, 1.5, 0), 1e-9);
        assertEquals(20.0, FormEffect.effective(10, 1.5, 10), 1e-9, "flat wins at low values");
        assertEquals(1500.0, FormEffect.effective(1000, 1.5, 10), 1e-9, "the multiplier wins at high values");
        assertEquals(10.0, FormEffect.effective(10, 1.0, 0), 1e-9, "no form changes nothing");
    }

    @Test
    @DisplayName("Base is neutral; a form scales STR, DEX and WIL; mastery adds to the multipliers")
    void formEffect() {
        FormEffect none = FormEffect.of(FormDef.BASE, 0.5);
        assertEquals(50.0, none.strength(50), 1e-9);
        assertEquals(50.0, none.dexterity(50), 1e-9);
        assertEquals(50.0, none.willpower(50), 1e-9);

        FormDef ssj = CATALOG.get(Form.SUPER_SAIYAN.id());
        FormEffect fe = FormEffect.of(ssj, 0.0);
        assertEquals(150.0, fe.strength(100), 1e-9);
        FormEffect mastered = FormEffect.of(ssj, 0.25);
        assertEquals(175.0, mastered.strength(100), 1e-9, "1.5 + 0.25 mastery");
        assertEquals(175.0, mastered.willpower(100), 1e-9);
    }

    // ---------------------------------------------------------------------- ladder
    @Test
    @DisplayName("each race sees only its own forms, in order")
    void ladders() {
        List<Form> saiyan = FormRules.ladder(CATALOG, SAIYAN);
        assertEquals(List.of(form("super_saiyan"), form("super_saiyan_grade_2"), form("super_saiyan_grade_3"),
                form("super_saiyan_2"), form("super_saiyan_3"), form("super_saiyan_4")), saiyan);
        assertEquals(saiyan, FormRules.ladder(CATALOG, HALF), "Half-Saiyans share the Saiyan line");
        assertEquals(List.of(form("human_full_released"), form("human_buffed")), FormRules.ladder(CATALOG, HUMAN));
        assertEquals(List.of(form("namekian_full_released"), form("giant_form")), FormRules.ladder(CATALOG, NAMEKIAN));
        assertEquals(List.of(form("fifth_form"), form("golden_form")), FormRules.ladder(CATALOG, ARCOSIAN));
        assertEquals(List.of(form("evil_majin"), form("super_majin"), form("pure_majin")), FormRules.ladder(CATALOG, MAJIN));
        assertTrue(FormRules.ladder(CATALOG, Identifier.fromNamespaceAndPath("mypack", "orc")).isEmpty());
    }

    @Test
    @DisplayName("a Human can no longer reach Super Saiyan")
    void humanHasNoSuperSaiyan() {
        assertFalse(FormRules.ladder(CATALOG, HUMAN).contains(Form.SUPER_SAIYAN));
        assertEquals(FormRules.Check.WRONG_RACE,
                FormRules.canEnter(CATALOG, HUMAN, skill(SkillCatalog.SUPER_FORM, 7), Form.SUPER_SAIYAN, 100));
        assertFalse(FormRules.validFor(CATALOG, HUMAN, Form.SUPER_SAIYAN));
        assertTrue(FormRules.validFor(CATALOG, SAIYAN, Form.SUPER_SAIYAN));
        assertTrue(FormRules.validFor(CATALOG, HUMAN, Form.BASE));
    }

    @Test
    @DisplayName("next climbs the ladder from Base; previous steps down to Base")
    void nextAndPrevious() {
        List<Form> ladder = FormRules.ladder(CATALOG, SAIYAN);
        assertEquals(form("super_saiyan"), FormRules.next(ladder, Form.BASE));
        assertEquals(form("super_saiyan_grade_2"), FormRules.next(ladder, form("super_saiyan")));
        assertNull(FormRules.next(ladder, form("super_saiyan_4")), "already at the top");
        assertEquals(Form.BASE, FormRules.previous(ladder, form("super_saiyan")));
        assertEquals(form("super_saiyan"), FormRules.previous(ladder, form("super_saiyan_grade_2")));
        assertEquals(Form.BASE, FormRules.previous(ladder, Form.BASE));
        assertEquals(form("super_saiyan"), FormRules.next(ladder, form("fifth_form")),
                "a form not on the ladder counts as Base");
        assertNull(FormRules.next(List.of(), Form.BASE), "a race with no forms never transforms");
    }

    @Test
    @DisplayName("canEnter checks race, then skill level, then Release")
    void canEnter() {
        Form ssj = Form.SUPER_SAIYAN;
        assertEquals(FormRules.Check.SKILL_LOCKED,
                FormRules.canEnter(CATALOG, SAIYAN, SkillData.EMPTY, ssj, 100));
        assertEquals(FormRules.Check.NEEDS_RELEASE,
                FormRules.canEnter(CATALOG, SAIYAN, skill(SkillCatalog.SUPER_FORM, 1), ssj, 9.9));
        assertEquals(FormRules.Check.OK,
                FormRules.canEnter(CATALOG, SAIYAN, skill(SkillCatalog.SUPER_FORM, 1), ssj, 10.0));
        assertEquals(FormRules.Check.SKILL_LOCKED,
                FormRules.canEnter(CATALOG, SAIYAN, skill(SkillCatalog.SUPER_FORM, 4), form("super_saiyan_2"), 100),
                "level 4 does not open Super Saiyan 2 (level 5)");
        assertEquals(FormRules.Check.OK,
                FormRules.canEnter(CATALOG, SAIYAN, skill(SkillCatalog.SUPER_FORM, 5), form("super_saiyan_2"), 100));
        assertEquals(FormRules.Check.NO_SUCH_FORM,
                FormRules.canEnter(CATALOG, SAIYAN, SkillData.EMPTY, form("nope"), 100));
        assertEquals(FormRules.Check.OK, FormRules.canEnter(CATALOG, SAIYAN, SkillData.EMPTY, Form.BASE, 0));
    }

    @Test
    @DisplayName("highestAvailable is the best form the skill level and Release allow")
    void highestAvailable() {
        assertEquals(Form.BASE, FormRules.highestAvailable(CATALOG, SAIYAN, SkillData.EMPTY, 100));
        assertEquals(form("super_saiyan"),
                FormRules.highestAvailable(CATALOG, SAIYAN, skill(SkillCatalog.SUPER_FORM, 1), 100));
        assertEquals(form("super_saiyan_grade_3"),
                FormRules.highestAvailable(CATALOG, SAIYAN, skill(SkillCatalog.SUPER_FORM, 4), 100),
                "level 4 reaches Grade 3; level 5 would add SSJ2");
        assertEquals(form("super_saiyan"),
                FormRules.highestAvailable(CATALOG, SAIYAN, skill(SkillCatalog.SUPER_FORM, 7), 12),
                "low Release holds you back");
        assertEquals(form("super_saiyan_4"),
                FormRules.highestAvailable(CATALOG, SAIYAN, skill(SkillCatalog.SUPER_FORM, 7), 100));
    }

    // ------------------------------------------------------------- racial skills
    @Test
    @DisplayName("racial skills can only be learned by their races")
    void racialSkillsAreRaceLocked() {
        SkillCatalog skills = SkillCatalog.builtin();
        SkillRules.Result saiyan = SkillRules.learn(SkillData.EMPTY, skills, SkillCatalog.SUPER_FORM, 10_000, 100, SAIYAN);
        assertTrue(saiyan.learned());
        SkillRules.Result half = SkillRules.learn(SkillData.EMPTY, skills, SkillCatalog.SUPER_FORM, 10_000, 100, HALF);
        assertTrue(half.learned());
        SkillRules.Result human = SkillRules.learn(SkillData.EMPTY, skills, SkillCatalog.SUPER_FORM, 10_000, 100, HUMAN);
        assertEquals(SkillRules.Outcome.WRONG_RACE, human.outcome());
        assertEquals(0L, human.tpSpent());
        assertEquals(SkillData.EMPTY, human.data());
        assertTrue(SkillRules.learn(SkillData.EMPTY, skills, SkillCatalog.HUMAN_POTENTIAL, 10_000, 100, HUMAN).learned());
        assertEquals(SkillRules.Outcome.WRONG_RACE,
                SkillRules.learn(SkillData.EMPTY, skills, SkillCatalog.HUMAN_POTENTIAL, 10_000, 100, MAJIN).outcome());
        assertTrue(SkillRules.learn(SkillData.EMPTY, skills, SkillCatalog.JUMP, 10_000, 100, MAJIN).learned(),
                "ordinary skills are open to every race");
    }

    @Test
    @DisplayName("racial skills have the max levels of the research (Saiyan 7, Human 5, Namekian 5, Arcosian 6, Majin 5)")
    void racialMaxLevels() {
        SkillCatalog skills = SkillCatalog.builtin();
        assertEquals(7, skills.get(SkillCatalog.SUPER_FORM).maxLevel());
        assertEquals(5, skills.get(SkillCatalog.HUMAN_POTENTIAL).maxLevel());
        assertEquals(5, skills.get(SkillCatalog.POWER_BOOST).maxLevel());
        assertEquals(6, skills.get(SkillCatalog.TRANSFORMATIONS).maxLevel());
        assertEquals(5, skills.get(SkillCatalog.ABILITIES).maxLevel());
    }

    // --------------------------------------------------------------------- names
    @Test
    @DisplayName("form names: full ids, bare names, spaces and the old names all resolve")
    void names() {
        assertEquals(Form.SUPER_SAIYAN_3, Form.byKey("super_saiyan_3"));
        assertEquals(Form.SUPER_SAIYAN_3, Form.byKey("SUPER_SAIYAN_3"));
        assertEquals(Form.SUPER_SAIYAN_2, Form.byKey("Super Saiyan 2"));
        assertEquals(Form.SUPER_SAIYAN, Form.byKey("surge"));
        assertEquals(Form.SUPER_SAIYAN_2, Form.byKey("ASCENT"));
        assertEquals(Form.SUPER_SAIYAN_3, Form.byKey("zenith"));
        assertEquals(new Form(Identifier.fromNamespaceAndPath("mypack", "rage")), Form.byKey("mypack:rage"));
        assertNull(Form.byKey(""));
        assertNull(Form.byKey(null));
        assertNull(Form.byKey("bad name!"));
        assertEquals(Form.BASE, Form.fromStored("???!"), "unreadable stored text is Base");
        assertTrue(Form.BASE.isBase());
        assertFalse(Form.SUPER_SAIYAN.isBase());
        assertEquals("super_saiyan", Form.SUPER_SAIYAN.key());
    }

    @Test
    @DisplayName("unknown forms behave like Base instead of failing")
    void unknownIsNeutral() {
        Form ghost = new Form(Identifier.fromNamespaceAndPath("gone", "form"));
        assertEquals(FormDef.BASE, ghost.def());
        assertEquals(0.0, ghost.energyDrainPerSecond(), 1e-9);
        assertEquals(1.0, FormDef.BASE.damageTakenDivisor(), 1e-9);
    }

    // -------------------------------------------------------------- the Ki loop
    @Test
    @DisplayName("a form drains Ki over time and reverts to Base when Ki runs out")
    void formDrainsAndReverts() {
        PowerState s = new PowerState(60, 5, 0, false, Form.SUPER_SAIYAN_3);
        PowerState after = s.tick(0.05, 100, 1000, 100);
        assertEquals(Form.SUPER_SAIYAN_3, after.form(), "one tick of drain does not empty 5 Ki");

        PowerState almostEmpty = new PowerState(60, 0.1, 0, false, Form.SUPER_SAIYAN_3);
        PowerState reverted = almostEmpty.tick(0.05, 100, 1000, 100);
        assertEquals(0.0, reverted.energy(), 1e-9);
        assertEquals(Form.BASE, reverted.form());
    }

    @Test
    @DisplayName("a form reverts if Release drops below its requirement")
    void formRevertsOnLowRelease() {
        PowerState s = new PowerState(8, 1000, 0, false, Form.SUPER_SAIYAN); // needs 10%
        assertEquals(Form.BASE, s.tick(0.05, 100, 1000, 100).form());
        PowerState fine = new PowerState(12, 1000, 0, false, Form.SUPER_SAIYAN);
        assertEquals(Form.SUPER_SAIYAN, fine.tick(0.05, 100, 1000, 100).form());
    }

    @Test
    @DisplayName("the drain comes from the form's data, so stronger forms cost more")
    void drainFromData() {
        assertTrue(Form.SUPER_SAIYAN_3.energyDrainPerSecond() > Form.SUPER_SAIYAN.energyDrainPerSecond());
        assertEquals(CATALOG.get(Form.SUPER_SAIYAN.id()).kiPerSecond(), Form.SUPER_SAIYAN.energyDrainPerSecond(), 1e-9);
        assertEquals(CATALOG.get(Form.SUPER_SAIYAN.id()).minRelease(), Form.SUPER_SAIYAN.requiredRelease(), 1e-9);
    }

    @Test
    @DisplayName("formMultiplier reflects the active form")
    void formMultiplier() {
        assertEquals(1.0, PowerState.INITIAL.formMultiplier(), 1e-9);
        assertEquals(Form.SUPER_SAIYAN_2.damageMultiplier(),
                PowerState.INITIAL.withForm(Form.SUPER_SAIYAN_2).formMultiplier(), 1e-9);
        assertEquals(3.0, Form.SUPER_SAIYAN_2.damageMultiplier(), 1e-9);
    }

    @Test
    @DisplayName("the form survives the state's network encoding")
    void stateCodecKeepsForm() {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        PowerState s = new PowerState(40, 100, 20, true, Form.SUPER_SAIYAN_2);
        PowerState.STREAM_CODEC.encode(buf, s);
        assertEquals(Form.SUPER_SAIYAN_2, PowerState.STREAM_CODEC.decode(buf).form());
    }

    @Test
    @DisplayName("a datapack form joins its race's ladder")
    void customForm() {
        Identifier orc = Identifier.fromNamespaceAndPath("mypack", "orc");
        Identifier rage = Identifier.fromNamespaceAndPath("mypack", "rage");
        Map<Identifier, FormDef> forms = new TreeMap<>(CATALOG.forms());
        forms.put(rage, parse("{\"races\":[\"mypack:orc\"],\"order\":1,\"multipliers\":{\"str\":2.5}}").getOrThrow());
        FormCatalog custom = new FormCatalog(forms);
        assertEquals(List.of(new Form(rage)), FormRules.ladder(custom, orc));
        assertEquals(FormRules.Check.OK, FormRules.canEnter(custom, orc, SkillData.EMPTY, new Form(rage), 0),
                "no skill and no Release requirement");
    }
}
