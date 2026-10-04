package net.kimon.kimon.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PowerData}'s progression rules.
 *
 * <p>These run on a plain JVM with no Minecraft bootstrap, because {@link PowerData} is pure data +
 * math. This is the fast, reliable test tier: it exercises every balancing rule without the game.</p>
 */
@DisplayName("PowerData progression")
class PowerDataTest {

    @Test
    @DisplayName("a fresh player starts at zero Power")
    void initialIsZero() {
        assertEquals(0, PowerData.INITIAL.power());
    }

    @Test
    @DisplayName("training grants exactly TRAIN_GAIN Power")
    void trainGrantsGain() {
        PowerData trained = PowerData.INITIAL.train();
        assertEquals(PowerData.TRAIN_GAIN, trained.power());
    }

    @Test
    @DisplayName("training repeatedly accumulates")
    void trainAccumulates() {
        PowerData data = PowerData.INITIAL;
        for (int i = 0; i < 10; i++) {
            data = data.train();
        }
        assertEquals(10 * PowerData.TRAIN_GAIN, data.power());
    }

    @Test
    @DisplayName("PowerData is immutable: train() returns a new instance")
    void trainIsImmutable() {
        PowerData original = new PowerData(100);
        PowerData trained = original.train();
        assertEquals(100, original.power(), "original must be untouched");
        assertNotSame(original, trained);
    }

    @Test
    @DisplayName("negative values are clamped to zero")
    void clampsNegative() {
        assertEquals(0, new PowerData(-50).power());
    }

    @Test
    @DisplayName("values above MAX_POWER are clamped to the ceiling")
    void clampsAboveMax() {
        assertEquals(PowerData.MAX_POWER, new PowerData(PowerData.MAX_POWER + 1_000).power());
    }

    @Test
    @DisplayName("training at the ceiling does not overflow past MAX_POWER")
    void trainAtCeilingStaysMaxed() {
        PowerData maxed = new PowerData(PowerData.MAX_POWER);
        assertEquals(PowerData.MAX_POWER, maxed.train().power());
    }

    @Test
    @DisplayName("isMaxed reflects the ceiling")
    void isMaxedReflectsCeiling() {
        assertFalse(PowerData.INITIAL.isMaxed());
        assertFalse(new PowerData(PowerData.MAX_POWER - 1).isMaxed());
        assertTrue(new PowerData(PowerData.MAX_POWER).isMaxed());
    }

    @Test
    @DisplayName("withPower sets an explicit (clamped) value")
    void withPowerSetsValue() {
        assertEquals(42, PowerData.INITIAL.withPower(42).power());
        assertEquals(0, PowerData.INITIAL.withPower(-1).power());
    }
}
