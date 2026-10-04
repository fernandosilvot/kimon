package net.kimon.kimon.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Post-damage regeneration lock")
class RegenLockTest {

    private static final PowerParams P = PowerParams.DEFAULTS;
    private static final double DT = 0.05;

    private static PowerState idle(double energy, double stamina) {
        return new PowerState(0, energy, stamina, false, false, false, ReleaseState.STABLE, Form.BASE);
    }

    private static PowerState tick(PowerState s, PowerParams p) {
        return s.tick(DT, 50, 400, 70, p);
    }

    @Test
    @DisplayName("hurt() starts the lock and never shortens a longer one")
    void hurtStartsLock() {
        assertEquals(600, idle(100, 0).hurt(600).regenLock());
        assertEquals(600, idle(100, 0).hurt(600).hurt(10).regenLock());
        assertEquals(0, idle(100, 0).hurt(-5).regenLock());
    }

    @Test
    @DisplayName("while locked, Energy does not regenerate; the lock counts down one per tick")
    void lockBlocksEnergyRegen() {
        PowerState free = tick(idle(100, 0), P);
        assertTrue(free.energy() > 100, "regens when not locked");

        PowerState locked = tick(idle(100, 0).hurt(5), P);
        assertEquals(100.0, locked.energy(), 1e-9);
        assertEquals(4, locked.regenLock());
    }

    @Test
    @DisplayName("regeneration resumes once the lock expires (600 ticks = 30 s)")
    void regenResumesAfterLock() {
        PowerState s = idle(100, 0).hurt(P.regenLockTicks());
        for (int i = 0; i < P.regenLockTicks(); i++) {
            s = tick(s, P);
        }
        assertEquals(100.0, s.energy(), 1e-9, "no regen for the whole lock");
        assertEquals(0, s.regenLock());
        assertTrue(tick(s, P).energy() > 100, "regen is back after the lock");
    }

    @Test
    @DisplayName("Stamina keeps regenerating during the lock unless staminaRegenLocked is set")
    void staminaLockIsOptional() {
        PowerState hurt = idle(100, 10).hurt(100);
        assertTrue(tick(hurt, P).stamina() > 10);

        PowerParams strict = new PowerParams(50, false, 10, 0.5, 1.5, 0.01, 25, 0.002,
                40, 0.02, KiRegenRate.NORMAL, 50, 0.05, 600, true, 0.02);
        assertEquals(10.0, tick(hurt, strict).stamina(), 1e-9);
    }

    @Test
    @DisplayName("an exhausted player cannot recover Energy while locked")
    void exhaustionRecoveryBlocked() {
        PowerState s = new PowerState(0, 0, 0, false, false, false, ReleaseState.EXHAUSTED, Form.BASE, 100);
        PowerState after = tick(s, P);
        assertEquals(ReleaseState.EXHAUSTED, after.releaseState());
        assertEquals(0.0, after.energy(), 1e-9);
    }

    @Test
    @DisplayName("the lock survives input, form and resource changes")
    void lockIsPreserved() {
        PowerState s = idle(100, 10).hurt(50);
        assertEquals(50, s.withInput(true, false, true).regenLock());
        assertEquals(50, s.withForm(Form.SURGE).regenLock());
        assertEquals(50, s.reset().regenLock());
        assertEquals(50, s.withResources(1, 2, 3, 50, 400, 70).regenLock());
    }

    @Test
    @DisplayName("regenLockTicks = 0 disables the lock")
    void zeroDisables() {
        PowerParams off = new PowerParams(50, false, 10, 0.5, 1.5, 0.01, 25, 0.002,
                40, 0.02, KiRegenRate.NORMAL, 50, 0.05, 0, false, 0.02);
        assertEquals(0, idle(100, 0).hurt(off.regenLockTicks()).regenLock());
    }
}
