package net.kimon.kimon.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StatBlock TP economy")
class StatBlockTest {

    @Test
    @DisplayName("initial block has all attributes at START_VALUE and zero TP")
    void initialState() {
        StatBlock block = StatBlock.initial();
        for (Attribute a : Attribute.VALUES) {
            assertEquals(StatBlock.START_VALUE, block.get(a));
        }
        assertEquals(0L, block.trainingPoints());
    }

    @Test
    @DisplayName("cost rises with attribute level")
    void costRises() {
        assertEquals(StatBlock.BASE_COST, StatBlock.costAt(0));
        assertTrue(StatBlock.costAt(100) > StatBlock.costAt(10));
        assertTrue(StatBlock.costAt(1000) > StatBlock.costAt(100));
    }

    @Test
    @DisplayName("cannot raise without enough TP")
    void cannotAfford() {
        StatBlock block = StatBlock.initial(); // 0 TP
        assertFalse(block.canRaise(Attribute.STRENGTH));
        assertSame(block, block.raise(Attribute.STRENGTH), "raise with no TP returns same block");
    }

    @Test
    @DisplayName("raising deducts the exact cost and increments the attribute")
    void raiseDeductsCost() {
        StatBlock block = StatBlock.initial().addTrainingPoints(1000);
        long cost = block.costToRaise(Attribute.STRENGTH);
        StatBlock raised = block.raise(Attribute.STRENGTH);

        assertEquals(StatBlock.START_VALUE + 1, raised.get(Attribute.STRENGTH));
        assertEquals(1000 - cost, raised.trainingPoints());
    }

    @Test
    @DisplayName("raising is immutable: original is unchanged")
    void raiseIsImmutable() {
        StatBlock block = StatBlock.initial().addTrainingPoints(1000);
        block.raise(Attribute.STRENGTH);
        assertEquals(StatBlock.START_VALUE, block.get(Attribute.STRENGTH));
        assertEquals(1000L, block.trainingPoints());
    }

    @Test
    @DisplayName("only the targeted attribute changes")
    void onlyTargetChanges() {
        StatBlock raised = StatBlock.initial().addTrainingPoints(1000).raise(Attribute.ENERGY);
        for (Attribute a : Attribute.VALUES) {
            int expected = a == Attribute.ENERGY ? StatBlock.START_VALUE + 1 : StatBlock.START_VALUE;
            assertEquals(expected, raised.get(a));
        }
    }

    @Test
    @DisplayName("repeated raises cost progressively more TP")
    void progressiveCost() {
        StatBlock block = StatBlock.initial().addTrainingPoints(1_000_000);
        long firstCost = block.costToRaise(Attribute.STRENGTH);
        StatBlock afterOne = block.raise(Attribute.STRENGTH);
        long secondCost = afterOne.costToRaise(Attribute.STRENGTH);
        assertTrue(secondCost >= firstCost);
    }

    @Test
    @DisplayName("addTrainingPoints accumulates and clamps at zero")
    void addTp() {
        StatBlock block = StatBlock.initial().addTrainingPoints(50);
        assertEquals(50L, block.trainingPoints());
        assertEquals(0L, block.addTrainingPoints(-100).trainingPoints());
    }

    @Test
    @DisplayName("of() round-trips values and defaults missing attributes")
    void ofRoundTrips() {
        Map<Attribute, Integer> vals = new EnumMap<>(Attribute.class);
        vals.put(Attribute.STRENGTH, 42);
        StatBlock block = StatBlock.of(vals, 99);

        assertEquals(42, block.get(Attribute.STRENGTH));
        assertEquals(StatBlock.START_VALUE, block.get(Attribute.VITALITY)); // defaulted
        assertEquals(99L, block.trainingPoints());
    }

    @Test
    @DisplayName("of() clamps values into range")
    void ofClamps() {
        Map<Attribute, Integer> vals = new EnumMap<>(Attribute.class);
        vals.put(Attribute.STRENGTH, -10);
        vals.put(Attribute.ENERGY, StatBlock.MAX_VALUE + 500);
        StatBlock block = StatBlock.of(vals, 0);

        assertEquals(0, block.get(Attribute.STRENGTH));
        assertEquals(StatBlock.MAX_VALUE, block.get(Attribute.ENERGY));
    }

    @Test
    @DisplayName("equal blocks are equal and hash equally")
    void equalityContract() {
        StatBlock a = StatBlock.initial().addTrainingPoints(10).raise(Attribute.FOCUS);
        StatBlock b = StatBlock.initial().addTrainingPoints(10).raise(Attribute.FOCUS);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
