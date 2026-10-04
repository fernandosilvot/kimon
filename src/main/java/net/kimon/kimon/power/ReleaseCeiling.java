package net.kimon.kimon.power;

import net.kimon.kimon.config.KimonConfig;
import net.kimon.kimon.skill.ModSkillAttachments;
import net.kimon.kimon.skill.SkillCatalog;
import net.kimon.kimon.skill.SkillEffects;
import net.kimon.kimon.stats.StatCalculator;
import net.minecraft.world.entity.player.Player;

/** The Release ceiling of a player: the configured base plus Potential Unlock, capped. */
public final class ReleaseCeiling {

    private ReleaseCeiling() {
    }

    public static double of(Player player) {
        PowerParams params = KimonConfig.params();
        double bonus = SkillEffects.releaseCap(player.getData(ModSkillAttachments.SKILLS.get()), SkillCatalog.current());
        return StatCalculator.maxRelease(params.baseMaxRelease(), bonus, params.hardMaxRelease());
    }
}
