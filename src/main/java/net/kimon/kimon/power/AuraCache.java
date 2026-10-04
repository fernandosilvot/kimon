package net.kimon.kimon.power;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side record of the {@link AuraState} of the players around you, keyed by entity id. Written
 * by the aura payload handler and read by whatever draws auras. Plain data, no client-only imports.
 */
public final class AuraCache {

    private static final Map<Integer, AuraState> AURAS = new ConcurrentHashMap<>();

    private AuraCache() {
    }

    public static void put(int entityId, AuraState state) {
        if (state.equals(AuraState.NONE)) {
            AURAS.remove(entityId);
        } else {
            AURAS.put(entityId, state);
        }
    }

    public static AuraState get(int entityId) {
        return AURAS.getOrDefault(entityId, AuraState.NONE);
    }

    public static void clear() {
        AURAS.clear();
    }
}
