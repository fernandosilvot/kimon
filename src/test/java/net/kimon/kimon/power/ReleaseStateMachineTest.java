package net.kimon.kimon.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Release state machine (Stable / Charging / At max / Lowering / Exhausted)")
class ReleaseStateMachineTest {

    private static final PowerParams P = PowerParams.DEFAULTS;
    private static final double MAX_RELEASE = 50.0;
    private static final double MAX_ENERGY = 400.0;
    private static final double MAX_STAMINA = 70.0;
    private static final double DT = 0.05;

    private static PowerState tick(PowerState s) {
        return s.tick(DT, MAX_RELEASE, MAX_ENERGY, MAX_STAMINA, P);
    }

    private static PowerState tickFor(PowerState s, double seconds) {
        for (int i = 0, n = (int) Math.round(seconds / DT); i < n; i++) {
            s = tick(s);
        }
        return s;
    }

    private static PowerState idle(double release, double energy) {
        return new PowerState(release, energy, 0, false, false, false, ReleaseState.STABLE, Form.BASE);
    }

    @Test
    @DisplayName("no input → STABLE")
    void stableByDefault() {
        assertEquals(ReleaseState.STABLE, tick(idle(0, MAX_ENERGY)).releaseState());
    }

    @Test
    @DisplayName("charge below the ceiling → CHARGING and Release rises at chargeRate")
    void charging() {
        PowerState s = tick(idle(0, MAX_ENERGY).withInput(true, false, false));
        assertEquals(ReleaseState.CHARGING, s.releaseState());
        assertEquals(P.chargeRate() * DT, s.release(), 1e-9);
    }

    @Test
    @DisplayName("charge reaching the ceiling → AT_MAX, and Release is capped")
    void atMax() {
        PowerState s = tickFor(idle(0, MAX_ENERGY).withInput(true, false, false), 20);
        assertEquals(ReleaseState.AT_MAX, s.releaseState());
        assertEquals(MAX_RELEASE, s.release(), 1e-9);
    }

    @Test
    @DisplayName("charging is slower at or above 50%")
    void slowerAbove50() {
        PowerState low = tick(idle(10, MAX_ENERGY).withInput(true, false, false));
        PowerState high = s(60);
        double lowGain = low.release() - 10;
        double highGain = high.tick(DT, 100, MAX_ENERGY, MAX_STAMINA, P).release() - 60;
        assertEquals(lowGain * P.slowdownAbove50(), highGain, 1e-9);
    }

    private static PowerState s(double release) {
        return idle(release, MAX_ENERGY).withInput(true, false, false);
    }

    @Test
    @DisplayName("charge→max takes ~5 s to 50% at the default 10%/s")
    void defaultFiveSecondsToFifty() {
        PowerState s = tickFor(idle(0, MAX_ENERGY).withInput(true, false, false), 5.0);
        assertTrue(s.release() > 45 && s.release() <= 50, "got " + s.release());
    }

    @Test
    @DisplayName("discharge → LOWERING at dischargeRate, bottoming out at 0 as STABLE")
    void discharging() {
        PowerState s = tick(idle(40, MAX_ENERGY).withInput(false, true, false));
        assertEquals(ReleaseState.LOWERING, s.releaseState());
        assertEquals(40 - P.dischargeRate() * DT, s.release(), 1e-9);

        PowerState bottom = tickFor(idle(10, MAX_ENERGY).withInput(false, true, false), 5);
        assertEquals(0.0, bottom.release(), 1e-9);
        assertEquals(ReleaseState.STABLE, bottom.releaseState());
    }

    @Test
    @DisplayName("discharge wins when charge and discharge are both held")
    void dischargeWins() {
        PowerState s = tick(idle(30, MAX_ENERGY).withInput(true, true, false));
        assertEquals(ReleaseState.LOWERING, s.releaseState());
        assertTrue(s.release() < 30);
    }

    @Test
    @DisplayName("turbo multiplies the charge rate and costs extra Energy")
    void turbo() {
        // Start below max Energy so neither result is clamped to the pool size.
        PowerState normal = tick(idle(10, 200).withInput(true, false, false));
        PowerState turbo = tick(idle(10, 200).withInput(true, false, true));
        assertEquals((normal.release() - 10) * P.turboMult(), turbo.release() - 10, 1e-9);
        assertTrue(turbo.energy() < normal.energy(), "turbo drains more Energy");
    }

    @Test
    @DisplayName("turbo without charging costs nothing extra")
    void turboIdleIsFree() {
        PowerState a = tick(idle(10, MAX_ENERGY));
        PowerState b = tick(idle(10, MAX_ENERGY).withInput(false, false, true));
        assertEquals(a.energy(), b.energy(), 1e-9);
    }

    @Test
    @DisplayName("holding Release costs Energy (upkeep) with no regen above the cutoff")
    void upkeepDrainsAtHighRelease() {
        PowerState s = tick(idle(50, 200));
        assertTrue(s.energy() < 200);
    }

