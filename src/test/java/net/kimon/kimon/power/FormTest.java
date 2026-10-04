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
        assertTrue(Form.ASCENT.damageMultiplier() > Form.SURGE.damageMultiplier());
        assertTrue(Form.ZENITH.damageMultiplier() > Form.ASCENT.damageMultiplier());
        assertTrue(Form.ZENITH.energyDrainPerSecond() > Form.SURGE.energyDrainPerSecond());
    }

    @Test
    @DisplayName("a form is gated by both Power tier and Release")
    void availabilityGating() {
        // SURGE needs tier 5 and 10% release.
        assertFalse(Form.SURGE.isAvailable(4, 50));   // tier too low
        assertFalse(Form.SURGE.isAvailable(5, 5));    // release too low
        assertTrue(Form.SURGE.isAvailable(5, 10));    // both met
    }

    @Test
    @DisplayName("next() / previous() walk the ladder")
    void ladderNavigation() {
        assertEquals(Form.SURGE, Form.BASE.next());
        assertEquals(Form.ZENITH, Form.ASCENT.next());
        assertNull(Form.ZENITH.next());
        assertEquals(Form.ASCENT, Form.ZENITH.previous());
        assertSame(Form.BASE, Form.BASE.previous());
    }

    @Test
    @DisplayName("highestAvailable picks the strongest reachable form")
    void highestAvailable() {
        assertSame(Form.BASE, Form.highestAvailable(0, 0));
        assertSame(Form.SURGE, Form.highestAvailable(5, 10));
        assertSame(Form.ZENITH, Form.highestAvailable(100, 100));
    }

    @Test
    @DisplayName("byKey resolves and is case-insensitive")
    void byKey() {
        assertEquals(Form.ZENITH, Form.byKey("zenith"));
        assertEquals(Form.ZENITH, Form.byKey("ZENITH"));
        assertNull(Form.byKey("nope"));
    }

    @Test
    @DisplayName("a form drains Energy over time and reverts to BASE when Energy runs out")
    void formDrainsAndReverts() {
        // In ZENITH with little energy, charging off: the drain empties energy and reverts form.
        PowerState s = new PowerState(60, 5, 0, false, Form.ZENITH);
        PowerState after = s.tick(0.05, 100, 1000, 100);
        // One tick of 12/s drain = 0.6; not empty yet, still ZENITH.
        assertSame(Form.ZENITH, after.form());

        PowerState almostEmpty = new PowerState(60, 0.1, 0, false, Form.ZENITH);
        PowerState reverted = almostEmpty.tick(0.05, 100, 1000, 100);
        assertEquals(0.0, reverted.energy(), 1e-9);
        assertSame(Form.BASE, reverted.form());
    }

    @Test
    @DisplayName("a form reverts if Release drops below its requirement")
    void formRevertsOnLowRelease() {
        // SURGE needs 10% release; sit at 8% with plenty of energy, not charging.
        PowerState s = new PowerState(8, 1000, 0, false, Form.SURGE);
        PowerState after = s.tick(0.05, 100, 1000, 100);
        assertSame(Form.BASE, after.form());
    }

    @Test
    @DisplayName("formMultiplier reflects the active form")
    void formMultiplier() {
        assertEquals(1.0, PowerState.INITIAL.formMultiplier(), 1e-9);
        assertEquals(Form.ASCENT.damageMultiplier(),
                PowerState.INITIAL.withForm(Form.ASCENT).formMultiplier(), 1e-9);
    }
}
