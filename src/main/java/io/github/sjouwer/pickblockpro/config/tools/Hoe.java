package io.github.sjouwer.pickblockpro.config.tools;

import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class Hoe extends Tool {
    @Tooltip
    String item = Registries.ITEM.getId(Items.NETHERITE_HOE).toString();
    int unbreaking = 3;
    int efficiency = 5;
    @Tooltip
    int fortune = 3;
    @Tooltip
    boolean silk_touch = true;
    boolean mending = true;
}
