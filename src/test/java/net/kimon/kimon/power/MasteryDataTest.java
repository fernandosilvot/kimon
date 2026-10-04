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
            m = m.practice(Form.SUPER_SAIYAN, 0.05);
        }
        assertEquals(5, m.level(Form.SUPER_SAIYAN));
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
        MasteryData m = MasteryData.of(Map.of(Form.SUPER_SAIYAN_3, MasteryData.MAX_LEVEL + 100));
        assertEquals(MasteryData.MAX_LEVEL, m.level(Form.SUPER_SAIYAN_3));
        // Practising at the cap is a no-op (returns same instance).
        assertSame(m, m.practice(Form.SUPER_SAIYAN_3, 10.0));
    }

    @Test
    @DisplayName("practising only affects the practised form")
    void onlyAffectsPractisedForm() {
        MasteryData m = MasteryData.initial().practice(Form.SUPER_SAIYAN, 10.0);
        assertTrue(m.level(Form.SUPER_SAIYAN) > 0);
        assertEquals(0, m.level(Form.SUPER_SAIYAN_2));
        assertEquals(0, m.level(Form.SUPER_SAIYAN_3));
    }

    @Test
    @DisplayName("mastery raises the effective damage multiplier up to the bonus cap")
    void masteryBoostsDamage() {
        MasteryData none = MasteryData.initial();
        MasteryData full = MasteryData.of(Map.of(Form.SUPER_SAIYAN_3, MasteryData.MAX_LEVEL));

        assertEquals(Form.SUPER_SAIYAN_3.damageMultiplier(), none.effectiveDamageMultiplier(Form.SUPER_SAIYAN_3), 1e-9);
        assertEquals(Form.SUPER_SAIYAN_3.damageMultiplier() + MasteryData.MAX_DAMAGE_BONUS,
                full.effectiveDamageMultiplier(Form.SUPER_SAIYAN_3), 1e-9);
    }

    @Test
    @DisplayName("mastery reduces the effective Energy drain up to the reduction cap")
    void masteryReducesDrain() {
        MasteryData none = MasteryData.initial();
        MasteryData full = MasteryData.of(Map.of(Form.SUPER_SAIYAN_2, MasteryData.MAX_LEVEL));

        assertEquals(Form.SUPER_SAIYAN_2.energyDrainPerSecond(), none.effectiveDrain(Form.SUPER_SAIYAN_2), 1e-9);
        assertEquals(Form.SUPER_SAIYAN_2.energyDrainPerSecond() * (1.0 - MasteryData.MAX_DRAIN_REDUCTION),
                full.effectiveDrain(Form.SUPER_SAIYAN_2), 1e-9);
    }

    @Test
    @DisplayName("asLevelMap round-trips through of()")
    void roundTrip() {
        MasteryData m = MasteryData.of(Map.of(Form.SUPER_SAIYAN, 7, Form.SUPER_SAIYAN_3, 20));
        MasteryData copy = MasteryData.of(m.asLevelMap());
        assertEquals(7, copy.level(Form.SUPER_SAIYAN));
        assertEquals(20, copy.level(Form.SUPER_SAIYAN_3));
        assertEquals(0, copy.level(Form.SUPER_SAIYAN_2));
    }
}
