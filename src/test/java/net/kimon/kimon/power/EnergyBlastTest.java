package net.kimon.kimon.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.kimon.kimon.stats.Attribute;
import net.kimon.kimon.stats.CharacterProfile;
import net.kimon.kimon.stats.PlayerClass;
import net.kimon.kimon.stats.Race;
import net.kimon.kimon.stats.StatBlock;

@DisplayName("EnergyBlast damage/cost rules")
class EnergyBlastTest {

    private static StatBlock withEnergy(int value) {
        return StatBlock.of(Map.of(Attribute.ENERGY, value), 0);
    }

    @Test
    @DisplayName("canFire requires at least the energy cost")
    void canFireThreshold() {
        assertFalse(EnergyBlast.canFire(EnergyBlast.ENERGY_COST - 0.01));
        assertTrue(EnergyBlast.canFire(EnergyBlast.ENERGY_COST));
    }

    @Test
    @DisplayName("damage is at least the base, even at 0% release and start energy")
    void baseDamageFloor() {
        double dmg = EnergyBlast.damage(StatBlock.initial(), null, 0.0);
        assertEquals(EnergyBlast.BASE_DAMAGE, dmg, 1e-9);
    }

    @Test
    @DisplayName("the Energy-scaled part is gated by Release")
    void energyPartScalesWithRelease() {
        StatBlock stats = withEnergy(StatBlock.START_VALUE + 100);
        double atZero = EnergyBlast.damage(stats, null, 0.0);
        double atHalf = EnergyBlast.damage(stats, null, 0.5);
        double atFull = EnergyBlast.damage(stats, null, 1.0);

        assertEquals(EnergyBlast.BASE_DAMAGE, atZero, 1e-9);
        assertTrue(atHalf > atZero);
        assertTrue(atFull > atHalf);
    }

    @Test
    @DisplayName("more Energy attribute means more damage at full release")
    void moreEnergyMoreDamage() {
        double low = EnergyBlast.damage(withEnergy(StatBlock.START_VALUE + 10), null, 1.0);
        double high = EnergyBlast.damage(withEnergy(StatBlock.START_VALUE + 200), null, 1.0);
        assertTrue(high > low);
    }

    @Test
    @DisplayName("Sage/Channeler energy build out-damages a plain Human for the same stats")
    void profileBoostsBlast() {
        StatBlock stats = withEnergy(StatBlock.START_VALUE + 100);
        CharacterProfile human = new CharacterProfile(Race.HUMAN, PlayerClass.BRAWLER);
        CharacterProfile sage = new CharacterProfile(Race.SAGE, PlayerClass.CHANNELER);
        assertTrue(EnergyBlast.damage(stats, sage, 1.0) > EnergyBlast.damage(stats, human, 1.0));
    }
}
