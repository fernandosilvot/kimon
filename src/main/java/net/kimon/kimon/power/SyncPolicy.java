package net.kimon.kimon.power;

/**
 * Pure decisions for when the server sends resource state to clients, so the network cost stays low:
 * the owner gets {@link PowerState} at most every {@link #INTERVAL_TICKS} ticks and only if it
 * changed; neighbours get an {@link AuraState} only when it changes.
 */
public final class SyncPolicy {

    private SyncPolicy() {
    }

    /** Server ticks between resource syncs to the owning player. */
    public static final int INTERVAL_TICKS = 2;

    private static final double EPS = 1e-4;

    /** Whether two states are the same for sync purposes (the lock counter is server-only). */
    public static boolean sameForSync(PowerState a, PowerState b) {
        return a.charging() == b.charging()
                && a.discharging() == b.discharging()
                && a.turbo() == b.turbo()
                && a.releaseState() == b.releaseState()
                && a.form() == b.form()
                && Math.abs(a.release() - b.release()) < EPS
                && Math.abs(a.energy() - b.energy()) < EPS
                && Math.abs(a.stamina() - b.stamina()) < EPS;
    }

    /** Whether the owner's resources should be sent this tick. */
    public static boolean shouldSendResources(int tickCount, PowerState lastSent, PowerState now) {
        if (lastSent == null) {
            return true;
        }
        return tickCount % INTERVAL_TICKS == 0 && !sameForSync(lastSent, now);
    }

    /** Whether the aura should be re-broadcast to players tracking this one. */
    public static boolean shouldSendAura(AuraState lastSent, AuraState now) {
        return lastSent == null || !lastSent.equals(now);
    }
}
