package net.kimon.kimon.skill;

import net.minecraft.resources.Identifier;

/**
 * Pure rules for learning skills: what the next level costs and whether the player can afford it in
 * both TP and Mind. Mind is a budget given by the MIND attribute: it limits how many skill levels you
 * can hold at once.
 */
public final class SkillRules {

    private SkillRules() {
    }

    /** Why a learn attempt did or did not work. */
    public enum Outcome {
        LEARNED, UNKNOWN_SKILL, MAX_LEVEL, NOT_ENOUGH_TP, NOT_ENOUGH_MIND
    }

    /**
     * The result of {@link #learn}: the outcome, the (possibly updated) skills and the TP spent.
     */
    public record Result(Outcome outcome, SkillData data, long tpSpent) {
        public boolean learned() {
            return outcome == Outcome.LEARNED;
        }
    }

    /** Mind budget from the MIND attribute: {@code mind * perPoint}, rounded down. */
    public static int mindBudget(int mindAttribute, double perPoint) {
        return (int) Math.floor(Math.max(0, mindAttribute) * Math.max(0.0, perPoint));
    }

    /**
     * Tries to raise {@code id} by one level. Never partially applies: on any failure the data is
     * returned unchanged and nothing is spent.
     *
     * @param tp         the player's unspent TP
     * @param mindBudget the player's total Mind budget
     */
    public static Result learn(SkillData data, SkillCatalog catalog, Identifier id, long tp, int mindBudget) {
        SkillDef def = catalog.get(id);
        if (def == null) {
            return new Result(Outcome.UNKNOWN_SKILL, data, 0L);
        }
        int level = data.level(id);
        if (level >= def.maxLevel()) {
            return new Result(Outcome.MAX_LEVEL, data, 0L);
        }
        long cost = def.tpCostOfLevel(level + 1);
        if (tp < cost) {
            return new Result(Outcome.NOT_ENOUGH_TP, data, 0L);
        }
        if (data.mindUsed(catalog) + def.mindCostOfLevel() > mindBudget) {
            return new Result(Outcome.NOT_ENOUGH_MIND, data, 0L);
        }
        return new Result(Outcome.LEARNED, data.with(id, level + 1), cost);
    }

    /** TP the next level of {@code id} costs, or -1 if it is unknown or already maxed. */
    public static long nextCost(SkillData data, SkillCatalog catalog, Identifier id) {
        SkillDef def = catalog.get(id);
        if (def == null || data.level(id) >= def.maxLevel()) {
            return -1L;
        }
        return def.tpCostOfLevel(data.level(id) + 1);
    }
}
