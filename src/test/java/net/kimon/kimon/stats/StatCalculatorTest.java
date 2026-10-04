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
        StatBlock atStart = withAttribute(Attribute.VITALITY, StatBlock.START_VALUE);
        assertEquals(0.0, StatCalculator.bonusHealth(atStart), 1e-9);

        StatBlock plusTen = withAttribute(Attribute.VITALITY, StatBlock.START_VALUE + 10);
        assertEquals(10 * StatCalculator.HEALTH_PER_VITALITY, StatCalculator.bonusHealth(plusTen), 1e-9);
    }

    @Test
    @DisplayName("strength drives attack damage, vitality drives health, agility drives speed")
    void eachAttributeDrivesItsStat() {
        StatBlock strong = withAttribute(Attribute.STRENGTH, StatBlock.START_VALUE + 50);
        assertEquals(50 * StatCalculator.DAMAGE_PER_STRENGTH, StatCalculator.bonusAttackDamage(strong), 1e-9);
        assertEquals(0.0, StatCalculator.bonusHealth(strong), 1e-9);

        StatBlock agile = withAttribute(Attribute.AGILITY, StatBlock.START_VALUE + 25);
        assertEquals(25 * StatCalculator.SPEED_PER_AGILITY, StatCalculator.bonusMovementSpeed(agile), 1e-9);
    }

    @Test
    @DisplayName("bonuses are capped")
    void bonusesCapped() {
        StatBlock maxed = StatBlock.of(Map.of(
                Attribute.VITALITY, StatBlock.MAX_VALUE,
                Attribute.STRENGTH, StatBlock.MAX_VALUE,
                Attribute.AGILITY, StatBlock.MAX_VALUE
        ), 0);
        assertEquals(StatCalculator.MAX_BONUS_HEALTH, StatCalculator.bonusHealth(maxed), 1e-9);
        assertEquals(StatCalculator.MAX_BONUS_DAMAGE, StatCalculator.bonusAttackDamage(maxed), 1e-9);
        assertEquals(StatCalculator.MAX_BONUS_SPEED, StatCalculator.bonusMovementSpeed(maxed), 1e-9);
    }

    @Test
    @DisplayName("bonuses never go negative")
    void neverNegative() {
        StatBlock lowered = withAttribute(Attribute.VITALITY, 0);
        assertTrue(StatCalculator.bonusHealth(lowered) >= 0.0);
    }
}
