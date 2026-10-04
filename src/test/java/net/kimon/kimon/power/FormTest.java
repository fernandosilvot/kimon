package net.kimon.kimon.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Form transformations")
class FormTest {

    @Test
    @DisplayName("BASE is always available and multiplies by 1 with no drain")
    void baseIsNeutral() {
        assertEquals(1.0, Form.BASE.damageMultiplier(), 1e-9);
        assertEquals(0.0, Form.BASE.energyDrainPerSecond(), 1e-9);
        assertTrue(Form.BASE.isAvailable(0, 0));
    }

    @Test
    @DisplayName("higher forms multiply more and drain more")
    void higherFormsStronger() {
        assertTrue(Form.SUPER_SAIYAN_2.damageMultiplier() > Form.SUPER_SAIYAN.damageMultiplier());
        assertTrue(Form.SUPER_SAIYAN_3.damageMultiplier() > Form.SUPER_SAIYAN_2.damageMultiplier());
        assertTrue(Form.SUPER_SAIYAN_3.energyDrainPerSecond() > Form.SUPER_SAIYAN.energyDrainPerSecond());
    }

    @Test
    @DisplayName("a form is gated by both Power tier and Release")
    void availabilityGating() {
        // SUPER_SAIYAN needs tier 5 and 10% release.
        assertFalse(Form.SUPER_SAIYAN.isAvailable(4, 50));   // tier too low
        assertFalse(Form.SUPER_SAIYAN.isAvailable(5, 5));    // release too low
        assertTrue(Form.SUPER_SAIYAN.isAvailable(5, 10));    // both met
    }

    @Test
    @DisplayName("next() / previous() walk the ladder")
    void ladderNavigation() {
        assertEquals(Form.SUPER_SAIYAN, Form.BASE.next());
        assertEquals(Form.SUPER_SAIYAN_3, Form.SUPER_SAIYAN_2.next());
        assertNull(Form.SUPER_SAIYAN_3.next());
        assertEquals(Form.SUPER_SAIYAN_2, Form.SUPER_SAIYAN_3.previous());
        assertSame(Form.BASE, Form.BASE.previous());
    }

    @Test
    @DisplayName("highestAvailable picks the strongest reachable form")
    void highestAvailable() {
        assertSame(Form.BASE, Form.highestAvailable(0, 0));
        assertSame(Form.SUPER_SAIYAN, Form.highestAvailable(5, 10));
        assertSame(Form.SUPER_SAIYAN_3, Form.highestAvailable(100, 100));
    }

    @Test
    @DisplayName("old form names still resolve to the new forms")
    void legacyFormNames() {
        assertEquals(Form.SUPER_SAIYAN, Form.byKey("surge"));
        assertEquals(Form.SUPER_SAIYAN_2, Form.byKey("ASCENT"));
        assertEquals(Form.SUPER_SAIYAN_3, Form.byKey("zenith"));
        assertEquals(Form.SUPER_SAIYAN_2, Form.byKey("Super Saiyan 2"));
    }

    @Test
    @DisplayName("byKey resolves and is case-insensitive")
    void byKey() {
        assertEquals(Form.SUPER_SAIYAN_3, Form.byKey("super_saiyan_3"));
        assertEquals(Form.SUPER_SAIYAN_3, Form.byKey("SUPER_SAIYAN_3"));
        assertNull(Form.byKey("nope"));
    }

    @Test
    @DisplayName("a form drains Energy over time and reverts to BASE when Energy runs out")
    void formDrainsAndReverts() {
        // In SUPER_SAIYAN_3 with little energy, charging off: the drain empties energy and reverts form.
        PowerState s = new PowerState(60, 5, 0, false, Form.SUPER_SAIYAN_3);
        PowerState after = s.tick(0.05, 100, 1000, 100);
        // One tick of 12/s drain = 0.6; not empty yet, still SUPER_SAIYAN_3.
        assertSame(Form.SUPER_SAIYAN_3, after.form());

        PowerState almostEmpty = new PowerState(60, 0.1, 0, false, Form.SUPER_SAIYAN_3);
        PowerState reverted = almostEmpty.tick(0.05, 100, 1000, 100);
        assertEquals(0.0, reverted.energy(), 1e-9);
        assertSame(Form.BASE, reverted.form());
    }

    @Test
    @DisplayName("a form reverts if Release drops below its requirement")
    void formRevertsOnLowRelease() {
        // SUPER_SAIYAN needs 10% release; sit at 8% with plenty of energy, not charging.
        PowerState s = new PowerState(8, 1000, 0, false, Form.SUPER_SAIYAN);
        PowerState after = s.tick(0.05, 100, 1000, 100);
        assertSame(Form.BASE, after.form());
    }

    @Test
    @DisplayName("formMultiplier reflects the active form")
    void formMultiplier() {
        assertEquals(1.0, PowerState.INITIAL.formMultiplier(), 1e-9);
        assertEquals(Form.SUPER_SAIYAN_2.damageMultiplier(),
                PowerState.INITIAL.withForm(Form.SUPER_SAIYAN_2).formMultiplier(), 1e-9);
    }
}
