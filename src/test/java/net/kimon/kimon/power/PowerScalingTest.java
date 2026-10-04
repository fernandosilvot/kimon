package net.kimon.kimon.power;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PowerScaling}'s tier/bonus formulas. Pure JVM, no Minecraft.
 */
@DisplayName("PowerScaling bonus formulas")
class PowerScalingTest {

    @Test
    @DisplayName("zero or negative Power yields zero tiers and zero bonuses")
    void zeroPower() {
        assertEquals(0, PowerScaling.tiers(0));
        assertEquals(0, PowerScaling.tiers(-10));
        assertEquals(0.0, PowerScaling.bonusHealth(0));
        assertEquals(0.0, PowerScaling.bonusAttackDamage(0));
        assertEquals(0.0, PowerScaling.bonusMovementSpeed(0));
    }

    @Test
    @DisplayName("Power below one tier threshold still gives zero tiers")
    void belowFirstTier() {
        assertEquals(0, PowerScaling.tiers(PowerScaling.POWER_PER_TIER - 1));
    }

    @Test
    @DisplayName("one full tier is reached exactly at POWER_PER_TIER")
    void firstTierBoundary() {
        assertEquals(1, PowerScaling.tiers(PowerScaling.POWER_PER_TIER));
        assertEquals(1, PowerScaling.tiers(PowerScaling.POWER_PER_TIER + 1));
    }

    @Test
    @DisplayName("tiers accumulate linearly with Power")
    void tiersAccumulate() {
        assertEquals(4, PowerScaling.tiers(PowerScaling.POWER_PER_TIER * 4));
        assertEquals(10, PowerScaling.tiers(PowerScaling.POWER_PER_TIER * 10));
    }

    @Test
    @DisplayName("tiers are capped at MAX_TIERS")
    void tiersCapped() {
        int hugePower = PowerScaling.POWER_PER_TIER * (PowerScaling.MAX_TIERS + 100);
        assertEquals(PowerScaling.MAX_TIERS, PowerScaling.tiers(hugePower));
    }

    @Test
    @DisplayName("bonuses equal tiers times the per-tier constant")
    void bonusesMatchTiers() {
        int power = PowerScaling.POWER_PER_TIER * 3; // 3 tiers
        assertEquals(3 * PowerScaling.HEALTH_PER_TIER, PowerScaling.bonusHealth(power), 1e-9);
        assertEquals(3 * PowerScaling.ATTACK_PER_TIER, PowerScaling.bonusAttackDamage(power), 1e-9);
        assertEquals(3 * PowerScaling.SPEED_PER_TIER, PowerScaling.bonusMovementSpeed(power), 1e-9);
    }

    @Test
    @DisplayName("bonuses are bounded at the tier cap")
    void bonusesBoundedAtCap() {
        int maxedPower = PowerData.MAX_POWER;
        assertEquals(PowerScaling.MAX_TIERS * PowerScaling.HEALTH_PER_TIER,
                PowerScaling.bonusHealth(maxedPower), 1e-9);
    }
}
