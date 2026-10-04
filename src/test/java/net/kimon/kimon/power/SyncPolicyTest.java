package net.kimon.kimon.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Sync policy and aura state")
class SyncPolicyTest {

    private static PowerState at(double release, double energy) {
        return new PowerState(release, energy, 10, false, Form.BASE);
    }

    @Test
    @DisplayName("the first send always happens")
    void firstSend() {
        assertTrue(SyncPolicy.shouldSendResources(1, null, at(0, 0)));
    }

    @Test
    @DisplayName("resources are sent every 2 ticks and only when something changed")
    void throttle() {
        PowerState last = at(10, 100);
        PowerState changed = at(11, 100);
        assertTrue(SyncPolicy.shouldSendResources(2, last, changed));
        assertTrue(SyncPolicy.shouldSendResources(4, last, changed));
        assertFalse(SyncPolicy.shouldSendResources(3, last, changed), "odd tick: wait");
        assertFalse(SyncPolicy.shouldSendResources(2, last, last), "unchanged: nothing to send");
    }

    @Test
    @DisplayName("tiny float noise does not trigger a sync, and the lock counter is ignored")
    void noiseAndLock() {
        PowerState a = at(10, 100);
        assertTrue(SyncPolicy.sameForSync(a, at(10 + 1e-6, 100)));
        assertTrue(SyncPolicy.sameForSync(a, a.hurt(600)));
        assertFalse(SyncPolicy.sameForSync(a, at(10.01, 100)));
        assertFalse(SyncPolicy.sameForSync(a, a.withForm(Form.SURGE)));
        assertFalse(SyncPolicy.sameForSync(a, a.withInput(true, false, false)));
    }

    @Test
    @DisplayName("a steady player generates 1 send per 2 ticks at most, not 1 per tick")
    void bandwidth() {
        int sends = 0;
        PowerState last = null;
        for (int tick = 1; tick <= 200; tick++) {
            PowerState now = at(tick * 0.1, 100); // changes every tick
            if (SyncPolicy.shouldSendResources(tick, last, now)) {
                sends++;
                last = now;
            }
        }
        assertTrue(sends <= 101, "got " + sends);
    }

    @Test
    @DisplayName("aura is re-sent only when it changes")
    void auraSend() {
        AuraState a = new AuraState(true, false, 3);
        assertTrue(SyncPolicy.shouldSendAura(null, a));
        assertFalse(SyncPolicy.shouldSendAura(a, new AuraState(true, false, 3)));
        assertTrue(SyncPolicy.shouldSendAura(a, new AuraState(true, false, 4)));
        assertTrue(SyncPolicy.shouldSendAura(a, new AuraState(true, true, 3)));
    }

    @Test
    @DisplayName("aura shows only while charging, in 10% buckets, and never leaks exact Release")
    void auraOf() {
        PowerState charging = new PowerState(37, 100, 10, true, false, true, ReleaseState.CHARGING, Form.BASE);
        assertEquals(new AuraState(true, true, 3), AuraState.of(charging));

        PowerState atMax = new PowerState(50, 100, 10, true, false, false, ReleaseState.AT_MAX, Form.BASE);
        assertEquals(new AuraState(true, false, 5), AuraState.of(atMax));

        PowerState stable = new PowerState(60, 100, 10, false, false, true, ReleaseState.STABLE, Form.BASE);
        assertEquals(new AuraState(false, false, 6), AuraState.of(stable), "turbo only shows while charging");

        PowerState overcharged = new PowerState(250, 100, 10, true, false, false, ReleaseState.CHARGING, Form.BASE);
        assertEquals(20, AuraState.of(overcharged).tier(), "bucket is capped");
    }

    @Test
    @DisplayName("AuraCache stores, returns NONE when unknown, and forgets the idle")
    void auraCache() {
        AuraCache.clear();
        assertEquals(AuraState.NONE, AuraCache.get(7));
        AuraCache.put(7, new AuraState(true, false, 2));
        assertEquals(new AuraState(true, false, 2), AuraCache.get(7));
        AuraCache.put(7, AuraState.NONE);
        assertEquals(AuraState.NONE, AuraCache.get(7));
        AuraCache.put(8, new AuraState(true, false, 1));
        AuraCache.clear();
        assertEquals(AuraState.NONE, AuraCache.get(8));
    }
}
