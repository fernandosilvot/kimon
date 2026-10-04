package net.kimon.kimon.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TP gain by hitting (no free TP)")
class TpGainTest {

    private static final TpParams P = TpParams.DEFAULTS;

    @Test
    @DisplayName("no TP below 5% Release, whatever the roll or FOCUS")
    void noTpBelowMinimumRelease() {
        assertFalse(TpGain.eligible(0));
        assertFalse(TpGain.eligible(4.99));
        assertEquals(0L, TpGain.amount(1000, 4.99, P));
        assertEquals(0L, TpGain.forHit(1000, 0, 0.0, P));
    }

    @Test
    @DisplayName("formula: 2 + 2*floor(FOCUS/5)*release/100")
    void formula() {
        // FOCUS 10 → 2 steps; at 100% → 2 + 2*2*1 = 6; at 50% → 2 + 2*2*0.5 = 4
        assertEquals(6L, TpGain.amount(10, 100, P));
        assertEquals(4L, TpGain.amount(10, 50, P));
        // FOCUS below one step contributes nothing extra
        assertEquals(2L, TpGain.amount(4, 100, P));
        // exactly at the minimum Release it is still just the base
        assertEquals(2L, TpGain.amount(5, 5, P));
    }

    @Test
    @DisplayName("more FOCUS and more Release never reduce the reward")
    void monotonic() {
        assertTrue(TpGain.amount(50, 100, P) > TpGain.amount(10, 100, P));
        assertTrue(TpGain.amount(10, 100, P) >= TpGain.amount(10, 50, P));
    }

    @Test
    @DisplayName("the probability roll gates the reward")
    void rollGatesReward() {
        assertEquals(TpGain.amount(10, 50, P), TpGain.forHit(10, 50, 0.0, P));
        assertEquals(TpGain.amount(10, 50, P), TpGain.forHit(10, 50, P.hitChance() - 1e-9, P));
        assertEquals(0L, TpGain.forHit(10, 50, P.hitChance(), P));
        assertEquals(0L, TpGain.forHit(10, 50, 0.99, P));
    }

    @Test
    @DisplayName("hitChance 0 grants nothing, hitChance 1 always grants")
    void chanceExtremes() {
        TpParams never = new TpParams(2, 2, 5, 0.0, 0.25, 5);
        TpParams always = new TpParams(2, 2, 5, 1.0, 0.25, 5);
        assertEquals(0L, TpGain.forHit(10, 100, 0.0, never));
        assertEquals(6L, TpGain.forHit(10, 100, 0.999, always));
    }

    @Test
    @DisplayName("expected TP over many rolls matches chance * amount")
    void expectedValue() {
        long total = 0;
        int n = 10_000;
        for (int i = 0; i < n; i++) {
            total += TpGain.forHit(10, 100, (i + 0.5) / n, P);
        }
        assertEquals(P.hitChance() * 6 * n, total, 6.0);
    }

    @Test
    @DisplayName("config values are honoured")
    void paramsHonoured() {
        TpParams p = new TpParams(10, 1, 2, 1.0, 0.25, 5);
        assertEquals(10 + 1 * 5 * 1.0, TpGain.amount(10, 100, p), 1e-9);
    }
}
