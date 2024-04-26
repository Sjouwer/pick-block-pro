package io.github.sjouwer.pickblockpro.config.tools;

import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class Pickaxe extends Tool {
    String item = Registries.ITEM.getId(Items.NETHERITE_PICKAXE).toString();
    int unbreaking = 3;
    int efficiency = 5;
    @Tooltip
    int fortune = 3;
    @Tooltip
    boolean silk_touch = true;
    boolean mending = true;
}
