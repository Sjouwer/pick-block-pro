package io.github.sjouwer.pickblockpro.config.tools;

import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class Axe extends Tool {
    @Tooltip
    String item = Registries.ITEM.getId(Items.NETHERITE_AXE).toString();
    int unbreaking = 3;
    int efficiency = 5;
    @Tooltip
    int fortune = 3;
    @Tooltip
    boolean silk_touch = true;
    int sharpness = 5;
    @Tooltip
    int bane_of_arthropods = 5;
    @Tooltip
    int smite = 5;
    boolean mending = true;
}
