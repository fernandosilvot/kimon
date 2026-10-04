package net.kimon.kimon.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PowerState resource/Release loop")
class PowerStateTest {

    private static final double MAX_RELEASE = 50.0;
    private static final double MAX_ENERGY = 400.0;
    private static final double MAX_STAMINA = 70.0;

    private static PowerState tickFor(PowerState s, double seconds) {
        int steps = (int) Math.round(seconds / 0.05);
        for (int i = 0; i < steps; i++) {
            s = s.tick(0.05, MAX_RELEASE, MAX_ENERGY, MAX_STAMINA);
        }
        return s;
    }

    @Test
    @DisplayName("charging raises Release toward the max")
    void chargingRaisesRelease() {
        PowerState s = new PowerState(0, MAX_ENERGY, 0, true, Form.BASE);
        PowerState after = tickFor(s, 1.0);
        assertTrue(after.release() > 0, "Release should rise while charging");
        assertTrue(after.release() <= MAX_RELEASE, "Release never exceeds max");
    }

    @Test
    @DisplayName("Release never exceeds the ceiling")
    void releaseCappedAtMax() {
        PowerState s = new PowerState(0, MAX_ENERGY, 0, true, Form.BASE);
        PowerState after = tickFor(s, 10.0);
        assertTrue(after.release() <= MAX_RELEASE + 1e-6);
    }

    @Test
    @DisplayName("not charging decays Release back toward zero")
    void decayWhenNotCharging() {
        PowerState s = new PowerState(40, MAX_ENERGY, 0, false, Form.BASE);
        PowerState after = tickFor(s, 1.0);
        assertTrue(after.release() < 40, "Release should decay");
    }

    @Test
    @DisplayName("Energy regenerates at low Release")
    void energyRegenAtLowRelease() {
        PowerState s = new PowerState(0, 100, 0, false, Form.BASE);
        PowerState after = tickFor(s, 1.0);
        assertTrue(after.energy() > 100, "Energy should regen when Release is low");
    }

    @Test
    @DisplayName("Energy does not regenerate at/above the cutoff Release")
    void noRegenAboveCutoff() {
        // Hold at the cutoff, not charging; energy should not climb from regen.
        PowerState s = new PowerState(PowerState.ENERGY_REGEN_CUTOFF, 100, 0, false, Form.BASE);
        PowerState oneTick = s.tick(0.05, 60, MAX_ENERGY, MAX_STAMINA);
        assertTrue(oneTick.energy() <= 100 + 1e-6, "no regen at/above cutoff");
    }

    @Test
    @DisplayName("running out of energy collapses Release to zero")
    void outOfEnergyCollapsesRelease() {
        // High release, almost no energy, charging: upkeep drains the last energy.
        PowerState s = new PowerState(90, 0.01, 0, true, Form.BASE);
        PowerState after = s.tick(0.05, 100, MAX_ENERGY, MAX_STAMINA);
        assertEquals(0.0, after.energy(), 1e-9);
        assertEquals(0.0, after.release(), 1e-9);
    }

    @Test
    @DisplayName("stamina regenerates over time")
    void staminaRegen() {
        PowerState s = new PowerState(0, MAX_ENERGY, 0, false, Form.BASE);
        PowerState after = tickFor(s, 1.0);
        assertTrue(after.stamina() > 0);
    }

    @Test
    @DisplayName("isActive requires minimum Release")
    void isActiveThreshold() {
        assertFalse(new PowerState(0, 0, 0, false, Form.BASE).isActive());
        assertFalse(new PowerState(PowerState.MIN_ACTIVE_RELEASE - 1, 0, 0, false, Form.BASE).isActive());
        assertTrue(new PowerState(PowerState.MIN_ACTIVE_RELEASE, 0, 0, false, Form.BASE).isActive());
    }

    @Test
    @DisplayName("releaseMultiplier maps 0..100% to 0..1")
    void releaseMultiplier() {
        assertEquals(0.0, new PowerState(0, 0, 0, false, Form.BASE).releaseMultiplier(), 1e-9);
        assertEquals(0.5, new PowerState(50, 0, 0, false, Form.BASE).releaseMultiplier(), 1e-9);
        assertEquals(1.0, new PowerState(100, 0, 0, false, Form.BASE).releaseMultiplier(), 1e-9);
    }
}
