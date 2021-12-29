package io.github.sjouwer.pickblockpro.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import net.minecraft.world.RaycastContext;

@Config(name = "pick_block_pro")
public class ModConfig implements ConfigData {
    @Tooltip @Category("block_picker_settings")
    private int blockPickRange = 100;
    @Tooltip @Category("block_picker_settings")
    private boolean blockPickBlocks = true;
    @Tooltip @Category("block_picker_settings")
    private boolean blockPickEntities = true;
    @Tooltip @Category("block_picker_settings")
    private boolean blockPickFluids = true;
    @Tooltip @Category("block_picker_settings")
    private boolean blockPickFire = true;

    @Tooltip @Category("id_picker_settings")
    private int idPickRange = 100;
    @Tooltip @Category("id_picker_settings")
    private boolean idPickFluids = true;
    @Tooltip @Category("id_picker_settings")
    private boolean idPickBlocks = true;
    @Tooltip @Category("id_picker_settings")
    private boolean idPickEntities = true;
    @Tooltip @Category("id_picker_settings")
    private boolean addNamespace = false;
    @Tooltip @Category("id_picker_settings")
    private boolean addProperties = true;
    @Tooltip @Category("id_picker_settings")
    private boolean copyToClipboard = true;

    @Tooltip @Category("tool_picker_settings")
    private int toolPickRange = 100;
    @Tooltip @Category("tool_picker_settings")
    private boolean preferSilkTouch = true;
    @Tooltip @Category("tool_picker_settings")
    private boolean preferEfficiency = false;
    @Tooltip @Category("tool_picker_settings")
    private boolean preferSwordForBamboo = true;
    @Tooltip @Category("tool_picker_settings")
    private boolean enchantTools = true;

    @Tooltip @Category("inventory_settings")
    private boolean lockSlot1 = false;
    @Tooltip @Category("inventory_settings")
    private boolean lockSlot2 = false;
    @Tooltip @Category("inventory_settings")
    private boolean lockSlot3 = false;
    @Tooltip @Category("inventory_settings")
    private boolean lockSlot4 = false;
    @Tooltip @Category("inventory_settings")
    private boolean lockSlot5 = false;
    @Tooltip @Category("inventory_settings")
    private boolean lockSlot6 = false;
    @Tooltip @Category("inventory_settings")
    private boolean lockSlot7 = false;
    @Tooltip @Category("inventory_settings")
    private boolean lockSlot8 = false;
    @Tooltip @Category("inventory_settings")
    private boolean lockSlot9 = false;

    public int blockPickRange() {
        return blockPickRange;
    }

    public int idPickRange() {
        return idPickRange;
    }

    public int toolPickRange() {
        return toolPickRange;
    }

    public RaycastContext.FluidHandling blockFluidHandling() {
        return fluidHandling(blockPickFluids);
    }

    public RaycastContext.FluidHandling idFluidHandling() {
        return fluidHandling(idPickFluids);
    }

    private RaycastContext.FluidHandling fluidHandling(boolean snipeFluids) {
        if (snipeFluids) {
            return RaycastContext.FluidHandling.ANY;
        }
        return RaycastContext.FluidHandling.NONE;
    }

    public boolean blockPickFire() {
        return blockPickFire;
    }

    public boolean blockPickBlocks() {
        return blockPickBlocks;
    }

    public boolean idPickBlocks() {
        return idPickBlocks;
    }

    public boolean blockPickEntities() {
        return blockPickEntities;
    }

    public boolean idPickEntities() {
        return idPickEntities;
    }

    public boolean addNamespace() {
        return addNamespace;
    }

    public boolean addProperties() {
        return addProperties;
    }

    public boolean copyToClipboard() {
        return copyToClipboard;
    }

    public boolean preferSilkTouch() {
        return  preferSilkTouch;
    }

    public boolean preferEfficiency() {
        return  preferEfficiency;
    }

    public boolean preferSwordForBamboo() {
        return preferSwordForBamboo;
    }

    public boolean enchantTools() {
        return enchantTools;
    }

    public boolean isSlotLocked(int slot) {
        return switch (slot) {
            case 0 -> lockSlot1;
            case 1 -> lockSlot2;
            case 2 -> lockSlot3;
            case 3 -> lockSlot4;
            case 4 -> lockSlot5;
            case 5 -> lockSlot6;
            case 6 -> lockSlot7;
            case 7 -> lockSlot8;
            case 8 -> lockSlot9;
            default -> false;
        };
    }
}