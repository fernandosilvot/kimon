package net.kimon.kimon.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Empowered hit costs")
class CombatCostsTest {

    @Test
    @DisplayName("Energy per hit is 1 + STR/200: stronger characters pay more")
    void kiPerHit() {
        assertEquals(1.0, CombatCosts.kiPerHit(0), 1e-9);
        assertEquals(2.0, CombatCosts.kiPerHit(200), 1e-9);
        assertTrue(CombatCosts.kiPerHit(1000) > CombatCosts.kiPerHit(100));
        assertEquals(1.0, CombatCosts.kiPerHit(-10), 1e-9, "negative STR is clamped");
    }

    @Test
    @DisplayName("Stamina per hit is a fraction of the pool")
    void staminaPerHit() {
        assertEquals(1.4, CombatCosts.staminaPerHit(70, 0.02), 1e-9);
        assertEquals(0.0, CombatCosts.staminaPerHit(70, 0.0), 1e-9);
        assertEquals(0.0, CombatCosts.staminaPerHit(-5, 0.5), 1e-9);
    }

    @Test
    @DisplayName("a hit needs both Energy and Stamina")
    void canPay() {
        assertTrue(CombatCosts.canPay(10, 10, 5, 5));
        assertTrue(CombatCosts.canPay(5, 5, 5, 5), "exactly enough is enough");
        assertFalse(CombatCosts.canPay(4.9, 10, 5, 5));
        assertFalse(CombatCosts.canPay(10, 4.9, 5, 5));
    }
}
