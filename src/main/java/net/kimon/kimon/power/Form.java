package net.kimon.kimon.power;

import java.util.Locale;

import net.minecraft.resources.Identifier;

/**
 * A reference to a transformation by id. The numbers behind it ({@link FormDef}) live in the form
 * catalog (datapacks); this class only names one, so a form can be added without code. {@link #BASE}
 * is "no transformation".
 *
 * <p>The convenience accessors read the catalog currently in use.</p>
 *
 * @param id the form's id, e.g. {@code kimon:super_saiyan}
 */
public record Form(Identifier id) {

    public static final String NAMESPACE = "kimon";

    public static final Form BASE = of("base");
    public static final Form SUPER_SAIYAN = of("super_saiyan");
    public static final Form SUPER_SAIYAN_2 = of("super_saiyan_2");
    public static final Form SUPER_SAIYAN_3 = of("super_saiyan_3");

    public static Form of(String path) {
        return new Form(Identifier.fromNamespaceAndPath(NAMESPACE, path));
    }

    public boolean isBase() {
        return BASE.id.equals(id);
    }

    /** The id's path, e.g. {@code super_saiyan}. */
    public String key() {
        return id.getPath();
    }

    /** The form's numbers from the current catalog; neutral numbers for Base or an unknown form. */
    public FormDef def() {
        return isBase() ? FormDef.BASE : FormCatalog.current().formOrBase(id);
    }

    /** Ki drained per second to stay in this form. */
    public double energyDrainPerSecond() {
        return def().kiPerSecond();
    }

    /** Release % needed to enter and to stay in this form. */
    public double requiredRelease() {
        return def().minRelease();
    }

    /** Headline multiplier of the form (the largest attribute multiplier). */
    public double damageMultiplier() {
        return def().headlineMultiplier();
    }

    /**
     * Finds a form by name: a full id, a bare name (taken as {@code kimon:}), spaces as underscores,
     * and the names forms had before ({@code surge}, {@code ascent}, {@code zenith}). Returns null if
     * the text is not a valid id (it may still not exist in the catalog).
     */
    public static Form byKey(String text) {
        Identifier id = FormCatalog.parseId(text);
        return id == null ? null : new Form(id);
    }

    /** Safe parse for stored/synced text; anything unreadable is Base. */
    public static Form fromStored(String text) {
        Form form = byKey(text);
        return form == null ? BASE : form;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    static String normalize(String text) {
        return text.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
    }
}
