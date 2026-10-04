package net.kimon.kimon.training;

import net.minecraft.world.item.Item;

/**
 * A piece of training weight. While it is in a player's inventory its {@link #weight()} adds to the
 * carried weight (see {@link TrainingEffects}): it lowers melee damage but makes TP easier to earn.
 *
 * <p>Carrying counts for now; wearing them in dedicated slots comes with the equipment screen.</p>
 */
public class WeightItem extends Item {

    public static final double WRAPS_WEIGHT = 10.0;
    public static final double VEST_WEIGHT = 25.0;
    public static final double PLATES_WEIGHT = 50.0;

    private final double weight;

    public WeightItem(Item.Properties properties, double weight) {
        super(properties);
        this.weight = weight;
    }

    /** Weight points this item adds while carried. */
    public double weight() {
        return weight;
    }
}
