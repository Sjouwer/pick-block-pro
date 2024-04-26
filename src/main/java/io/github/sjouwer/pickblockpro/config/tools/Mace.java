package io.github.sjouwer.pickblockpro.config.tools;

import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class Mace extends Tool {
    String item = Registries.ITEM.getId(Items.MACE).toString();
    int unbreaking = 3;
    int density = 5;
    int breach = 4;
    int wind_burst = 3;
    int fire_aspect = 0;
    @Tooltip
    int bane_of_arthropods = 5;
    @Tooltip
    int smite = 5;
    boolean mending = true;
}
