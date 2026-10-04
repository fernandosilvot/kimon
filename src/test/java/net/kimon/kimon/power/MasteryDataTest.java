package net.kimon.kimon.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Form Mastery")
class MasteryDataTest {

    @Test
    @DisplayName("a fresh player has zero mastery everywhere")
    void initialZero() {
        MasteryData m = MasteryData.initial();
        for (Form f : Form.VALUES) {
            assertEquals(0, m.level(f));
        }
    }

    @Test
    @DisplayName("practising a form raises its mastery over time")
    void practiceRaisesMastery() {
        MasteryData m = MasteryData.initial();
        // 10 seconds at 0.5/s = level 5.
        for (int i = 0; i < 200; i++) {
            m = m.practice(Form.SURGE, 0.05);
        }
        assertEquals(5, m.level(Form.SURGE));
    }

    @Test
    @DisplayName("BASE never gains mastery")
    void baseNeverGains() {
        MasteryData m = MasteryData.initial().practice(Form.BASE, 100.0);
        assertEquals(0, m.level(Form.BASE));
    }

    @Test
    @DisplayName("mastery is capped at MAX_LEVEL")
    void cappedAtMax() {
        MasteryData m = MasteryData.of(Map.of(Form.ZENITH, MasteryData.MAX_LEVEL + 100));
        assertEquals(MasteryData.MAX_LEVEL, m.level(Form.ZENITH));
        // Practising at the cap is a no-op (returns same instance).
        assertSame(m, m.practice(Form.ZENITH, 10.0));
    }

    @Test
    @DisplayName("practising only affects the practised form")
    void onlyAffectsPractisedForm() {
        MasteryData m = MasteryData.initial().practice(Form.SURGE, 10.0);
        assertTrue(m.level(Form.SURGE) > 0);
        assertEquals(0, m.level(Form.ASCENT));
        assertEquals(0, m.level(Form.ZENITH));
    }

    @Test
    @DisplayName("mastery raises the effective damage multiplier up to the bonus cap")
    void masteryBoostsDamage() {
        MasteryData none = MasteryData.initial();
        MasteryData full = MasteryData.of(Map.of(Form.ZENITH, MasteryData.MAX_LEVEL));

        assertEquals(Form.ZENITH.damageMultiplier(), none.effectiveDamageMultiplier(Form.ZENITH), 1e-9);
        assertEquals(Form.ZENITH.damageMultiplier() + MasteryData.MAX_DAMAGE_BONUS,
                full.effectiveDamageMultiplier(Form.ZENITH), 1e-9);
    }

    @Test
    @DisplayName("mastery reduces the effective Energy drain up to the reduction cap")
    void masteryReducesDrain() {
        MasteryData none = MasteryData.initial();
        MasteryData full = MasteryData.of(Map.of(Form.ASCENT, MasteryData.MAX_LEVEL));

        assertEquals(Form.ASCENT.energyDrainPerSecond(), none.effectiveDrain(Form.ASCENT), 1e-9);
        assertEquals(Form.ASCENT.energyDrainPerSecond() * (1.0 - MasteryData.MAX_DRAIN_REDUCTION),
                full.effectiveDrain(Form.ASCENT), 1e-9);
    }

    @Test
    @DisplayName("asLevelMap round-trips through of()")
    void roundTrip() {
        MasteryData m = MasteryData.of(Map.of(Form.SURGE, 7, Form.ZENITH, 20));
        MasteryData copy = MasteryData.of(m.asLevelMap());
        assertEquals(7, copy.level(Form.SURGE));
        assertEquals(20, copy.level(Form.ZENITH));
        assertEquals(0, copy.level(Form.ASCENT));
    }
}