    @Test
    @DisplayName("Energy hitting 0 → EXHAUSTED: Release 0, form dropped")
    void exhaustion() {
        PowerState s = new PowerState(40, 0.001, 0, true, false, false, ReleaseState.CHARGING, Form.SUPER_SAIYAN);
        PowerState after = tick(s);
        assertEquals(ReleaseState.EXHAUSTED, after.releaseState());
        assertEquals(0.0, after.release(), 1e-9);
        assertEquals(Form.BASE, after.form());
    }

    @Test
    @DisplayName("EXHAUSTED ignores charge until Energy recovers to the threshold, then can charge again")
    void exhaustedLockAndRecovery() {
        PowerState s = new PowerState(0, 0, 0, true, false, false, ReleaseState.EXHAUSTED, Form.BASE);
        PowerState early = tick(s);
        assertEquals(ReleaseState.EXHAUSTED, early.releaseState());
        assertEquals(0.0, early.release(), 1e-9, "no charging while exhausted");

        double threshold = MAX_ENERGY * P.exhaustRecoverPct();
        PowerState state = s;
        int guard = 0;
        while (state.releaseState() == ReleaseState.EXHAUSTED && guard++ < 10_000) {
            state = tick(state);
        }
        assertTrue(guard < 10_000, "must eventually recover");
        assertTrue(state.energy() >= threshold - 1e-6);

        PowerState charged = tickFor(state, 1.0);
        assertTrue(charged.release() > 0, "can charge after recovering");
    }

    @Test
    @DisplayName("reset() drops Release to 0 and reverts the form")
    void reset() {
        PowerState s = new PowerState(70, 100, 5, true, false, false, ReleaseState.CHARGING, Form.SUPER_SAIYAN_2).reset();
        assertEquals(0.0, s.release(), 1e-9);
        assertEquals(Form.BASE, s.form());
        assertEquals(ReleaseState.STABLE, s.releaseState());
        assertEquals(100.0, s.energy(), 1e-9);
    }

    @Test
    @DisplayName("reset() does not clear EXHAUSTED")
    void resetKeepsExhausted() {
        PowerState s = new PowerState(0, 1, 0, false, false, false, ReleaseState.EXHAUSTED, Form.BASE).reset();
        assertEquals(ReleaseState.EXHAUSTED, s.releaseState());
    }

    @Test
    @DisplayName("lowering the ceiling clamps Release down to it")
    void ceilingDropClamps() {
        PowerState s = idle(80, MAX_ENERGY).tick(DT, 50, MAX_ENERGY, MAX_STAMINA, P);
        assertEquals(50.0, s.release(), 1e-9);
    }

    @Test
    @DisplayName("config values are honoured (faster charge, slower regen rate)")
    void paramsAreHonoured() {
        PowerParams fast = new PowerParams(50, false, 40, 0.5, 1.5, 0.01, 25, 0.002,
                40, 0.02, KiRegenRate.NORMAL, 50, 0.05);
        PowerState s = idle(0, MAX_ENERGY).withInput(true, false, false)
                .tick(DT, 50, MAX_ENERGY, MAX_STAMINA, fast);
        assertEquals(40 * DT, s.release(), 1e-9);

        PowerParams slow = new PowerParams(50, false, 10, 0.5, 1.5, 0.01, 25, 0.002,
                40, 0.02, KiRegenRate.SLOW, 50, 0.05);
        PowerState normalRegen = idle(0, 100).tick(DT, 50, MAX_ENERGY, MAX_STAMINA, P);
        PowerState slowRegen = idle(0, 100).tick(DT, 50, MAX_ENERGY, MAX_STAMINA, slow);
        assertEquals((normalRegen.energy() - 100) * 0.5, slowRegen.energy() - 100, 1e-9);
    }

    @Test
    @DisplayName("hardMaxRelease is 100, or 200 with overcharge")
    void hardMax() {
        assertEquals(100.0, P.hardMaxRelease(), 1e-9);
        PowerParams over = new PowerParams(100, true, 10, 0.5, 1.5, 0.01, 25, 0.002,
                40, 0.02, KiRegenRate.NORMAL, 50, 0.05);
        assertEquals(200.0, over.hardMaxRelease(), 1e-9);
        PowerState s = idle(150, 4000).withInput(true, false, false)
                .tick(DT, 200, 4000, MAX_STAMINA, over);
        assertTrue(s.release() > 150 && s.release() <= 200);
    }

    @Test
    @DisplayName("ReleaseState.byOrdinal falls back to STABLE for bad input")
    void byOrdinal() {
        assertEquals(ReleaseState.EXHAUSTED, ReleaseState.byOrdinal(4));
        assertEquals(ReleaseState.STABLE, ReleaseState.byOrdinal(99));
        assertEquals(ReleaseState.STABLE, ReleaseState.byOrdinal(-1));
    }
}
