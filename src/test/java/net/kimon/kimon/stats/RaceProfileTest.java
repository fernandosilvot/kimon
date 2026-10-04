package net.kimon.kimon.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Race / PlayerClass / CharacterProfile")
class RaceProfileTest {

    @Test
    @DisplayName("every race seeds a stat block with its starting spread and zero TP")
    void raceSeedsStatBlock() {
        for (Race race : Race.VALUES) {
            StatBlock block = race.newStatBlock();
            assertEquals(0L, block.trainingPoints());
            for (Attribute a : Attribute.VALUES) {
                assertEquals(race.startingSpread().get(a), block.get(a),
                        race.key() + " " + a.key());
            }
        }
    }

    @Test
    @DisplayName("race lookup by key is case-insensitive; unknown returns null")
    void raceLookup() {
        assertEquals(Race.TITAN, Race.byKey("titan"));
        assertEquals(Race.TITAN, Race.byKey("TITAN"));
        assertNull(Race.byKey("nonsense"));
    }

    @Test
    @DisplayName("class lookup by key works")
    void classLookup() {
        assertEquals(PlayerClass.CHANNELER, PlayerClass.byKey("channeler"));
        assertNull(PlayerClass.byKey("nope"));
    }

    @Test
    @DisplayName("profile combines race + class modifiers additively")
    void profileCombinesModifiers() {
        CharacterProfile profile = new CharacterProfile(Race.TITAN, PlayerClass.WARRIOR);
        double expected = Race.TITAN.modifier(Attribute.STRENGTH)
                + PlayerClass.WARRIOR.modifier(Attribute.STRENGTH);
        assertEquals(expected, profile.totalModifier(Attribute.STRENGTH), 1e-9);
    }

    @Test
    @DisplayName("Titan is the strongest melee race, Sage the strongest energy race")
    void raceIdentities() {
        assertTrue(Race.TITAN.modifier(Attribute.STRENGTH) > Race.HUMAN.modifier(Attribute.STRENGTH));
        assertTrue(Race.SAGE.modifier(Attribute.ENERGY) >= Race.TITAN.modifier(Attribute.ENERGY));
    }

    @Test
    @DisplayName("withRace / withClass return updated profiles")
    void profileMutators() {
        CharacterProfile base = CharacterProfile.DEFAULT;
        assertEquals(Race.FROST, base.withRace(Race.FROST).race());
        assertEquals(PlayerClass.WARRIOR, base.withClass(PlayerClass.WARRIOR).clazz());
    }

    @Test
    @DisplayName("default profile is Human / Brawler")
    void defaultProfile() {
        assertEquals(Race.HUMAN, CharacterProfile.DEFAULT.race());
        assertEquals(PlayerClass.BRAWLER, CharacterProfile.DEFAULT.clazz());
        assertNotNull(CharacterProfile.DEFAULT);
    }

    @Test
    @DisplayName("race modifier raises derived stats via StatCalculator")
    void modifierAffectsDerivedStats() {
        // A Titan (+30% STR) should get more melee bonus than a Human for the same strength.
        StatBlock stats = StatBlock.of(java.util.Map.of(Attribute.STRENGTH, StatBlock.START_VALUE + 100), 0);
        CharacterProfile human = new CharacterProfile(Race.HUMAN, PlayerClass.BRAWLER);
        CharacterProfile titan = new CharacterProfile(Race.TITAN, PlayerClass.WARRIOR);
        assertTrue(StatCalculator.bonusAttackDamage(stats, titan)
                > StatCalculator.bonusAttackDamage(stats, human));
    }
}
