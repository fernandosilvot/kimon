package net.kimon.kimon.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Level, attribute cost curve and bulk purchase")
class ProgressionTest {

    private static final CostParams P = CostParams.DEFAULTS;

    private static StatBlock human() {
        return CharacterCatalog.builtin().race(CharacterCatalog.DEFAULT_RACE).newStatBlock();
    }

    // ---------------------------------------------------------------- level
    @Test
    @DisplayName("a fresh 60-point character is level 1")
    void startsAtLevelOne() {
        for (var e : CharacterCatalog.builtin().races().entrySet()) {
            assertEquals(1, LevelCalculator.level(e.getValue().newStatBlock()), e.getKey().toString());
        }
        assertEquals(1, LevelCalculator.level(StatBlock.initial()), "below the baseline clamps to 1");
    }

    @Test
    @DisplayName("every 5 attribute points above 55 is one level")
    void fivePointsPerLevel() {
        assertEquals(1, LevelCalculator.level(60));
        assertEquals(1, LevelCalculator.level(64));
        assertEquals(2, LevelCalculator.level(65));
        assertEquals(3, LevelCalculator.level(70));
        assertEquals(101, LevelCalculator.level(560));
    }

    @Test
    @DisplayName("pointsToNextLevel counts down to the next threshold")
    void pointsToNext() {
        StatBlock human = human(); // 60 points → level 1, next at 65
        assertEquals(5, LevelCalculator.pointsToNextLevel(human));
        StatBlock raised = human.addTrainingPoints(1000).raise(Attribute.STRENGTH, P);
        assertEquals(4, LevelCalculator.pointsToNextLevel(raised));
    }

    @Test
    @DisplayName("buying attribute points raises the level")
    void buyingRaisesLevel() {
        StatBlock rich = human().addTrainingPoints(1_000_000);
        StatBlock after = rich.raiseMany(Attribute.STRENGTH, 50, P).block();
        assertTrue(LevelCalculator.level(after) > LevelCalculator.level(rich));
    }

    // ----------------------------------------------------------------- cost
    @Test
    @DisplayName("the cost never falls below the minimum and rises with the level")
    void costCurve() {
        assertEquals(P.minCost(), P.costAt(0));
        long previous = 0;
        for (int level = 0; level <= 10_000; level += 50) {
            long cost = P.costAt(level);
            assertTrue(cost >= P.minCost());
            assertTrue(cost >= previous, "monotonic at level " + level);
            previous = cost;
        }
        assertTrue(P.costAt(1000) > P.costAt(100) * 5, "the quadratic term makes high levels climb steeply");
    }

    @Test
    @DisplayName("startMinus delays the climb: a bigger divisor means cheaper high levels")
    void startMinusDelaysClimb() {
        CostParams late = new CostParams(1.0, 0.5, 0.75, 14_000.0, 1L);
        assertTrue(late.costAt(1000) < P.costAt(1000));
    }

    @Test
    @DisplayName("params are honoured: min cost floors, multiplier 0 flattens the curve")
    void paramsHonoured() {
        assertEquals(50L, new CostParams(1.0, 0.5, 0.75, 140.0, 50L).costAt(0));
        CostParams flat = new CostParams(3.0, 0.5, 0.0, 140.0, 1L);
        assertEquals(3L, flat.costAt(0));
        assertEquals(3L, flat.costAt(5000));
    }

    // ------------------------------------------------------------- raiseMany
    @Test
    @DisplayName("raiseMany spends exactly the sum of the rising prices")
    void raiseManySpendsSum() {
        StatBlock start = human().addTrainingPoints(100_000);
        StatBlock.RaiseResult result = start.raiseMany(Attribute.STRENGTH, 10, P);
        assertEquals(10, result.raised());

        long expected = 0;
        int level = start.get(Attribute.STRENGTH);
        for (int i = 0; i < 10; i++) {
            expected += P.costAt(level + i);
        }
        assertEquals(expected, result.spent());
        assertEquals(start.trainingPoints() - expected, result.block().trainingPoints());
        assertEquals(level + 10, result.block().get(Attribute.STRENGTH));
    }

