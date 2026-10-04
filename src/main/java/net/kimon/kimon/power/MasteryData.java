package net.kimon.kimon.power;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-player Form Mastery: how practiced the player is with each {@link Form}. Mastery rises while
 * the form is active and makes it stronger (bigger damage multiplier) and cheaper (less Energy
 * drain). Pure data — no Minecraft types — so the balance is unit-testable.
 *
 * <p>Mastery is a level in {@code [0, MAX_LEVEL]} stored per form. The design (forms improve with
 * use, with diminishing returns) is adapted from the research in {@code docs/DESIGN.md}; numbers are
 * original to Kimon.</p>
 */
public final class MasteryData {

    /** Maximum mastery level per form. */
    public static final int MAX_LEVEL = 50;

    /** Mastery points gained per second while a form is active (before reaching the cap). */
    public static final double GAIN_PER_SECOND = 0.5;

    /** Extra damage multiplier added at full mastery (so Zenith ×3 becomes up to ×3.75). */
    public static final double MAX_DAMAGE_BONUS = 0.25;

    /** Fraction of the form's Energy drain removed at full mastery (up to 40% cheaper). */
    public static final double MAX_DRAIN_REDUCTION = 0.40;

    /** Internal: mastery stored as a double per form for smooth accumulation; exposed as a level. */
    private final Map<Form, Double> points;

    private MasteryData(Map<Form, Double> points) {
        this.points = points;
    }

    /** A fresh player: zero mastery in every form. */
    public static MasteryData initial() {
        return new MasteryData(new HashMap<>());
    }

    /** Build from explicit integer levels (used by serialization). */
    public static MasteryData of(Map<Form, Integer> levels) {
        HashMap<Form, Double> map = new HashMap<>();
        for (Map.Entry<Form, Integer> e : levels.entrySet()) {
            map.put(e.getKey(), (double) clampLevel(e.getValue()));
        }
        return new MasteryData(map);
    }

    /** Current mastery level (0..MAX_LEVEL) for a form. */
    public int level(Form form) {
        return clampLevel((int) Math.floor(points.getOrDefault(form, 0.0)));
    }

    /** Mastery as a 0..1 fraction of the cap. */
    public double fraction(Form form) {
        return level(form) / (double) MAX_LEVEL;
    }

    /**
     * Returns a copy with {@code dt} seconds of practice added to {@code form}. BASE never gains
     * mastery (there's nothing to master). Clamped at the cap.
     */
    public MasteryData practice(Form form, double dt) {
        if (form.isBase() || dt <= 0) {
            return this;
        }
        double current = points.getOrDefault(form, 0.0);
        if (current >= MAX_LEVEL) {
            return this;
        }
        HashMap<Form, Double> next = new HashMap<>(points);
        next.put(form, Math.min(MAX_LEVEL, current + GAIN_PER_SECOND * dt));
        return new MasteryData(next);
    }

    /** Extra amount added to each of the form's attribute multipliers at the current mastery. */
    public double damageBonus(Form form) {
        return MAX_DAMAGE_BONUS * fraction(form);
    }

    /** The headline multiplier of a form including its mastery bonus (what the form is "worth"). */
    public double effectiveDamageMultiplier(Form form) {
        return form.damageMultiplier() + damageBonus(form);
    }

    /** The effective Energy drain for a form, reduced by mastery. */
    public double effectiveDrain(Form form) {
        return form.energyDrainPerSecond() * (1.0 - MAX_DRAIN_REDUCTION * fraction(form));
    }

    /** Immutable snapshot of levels (only forms with at least one level). */
    public Map<Form, Integer> asLevelMap() {
        Map<Form, Integer> out = new HashMap<>();
        for (Form f : points.keySet()) {
            int lvl = level(f);
            if (lvl > 0) {
                out.put(f, lvl);
            }
        }
        return out;
    }

    private static int clampLevel(int v) {
        if (v < 0) {
            return 0;
        }
        return Math.min(v, MAX_LEVEL);
    }
}
