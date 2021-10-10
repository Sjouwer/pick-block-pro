package io.github.sjouwer.pickblockpro.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.world.RaycastContext;

@Config(name = "pick_block_pro")
public class ModConfig implements ConfigData {
    @ConfigEntry.Category("block_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private int blockPickRange = 100;
    @ConfigEntry.Category("block_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean blockPickBlocks = true;
    @ConfigEntry.Category("block_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean blockPickEntities = true;
    @ConfigEntry.Category("block_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean blockPickFluids = true;
    @ConfigEntry.Category("block_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean blockPickFire = true;
    @ConfigEntry.Category("id_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private int idPickRange = 100;
    @ConfigEntry.Category("id_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean idPickFluids = true;
    @ConfigEntry.Category("id_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean idPickBlocks = true;
    @ConfigEntry.Category("id_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean idPickEntities = true;
    @ConfigEntry.Category("id_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean showNamespace = true;
    @ConfigEntry.Category("id_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean showProperties = true;
    @ConfigEntry.Category("id_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean copyToClipboard = true;
    @ConfigEntry.Category("tool_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private int toolPickRange = 100;
    @ConfigEntry.Category("tool_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean preferSilkTouch = true;
    @ConfigEntry.Category("tool_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean preferEfficiency = false;
    @ConfigEntry.Category("tool_picker_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean preferSwordForBamboo = true;
    @ConfigEntry.Category("inventory_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean lockSlot1 = false;
    @ConfigEntry.Category("inventory_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean lockSlot2 = false;
    @ConfigEntry.Category("inventory_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean lockSlot3 = false;
    @ConfigEntry.Category("inventory_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean lockSlot4 = false;
    @ConfigEntry.Category("inventory_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean lockSlot5 = false;
    @ConfigEntry.Category("inventory_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean lockSlot6 = false;
    @ConfigEntry.Category("inventory_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean lockSlot7 = false;
    @ConfigEntry.Category("inventory_settings")
    @ConfigEntry.Gui.Tooltip
    private boolean lockSlot8 = false;
    @ConfigEntry.Category("inventory_settings")
    @ConfigEntry.Gui.Tooltip
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

    public boolean showNamespace() {
        return showNamespace;
    }

    public boolean showProperties() {
        return showProperties;
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