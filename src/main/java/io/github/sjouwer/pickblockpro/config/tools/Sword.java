package io.github.sjouwer.pickblockpro.config.tools;

import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class Sword extends Tool {
    @Tooltip
    String item = Registries.ITEM.getId(Items.NETHERITE_SWORD).toString();
    int unbreaking = 3;
    int sharpness = 5;
    int sweeping_edge = 3;
    int looting = 3;
    int knockback = 0;
    int fire_aspect = 0;
    @Tooltip
    int bane_of_arthropods = 5;
    @Tooltip
    int smite = 5;
    boolean mending = true;
}
