package net.kimon.kimon.skill;

import java.util.Map;

import net.minecraft.resources.Identifier;

/**
 * Pure: adds up what a player's skills do. Every skill's effects of a given type are summed at that
 * skill's level, so any skill (shipped or from a datapack) can contribute to any effect type.
 */
public final class SkillEffects {

    private SkillEffects() {
    }

    /** Total value of an effect type across all learned skills. */
    public static double total(SkillData data, SkillCatalog catalog, String type) {
        double sum = 0.0;
        for (Map.Entry<Identifier, Integer> e : data.levels().entrySet()) {
            SkillDef def = catalog.get(e.getKey());
            if (def != null) {
                sum += def.effect(type, Math.min(e.getValue(), def.maxLevel()));
            }
        }
        return sum;
    }

    /** Whether any learned skill has this effect type active. */
    public static boolean has(SkillData data, SkillCatalog catalog, String type) {
        for (Map.Entry<Identifier, Integer> e : data.levels().entrySet()) {
            SkillDef def = catalog.get(e.getKey());
            if (def == null) {
                continue;
            }
            for (SkillEffect effect : def.effects()) {
                if (effect.type().equals(type) && e.getValue() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Percent bonus to the Release ceiling (Potential Unlock). */
    public static double releaseCap(SkillData data, SkillCatalog catalog) {
        return total(data, catalog, SkillEffect.RELEASE_CAP);
    }

    /** Fraction of damage that gets through, from Endurance: 1.0 with none, never below 0. */
    public static double damageTakenFactor(SkillData data, SkillCatalog catalog) {
        return Math.max(0.0, 1.0 - total(data, catalog, SkillEffect.DAMAGE_REDUCTION) / 100.0);
    }
}
