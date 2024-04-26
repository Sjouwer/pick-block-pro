package io.github.sjouwer.pickblockpro.config.tools;

import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class Crossbow extends Tool {
    String item = Registries.ITEM.getId(Items.CROSSBOW).toString();
    int unbreaking = 3;
    int quick_charge = 3;
    int piercing = 4;
    boolean multishot = true;
    boolean mending = true;
}
