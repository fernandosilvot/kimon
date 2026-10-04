package net.kimon.kimon.training;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Training weights and gravity")
class TrainingEffectsTest {

    private static final TrainingParams P = TrainingParams.DEFAULTS;

    @Test
    @DisplayName("no load changes nothing")
    void noLoad() {
        TrainingLoad none = TrainingLoad.NONE;
        assertTrue(none.isNone());
        assertEquals(1.0, TrainingEffects.damageFactor(none, P), 1e-9);
        assertEquals(1.0, TrainingEffects.statFactor(none, P), 1e-9);
        assertEquals(1.0, TrainingEffects.tpChanceMultiplier(none, P), 1e-9);
        assertEquals(0.0, TrainingEffects.gravityAttributeModifier(none, P), 1e-9);
    }

    @Test
    @DisplayName("weight is multiplied by gravity (10 weight under 10G = 100)")
    void effectiveWeight() {
        assertEquals(100.0, new TrainingLoad(10, 10).effectiveWeight(), 1e-9);
        assertEquals(10.0, new TrainingLoad(10, 1).effectiveWeight(), 1e-9);
    }

    @Test
    @DisplayName("weight lowers melee damage, never past the cap")
    void weightLowersDamage() {
        double light = TrainingEffects.damageFactor(new TrainingLoad(10, 1), P);
        double heavy = TrainingEffects.damageFactor(new TrainingLoad(100, 1), P);
        assertTrue(light < 1.0 && heavy < light);
        assertEquals(1.0 - P.maxWeightPenalty(),
                TrainingEffects.damageFactor(new TrainingLoad(100_000, 1), P), 1e-9);
    }

    @Test
    @DisplayName("gravity lowers STR/DEX, more as it grows, but never to zero")
    void gravityLowersStats() {
        double g2 = TrainingEffects.statFactor(new TrainingLoad(0, 2), P);
        double g10 = TrainingEffects.statFactor(new TrainingLoad(0, 10), P);
        assertTrue(g2 < 1.0 && g10 < g2 && g10 > 0.0);
    }

    @Test
    @DisplayName("weights and gravity make TP likelier, up to the cap")
    void tpBonus() {
        double w = TrainingEffects.tpChanceMultiplier(new TrainingLoad(50, 1), P);
        double g = TrainingEffects.tpChanceMultiplier(new TrainingLoad(0, 10), P);
        double both = TrainingEffects.tpChanceMultiplier(new TrainingLoad(50, 10), P);
        assertTrue(w > 1.0 && g > 1.0);
        assertTrue(both >= Math.max(w, g));
        assertEquals(P.maxTpMultiplier(),
                TrainingEffects.tpChanceMultiplier(new TrainingLoad(200, 1000), P), 1e-9);
    }

    @Test
    @DisplayName("the gravity attribute modifier follows (G-1) * factor")
    void gravityAttribute() {
        assertEquals(0.9, TrainingEffects.gravityAttributeModifier(new TrainingLoad(0, 10), P), 1e-9);
    }

    @Test
    @DisplayName("carried weight sums stacks and is capped")
    void totalWeight() {
        assertEquals(35.0, TrainingEffects.totalWeight(new double[] {10, 25, 0}, new int[] {1, 1, 64}, P), 1e-9);
        assertEquals(P.maxWeight(),
                TrainingEffects.totalWeight(new double[] {50}, new int[] {1000}, P), 1e-9);
        assertEquals(0.0, TrainingEffects.totalWeight(new double[] {-5}, new int[] {3}, P), 1e-9);
    }

    @Test
    @DisplayName("gravity is the device's value inside its field, 1G outside; devices don't stack")
    void gravityFor() {
        assertEquals(P.deviceGravity(), TrainingEffects.gravityFor(true, P), 1e-9);
        assertEquals(1.0, TrainingEffects.gravityFor(false, P), 1e-9);
    }

    @Test
    @DisplayName("TrainingLoad clamps nonsense values")
    void loadClamps() {
        TrainingLoad bad = new TrainingLoad(-4, 0.2);
        assertEquals(0.0, bad.weight(), 1e-9);
        assertEquals(1.0, bad.gravity(), 1e-9);
        assertFalse(new TrainingLoad(5, 1).isNone());
        assertFalse(new TrainingLoad(0, 2).isNone());
    }
}
