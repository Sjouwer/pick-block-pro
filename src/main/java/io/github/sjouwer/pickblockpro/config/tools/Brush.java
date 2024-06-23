package io.github.sjouwer.pickblockpro.config.tools;

import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class Brush extends Tool {
    @Tooltip
    String item = Registries.ITEM.getId(Items.BRUSH).toString();
    int unbreaking = 3;
    boolean mending = true;
}
