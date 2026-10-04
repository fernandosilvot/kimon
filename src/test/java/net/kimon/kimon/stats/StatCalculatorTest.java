package net.kimon.kimon.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StatCalculator derived values")
class StatCalculatorTest {

    private static StatBlock withAttribute(Attribute attribute, int value) {
        Map<Attribute, Integer> map = new EnumMap<>(Attribute.class);
        map.put(attribute, value);
        return StatBlock.of(map, 0);
    }

    @Test
    @DisplayName("a starting character has zero bonuses (plays like vanilla)")
    void startingCharacterNoBonus() {
        StatBlock initial = StatBlock.initial();
        assertEquals(0.0, StatCalculator.bonusHealth(initial), 1e-9);
        assertEquals(0.0, StatCalculator.bonusAttackDamage(initial), 1e-9);
        assertEquals(0.0, StatCalculator.bonusMovementSpeed(initial), 1e-9);
    }

    @Test
    @DisplayName("only points above the start value contribute")
    void onlyPointsAboveStartCount() {
        StatBlock atStart = withAttribute(Attribute.CONSTITUTION, StatBlock.START_VALUE);
        assertEquals(0.0, StatCalculator.bonusHealth(atStart), 1e-9);

        StatBlock plusTen = withAttribute(Attribute.CONSTITUTION, StatBlock.START_VALUE + 10);
        assertEquals(10 * StatCalculator.HEALTH_PER_VITALITY, StatCalculator.bonusHealth(plusTen), 1e-9);
    }

    @Test
    @DisplayName("strength drives attack damage, vitality drives health, agility drives speed")
    void eachAttributeDrivesItsStat() {
        StatBlock strong = withAttribute(Attribute.STRENGTH, StatBlock.START_VALUE + 50);
        assertEquals(50 * StatCalculator.DAMAGE_PER_STRENGTH, StatCalculator.bonusAttackDamage(strong), 1e-9);
        assertEquals(0.0, StatCalculator.bonusHealth(strong), 1e-9);

        StatBlock agile = withAttribute(Attribute.DEXTERITY, StatBlock.START_VALUE + 25);
        assertEquals(25 * StatCalculator.SPEED_PER_AGILITY, StatCalculator.bonusMovementSpeed(agile), 1e-9);
    }

    @Test
    @DisplayName("bonuses are capped")
    void bonusesCapped() {
        StatBlock maxed = StatBlock.of(Map.of(
                Attribute.CONSTITUTION, StatBlock.MAX_VALUE,
                Attribute.STRENGTH, StatBlock.MAX_VALUE,
                Attribute.DEXTERITY, StatBlock.MAX_VALUE
        ), 0);
        assertEquals(StatCalculator.MAX_BONUS_HEALTH, StatCalculator.bonusHealth(maxed), 1e-9);
        assertEquals(StatCalculator.MAX_BONUS_DAMAGE, StatCalculator.bonusAttackDamage(maxed), 1e-9);
        assertEquals(StatCalculator.MAX_BONUS_SPEED, StatCalculator.bonusMovementSpeed(maxed), 1e-9);
    }

    @Test
    @DisplayName("bonuses never go negative")
    void neverNegative() {
        StatBlock lowered = withAttribute(Attribute.CONSTITUTION, 0);
        assertTrue(StatCalculator.bonusHealth(lowered) >= 0.0);
    }

    @Test
    @DisplayName("max energy scales with Spirit")
    void maxEnergyFromSpirit() {
        StatBlock s = withAttribute(Attribute.SPIRIT, 10);
        assertEquals(10 * StatCalculator.ENERGY_PER_SPIRIT, StatCalculator.maxEnergy(s), 1e-9);
    }

    @Test
    @DisplayName("max stamina scales with Vitality")
    void maxStaminaFromVitality() {
        StatBlock s = withAttribute(Attribute.CONSTITUTION, 20);
        assertEquals(20 * StatCalculator.STAMINA_PER_VITALITY, StatCalculator.maxStamina(s), 1e-9);
    }

    @Test
    @DisplayName("max Release is 50 at start and rises with Focus, capped at 100")
    void maxReleaseFromFocus() {
        assertEquals(50.0, StatCalculator.maxRelease(StatBlock.initial()), 1e-9);
        StatBlock focused = withAttribute(Attribute.MIND, StatBlock.START_VALUE + 20);
        assertEquals(70.0, StatCalculator.maxRelease(focused), 1e-9);
        StatBlock maxed = withAttribute(Attribute.MIND, StatBlock.MAX_VALUE);
        assertEquals(100.0, StatCalculator.maxRelease(maxed), 1e-9);
    }

    @Test
    @DisplayName("melee bonus is zero at 0% Release and full at 100%")
    void meleeScalesWithRelease() {
        StatBlock strong = withAttribute(Attribute.STRENGTH, StatBlock.START_VALUE + 100);
        double full = StatCalculator.bonusAttackDamage(strong);
        assertEquals(0.0, StatCalculator.meleeDamageBonus(strong, 0.0), 1e-9);
        assertEquals(full * 0.5, StatCalculator.meleeDamageBonus(strong, 0.5), 1e-9);
        assertEquals(full, StatCalculator.meleeDamageBonus(strong, 1.0), 1e-9);
    }

    @Test
    void configurableEnergyPerSpiritScalesMaxEnergy() {
        StatBlock stats = StatBlock.initial();
        assertEquals(StatCalculator.maxEnergy(stats) * 2, StatCalculator.maxEnergy(stats, 80.0), 1e-9);
    }

    @Test
    void configurableMaxReleaseRespectsBaseAndHardCap() {
        StatBlock stats = StatBlock.initial();
        assertEquals(50.0, StatCalculator.maxRelease(stats), 1e-9);
        assertEquals(30.0, StatCalculator.maxRelease(stats, 30.0, 100.0), 1e-9);
        assertEquals(100.0, StatCalculator.maxRelease(stats, 500.0, 100.0), 1e-9);
        assertEquals(200.0, StatCalculator.maxRelease(stats, 500.0, 200.0), 1e-9);
    }
}
