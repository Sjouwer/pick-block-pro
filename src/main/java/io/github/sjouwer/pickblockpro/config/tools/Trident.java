package io.github.sjouwer.pickblockpro.config.tools;

import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class Trident extends Tool {
    @Tooltip
    String item = Registries.ITEM.getId(Items.TRIDENT).toString();
    int unbreaking = 3;
    int impaling = 5;
    int riptide = 3;
    int loyalty = 3;
    boolean channeling = true;
    boolean mending = true;
}
