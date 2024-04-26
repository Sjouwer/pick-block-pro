package io.github.sjouwer.pickblockpro.config.tools;

import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class Shears extends Tool {
    String item = Registries.ITEM.getId(Items.SHEARS).toString();
    int unbreaking = 3;
    int efficiency = 5;
    boolean mending = true;
}