    @Test
    @DisplayName("raiseMany stops at the first unaffordable point and never overspends")
    void raiseManyStopsWhenBroke() {
        StatBlock poor = human().addTrainingPoints(20);
        StatBlock.RaiseResult result = poor.raiseMany(Attribute.STRENGTH, 1000, P);
        assertTrue(result.raised() > 0 && result.raised() < 1000);
        assertTrue(result.block().trainingPoints() >= 0);
        assertEquals(20 - result.block().trainingPoints(), result.spent());
        assertTrue(result.block().trainingPoints() < result.block().costToRaise(Attribute.STRENGTH, P),
                "it stopped because the next point is unaffordable");
    }

    @Test
    @DisplayName("raiseMany with no TP or a non-positive count changes nothing")
    void raiseManyNoOp() {
        StatBlock none = human();
        assertSame(none, none.raiseMany(Attribute.STRENGTH, 10, P).block());
        StatBlock rich = none.addTrainingPoints(1000);
        assertEquals(0, rich.raiseMany(Attribute.STRENGTH, 0, P).raised());
        assertEquals(0, rich.raiseMany(Attribute.STRENGTH, -5, P).raised());
    }

    @Test
    @DisplayName("bulk buying equals buying one at a time")
    void bulkEqualsSingles() {
        StatBlock start = human().addTrainingPoints(5000);
        StatBlock singles = start;
        for (int i = 0; i < 25; i++) {
            singles = singles.raise(Attribute.SPIRIT, P);
        }
        assertEquals(singles, start.raiseMany(Attribute.SPIRIT, 25, P).block());
    }

    @Test
    @DisplayName("an attribute never goes past its maximum")
    void maxValue() {
        StatBlock near = StatBlock.of(java.util.Map.of(Attribute.STRENGTH, StatBlock.MAX_VALUE - 2), 1_000_000_000L);
        StatBlock.RaiseResult result = near.raiseMany(Attribute.STRENGTH, 1000, new CostParams(1, 0, 0, 1, 1));
        assertEquals(2, result.raised());
        assertEquals(StatBlock.MAX_VALUE, result.block().get(Attribute.STRENGTH));
    }

    // ----------------------------------------------------------- TP chance
    @Test
    @DisplayName("a chance multiplier makes TP likelier, capped at certainty")
    void tpChanceMultiplier() {
        TpParams p = TpParams.DEFAULTS; // 20% chance
        double roll = 0.35;
        assertEquals(0L, TpGain.forHit(10, 50, roll, p));
        assertTrue(TpGain.forHit(10, 50, roll, p, 2.0) > 0, "40% chance catches a 0.35 roll");
        assertTrue(TpGain.forHit(10, 50, 0.99, p, 100.0) > 0, "chance is capped at 1, so 0.99 still hits");
        assertEquals(0L, TpGain.forHit(10, 2, 0.0, p, 100.0), "still needs the minimum Release");
        assertEquals(0L, TpGain.forHit(10, 50, 0.0, p, 0.0), "multiplier 0 never grants");
    }

    @Test
    @DisplayName("attribute names: full keys, doc abbreviations and old names all resolve")
    void attributeAliases() {
        assertEquals(Attribute.DEXTERITY, Attribute.byKey("dexterity"));
        assertEquals(Attribute.DEXTERITY, Attribute.byKey("DEX"));
        assertEquals(Attribute.DEXTERITY, Attribute.byKey("agility"));
        assertEquals(Attribute.CONSTITUTION, Attribute.byKey("con"));
        assertEquals(Attribute.WILLPOWER, Attribute.byKey("wil"));
        assertEquals(Attribute.WILLPOWER, Attribute.byKey("energy"));
        assertEquals(Attribute.MIND, Attribute.byKey("mnd"));
        assertEquals(Attribute.MIND, Attribute.byKey(" Focus "));
        assertEquals(Attribute.STRENGTH, Attribute.byKey("str"));
        assertEquals(Attribute.SPIRIT, Attribute.byKey("spi"));
        assertEquals(null, Attribute.byKey("luck"));
        assertEquals(null, Attribute.byKey(null));
        for (Attribute a : Attribute.VALUES) {
            assertEquals(a, Attribute.byKey(a.key()), "every key resolves to itself");
        }
    }
}
