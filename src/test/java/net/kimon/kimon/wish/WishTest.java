package net.kimon.kimon.wish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Wish reward table")
class WishTest {

    @Test
    @DisplayName("every wish grants something (Power or TP)")
    void everyWishGrantsSomething() {
        for (Wish w : Wish.VALUES) {
            assertTrue(w.powerReward() > 0 || w.tpReward() > 0, w.key() + " grants nothing");
        }
    }

    @Test
    @DisplayName("fromRoll is stable and in range")
    void fromRollInRange() {
        for (int roll = 0; roll < 100; roll++) {
            Wish w = Wish.fromRoll(roll);
            assertTrue(w != null);
        }
        // Same roll → same wish (deterministic).
        assertSame(Wish.fromRoll(7), Wish.fromRoll(7));
    }

    @Test
    @DisplayName("fromRoll handles negative rolls without throwing")
    void fromRollNegative() {
        assertSame(Wish.VALUES[0], Wish.fromRoll(0));
        // floorMod keeps it in range for negatives too.
        Wish w = Wish.fromRoll(-3);
        assertTrue(w != null);
    }

    @Test
    @DisplayName("rolling across the range can reach every wish")
    void coversAllWishes() {
        Set<Wish> seen = EnumSet.noneOf(Wish.class);
        for (int roll = 0; roll < Wish.VALUES.length; roll++) {
            seen.add(Wish.fromRoll(roll));
        }
        assertEquals(Wish.VALUES.length, seen.size());
    }

    @Test
    @DisplayName("great variants out-reward their base variants")
    void greatVariantsStronger() {
        assertTrue(Wish.GREAT_TRAINING_BOON.tpReward() > Wish.TRAINING_BOON.tpReward());
        assertTrue(Wish.GREAT_POWER_SURGE.powerReward() > Wish.POWER_SURGE.powerReward());
    }
}
