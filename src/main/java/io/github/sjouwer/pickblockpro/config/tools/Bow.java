package io.github.sjouwer.pickblockpro.config.tools;

import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class Bow extends Tool {
    @Tooltip
    String item = Registries.ITEM.getId(Items.BOW).toString();
    int unbreaking = 3;
    int power = 5;
    int punch = 2;
    boolean flame = true;
    boolean infinity = true;
    boolean mending = true;
}
