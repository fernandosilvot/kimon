package net.kimon.kimon.power;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.kimon.kimon.skill.SkillCatalog;
import net.kimon.kimon.skill.SkillData;
import net.minecraft.resources.Identifier;

/**
 * Pure rules for transformations: which forms a race has, in what order, and whether a player can
 * enter one. A race's forms form a ladder sorted by {@link FormDef#order()}; going up moves to the next
 * rung (if the player has the racial skill level and the Release), going down to the previous one.
 */
public final class FormRules {

    private FormRules() {
    }

    /** Why a transformation can or cannot happen. */
    public enum Check {
        OK, NO_SUCH_FORM, WRONG_RACE, SKILL_LOCKED, NEEDS_RELEASE, ALREADY_TOP
    }

    /** The race's forms, lowest order first. */
    public static List<Form> ladder(FormCatalog catalog, Identifier raceId) {
        List<Map.Entry<Identifier, FormDef>> entries = new ArrayList<>();
        for (Map.Entry<Identifier, FormDef> e : catalog.forms().entrySet()) {
            if (e.getValue().allowsRace(raceId)) {
                entries.add(e);
            }
        }
        entries.sort(Comparator.<Map.Entry<Identifier, FormDef>>comparingInt(e -> e.getValue().order())
                .thenComparing(Map.Entry::getKey));
        List<Form> ladder = new ArrayList<>();
        for (Map.Entry<Identifier, FormDef> e : entries) {
            ladder.add(new Form(e.getKey()));
        }
        return ladder;
    }

    /** Whether the player's racial skill level opens the form (forms with no requirement are open). */
    public static boolean unlocked(FormDef def, SkillData skills) {
        return def.skill() == null || skills.level(def.skill()) >= def.skillLevel();
    }

    /** The form one rung above {@code current} on the ladder, or null if it is already the top. */
    public static Form next(List<Form> ladder, Form current) {
        int index = ladder.indexOf(current);
        int target = index + 1; // Base (or a form not on the ladder) is index -1
        return target < ladder.size() ? ladder.get(target) : null;
    }

    /** The form one rung below {@code current}: Base from the first rung (or if it is not on the ladder). */
    public static Form previous(List<Form> ladder, Form current) {
        int index = ladder.indexOf(current);
        return index > 0 ? ladder.get(index - 1) : Form.BASE;
    }

    /** Whether the player can enter {@code target} right now. */
    public static Check canEnter(FormCatalog catalog, Identifier raceId, SkillData skills, Form target,
                                 double release) {
        if (target.isBase()) {
            return Check.OK;
        }
        FormDef def = catalog.get(target.id());
        if (def == null) {
            return Check.NO_SUCH_FORM;
        }
        if (!def.allowsRace(raceId)) {
            return Check.WRONG_RACE;
        }
        if (!unlocked(def, skills)) {
            return Check.SKILL_LOCKED;
        }
        if (release < def.minRelease()) {
            return Check.NEEDS_RELEASE;
        }
        return Check.OK;
    }

    /** The highest form on the ladder the player could hold at this Release (Base if none). */
    public static Form highestAvailable(FormCatalog catalog, Identifier raceId, SkillData skills, double release) {
        Form best = Form.BASE;
        for (Form f : ladder(catalog, raceId)) {
            if (canEnter(catalog, raceId, skills, f, release) == Check.OK) {
                best = f;
            }
        }
        return best;
    }

    /** Whether a form the player is in is still valid for their race (e.g. after changing race). */
    public static boolean validFor(FormCatalog catalog, Identifier raceId, Form form) {
        if (form.isBase()) {
            return true;
        }
        FormDef def = catalog.get(form.id());
        return def != null && def.allowsRace(raceId);
    }

    /** The racial skill ids used by the shipped forms, for convenience. */
    public static final Identifier SUPER_FORM = SkillCatalog.id("super_form");
}
