package net.kimon.kimon.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import net.minecraft.resources.Identifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Form Mastery")
class MasteryDataTest {

    @Test
    @DisplayName("a fresh player has zero mastery everywhere")
    void initialZero() {
        MasteryData m = MasteryData.initial();
        for (Identifier id : FormCatalog.current().forms().keySet()) {
            assertEquals(0, m.level(new Form(id)));
        }
        assertEquals(0, m.level(Form.BASE));
    }

    @Test
    @DisplayName("practising a form raises its mastery over time")
    void practiceRaisesMastery() {
        MasteryData m = MasteryData.initial();
        // 10 seconds at 0.5/s = level 5.
        for (int i = 0; i < 200; i++) {
            m = m.practice(Form.SUPER_SAIYAN, 0.05);
        }
        assertEquals(5, m.level(Form.SUPER_SAIYAN));
    }

    @Test
    @DisplayName("BASE never gains mastery")
    void baseNeverGains() {
        MasteryData m = MasteryData.initial().practice(Form.BASE, 100.0);
        assertEquals(0, m.level(Form.BASE));
    }

    @Test
    @DisplayName("mastery is capped at MAX_LEVEL")
    void cappedAtMax() {
        MasteryData m = MasteryData.of(Map.of(Form.SUPER_SAIYAN_3, MasteryData.MAX_LEVEL + 100));
        assertEquals(MasteryData.MAX_LEVEL, m.level(Form.SUPER_SAIYAN_3));
        // Practising at the cap is a no-op (returns same instance).
        assertSame(m, m.practice(Form.SUPER_SAIYAN_3, 10.0));
    }

    @Test
    @DisplayName("practising only affects the practised form")
    void onlyAffectsPractisedForm() {
        MasteryData m = MasteryData.initial().practice(Form.SUPER_SAIYAN, 10.0);
        assertTrue(m.level(Form.SUPER_SAIYAN) > 0);
        assertEquals(0, m.level(Form.SUPER_SAIYAN_2));
        assertEquals(0, m.level(Form.SUPER_SAIYAN_3));
    }

    @Test
    @DisplayName("mastery raises the effective damage multiplier up to the bonus cap")
    void masteryBoostsDamage() {
        MasteryData none = MasteryData.initial();
        MasteryData full = MasteryData.of(Map.of(Form.SUPER_SAIYAN_3, MasteryData.MAX_LEVEL));

        assertEquals(Form.SUPER_SAIYAN_3.damageMultiplier(), none.effectiveDamageMultiplier(Form.SUPER_SAIYAN_3), 1e-9);
        assertEquals(Form.SUPER_SAIYAN_3.damageMultiplier() + MasteryData.MAX_DAMAGE_BONUS,
                full.effectiveDamageMultiplier(Form.SUPER_SAIYAN_3), 1e-9);
    }

    @Test
    @DisplayName("mastery reduces the effective Energy drain up to the reduction cap")
    void masteryReducesDrain() {
        MasteryData none = MasteryData.initial();
        MasteryData full = MasteryData.of(Map.of(Form.SUPER_SAIYAN_2, MasteryData.MAX_LEVEL));

        assertEquals(Form.SUPER_SAIYAN_2.energyDrainPerSecond(), none.effectiveDrain(Form.SUPER_SAIYAN_2), 1e-9);
        assertEquals(Form.SUPER_SAIYAN_2.energyDrainPerSecond() * (1.0 - MasteryData.MAX_DRAIN_REDUCTION),
                full.effectiveDrain(Form.SUPER_SAIYAN_2), 1e-9);
    }

    @Test
    @DisplayName("asLevelMap round-trips through of()")
    void roundTrip() {
        MasteryData m = MasteryData.of(Map.of(Form.SUPER_SAIYAN, 7, Form.SUPER_SAIYAN_3, 20));
        MasteryData copy = MasteryData.of(m.asLevelMap());
        assertEquals(7, copy.level(Form.SUPER_SAIYAN));
        assertEquals(20, copy.level(Form.SUPER_SAIYAN_3));
        assertEquals(0, copy.level(Form.SUPER_SAIYAN_2));
    }

    @Test
    @DisplayName("saves from before forms were data (surge/ascent/zenith) load onto the Super Saiyan forms")
    void legacySaves() {
        var json = com.google.gson.JsonParser.parseString("{\"surge\":12,\"ascent\":5,\"zenith\":40}");
        MasteryData m = MasteryCodecs.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(12, m.level(Form.SUPER_SAIYAN));
        assertEquals(5, m.level(Form.SUPER_SAIYAN_2));
        assertEquals(40, m.level(Form.SUPER_SAIYAN_3));
    }

    @Test
    @DisplayName("mastery of any form (including datapack ones) round-trips through the codec and the network")
    void newFormatRoundTrips() {
        Form custom = new Form(Identifier.fromNamespaceAndPath("mypack", "rage"));
        MasteryData m = MasteryData.of(Map.of(Form.SUPER_SAIYAN, 9, custom, 33));
        MasteryData viaCodec = MasteryCodecs.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,
                MasteryCodecs.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, m).getOrThrow()).getOrThrow();
        assertEquals(9, viaCodec.level(Form.SUPER_SAIYAN));
        assertEquals(33, viaCodec.level(custom));

        var buf = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),
                net.minecraft.core.RegistryAccess.EMPTY);
        MasteryCodecs.STREAM_CODEC.encode(buf, m);
        MasteryData viaNet = MasteryCodecs.STREAM_CODEC.decode(buf);
        assertEquals(9, viaNet.level(Form.SUPER_SAIYAN));
        assertEquals(33, viaNet.level(custom));
    }

    @Test
    @DisplayName("mastery bonus is added to the form's multipliers, never to Base")
    void damageBonusScales() {
        MasteryData full = MasteryData.of(Map.of(Form.SUPER_SAIYAN, MasteryData.MAX_LEVEL));
        assertEquals(MasteryData.MAX_DAMAGE_BONUS, full.damageBonus(Form.SUPER_SAIYAN), 1e-9);
        assertEquals(0.0, full.damageBonus(Form.BASE), 1e-9);
        assertEquals(0.0, MasteryData.initial().damageBonus(Form.SUPER_SAIYAN), 1e-9);
    }
}
