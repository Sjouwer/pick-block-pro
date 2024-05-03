package io.github.sjouwer.pickblockpro.config.tools;

import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class FishingRod extends Tool {
    @Tooltip
    String item = Registries.ITEM.getId(Items.FISHING_ROD).toString();
    int unbreaking = 3;
    int lure = 3;
    int luck_of_the_sea = 3;
    boolean mending = true;
}
