package net.kimon.kimon.power;

/**
 * What the active form (plus its mastery) does to the three attributes forms touch. Pure.
 *
 * <p>The pipeline from the design research: {@code effective = max(attribute × multiplier,
 * attribute + flat)}. The flat bonus makes a form worth something even to a low-level character,
 * whose multiplied value would barely change.</p>
 *
 * @param strMult multiplier on STRENGTH
 * @param dexMult multiplier on DEXTERITY
 * @param wilMult multiplier on WILLPOWER
 * @param flat    attribute points a form guarantees
 */
public record FormEffect(double strMult, double dexMult, double wilMult, double flat) {

    /** No form: every attribute is unchanged. */
    public static final FormEffect NONE = new FormEffect(1.0, 1.0, 1.0, 0.0);

    /**
     * The effect of a form definition at a mastery bonus (added to each multiplier). Base stays neutral.
     */
    public static FormEffect of(FormDef def, double masteryBonus) {
        if (def == FormDef.BASE) {
            return NONE;
        }
        double bonus = Math.max(0.0, masteryBonus);
        return new FormEffect(def.strMult() + bonus, def.dexMult() + bonus, def.wilMult() + bonus, def.flatBonus());
    }

    /** {@code max(value × multiplier, value + flat)}. */
    public static double effective(double value, double multiplier, double flat) {
        return Math.max(value * multiplier, value + flat);
    }

    public double strength(int value) {
        return effective(value, strMult, flat);
    }

    public double dexterity(int value) {
        return effective(value, dexMult, flat);
    }

    public double willpower(int value) {
        return effective(value, wilMult, flat);
    }
}
