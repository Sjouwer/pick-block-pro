package io.github.sjouwer.pickblockpro.config;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.tools.*;
import io.github.sjouwer.pickblockpro.picker.WeaponPicker.Weapons;
import io.github.sjouwer.pickblockpro.picker.ToolPicker.Tools;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.CollapsibleObject;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.RequiresRestart;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.TransitiveObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
@Config(name = PickBlockPro.NAMESPACE + "/config")
public class ModConfig implements ConfigData {
    static class BlockPicker {
        @Tooltip
        private double range = 100;
        @Tooltip
        private boolean useInteractionRange = false;
        @Tooltip
        private double creativeRange = 100;
        @Tooltip
        private boolean useCreativeInteractionRange = false;
        @Tooltip
        private boolean pickBlocks = true;
        @Tooltip
        private boolean pickEntities = true;
        @Tooltip
        private boolean pickFluids = false;
        @Tooltip
        private boolean pickLight = true;
        @Tooltip
        private boolean overrideLitematica = true;
        @Tooltip
        private String blockStateTagBlacklist = "waterlogged";
        @Tooltip
        private String blockEntityTagBlacklist = "";
        @Tooltip
        private String entityTagBlacklist = "UUID, Pos, TileX, TileY, TileZ, Facing, facing, Rotation, Leash";
    }

    static class IdPicker {
        @Tooltip
        private double range = 100;
        @Tooltip
        private boolean useInteractionRange = false;
        @Tooltip
        private boolean pickBlocks = true;
        @Tooltip
        private boolean pickEntities = true;
        @Tooltip
        private boolean pickFluids = false;
        @Tooltip
        private boolean addNamespace = false;
        @Tooltip
        private boolean addProperties = true;
        @Tooltip
        private boolean copyToClipboard = true;
        @Tooltip
        private boolean enableHotbarPicker = true;
        @Tooltip
        private boolean convertItemToBlock = true;
        @Tooltip
        private boolean enableTagPicker = true;
        @Tooltip
        private boolean enablePrettyTags = true;
        @Tooltip
        private String blockStateTagBlacklist = "";
        @Tooltip
        private String blockEntityTagBlacklist = "";
        @Tooltip
        private String entityTagBlacklist = "";
    }

    static class ToolPicker {
        @Tooltip
        private double range = 100;
        @Tooltip
        private boolean useInteractionRange = false;
        @Tooltip
        private int durabilityThreshold = 20;
        @Tooltip
        private boolean pickFluids = false;
        @Tooltip
        private boolean enchantTools = true;
        @CollapsibleObject
        private ToolSettings tools = new ToolSettings();
        @CollapsibleObject
        private WeaponSettings weapons = new WeaponSettings();
    }

    static class ToolSettings {
        @Tooltip
        private boolean preferSilkTouch = true;
        @Tooltip
        private boolean preferEfficiency = false;
        @Tooltip
        private boolean preferSwordForBamboo = true;
        @Tooltip @RequiresRestart
        private boolean addToolsToOpTab = true;
        @CollapsibleObject
        private Pickaxe pickaxe = new Pickaxe();
        @CollapsibleObject
        private Axe axe = new Axe();
        @CollapsibleObject
        private Shovel shovel = new Shovel();
        @CollapsibleObject
        private Hoe hoe = new Hoe();
        @CollapsibleObject
        private Shears shears = new Shears();
        @CollapsibleObject
        private FishingRod fishingRod = new FishingRod();
    }

    static class WeaponSettings {
        @Tooltip @RequiresRestart
        private boolean addWeaponsToOpTab = true;
        @CollapsibleObject
        private Sword sword = new Sword();
        @CollapsibleObject
        private Bow bow = new Bow();
        @CollapsibleObject
        private Crossbow crossbow = new Crossbow();
        @CollapsibleObject
        private Trident trident = new Trident();
        @CollapsibleObject
        private Mace mace = new Mace();
    }

    static class Inventory {
        @Tooltip
        private boolean searchThroughContainers = true;
        @Tooltip
        private boolean stayInSameSlot = false;
        @CollapsibleObject(startExpanded=true) @Tooltip
        private LockedSlots lockedSlots = new LockedSlots();
    }

    static class LockedSlots {
        private boolean slot1 = false;
        private boolean slot2 = false;
        private boolean slot3 = false;
        private boolean slot4 = false;
        private boolean slot5 = false;
        private boolean slot6 = false;
        private boolean slot7 = false;
        private boolean slot8 = false;
        private boolean slot9 = false;
    }

    @TransitiveObject @Category("blockPickerSettings")
    private BlockPicker blockPicker = new BlockPicker();
    @TransitiveObject @Category("idPickerSettings")
    private IdPicker idPicker = new IdPicker();
    @TransitiveObject @Category("toolPickerSettings")
    private ToolPicker toolPicker = new ToolPicker();
    @TransitiveObject @Category("inventorySettings")
    private Inventory inventory = new Inventory();

    public double blockBlockPickRange(PlayerEntity player) {
        boolean isCreative = player.isCreative();
        if (!isCreative && blockPicker.useInteractionRange || isCreative && blockPicker.useCreativeInteractionRange) {
            return player.getBlockInteractionRange();
        }
        return isCreative ? blockPicker.creativeRange : blockPicker.range;
    }

    public double entityBlockPickRange(PlayerEntity player) {
        boolean isCreative = player.isCreative();
        if (!isCreative && blockPicker.useInteractionRange || isCreative && blockPicker.useCreativeInteractionRange) {
            return player.getEntityInteractionRange();
        }
        return isCreative ? blockPicker.creativeRange : blockPicker.range;
    }

    public boolean blockPickBlocks() {
        return blockPicker.pickBlocks;
    }

    public boolean blockPickEntities() {
        return blockPicker.pickEntities;
    }

    public boolean blockPickFluids() {
        return blockPicker.pickFluids;
    }

    public boolean blockPickLight() {
        return blockPicker.pickLight;
    }

    public boolean overrideLitematica() {
        return blockPicker.overrideLitematica;
    }

    public List<String> blockStateTagBlacklist() {
        return Arrays.asList(blockPicker.blockStateTagBlacklist.split("\\s*,\\s*"));
    }

    public List<String> blockEntityTagBlacklist() {
        return Arrays.asList(blockPicker.blockEntityTagBlacklist.split("\\s*,\\s*"));
    }

    public List<String> entityTagBlacklist() {
        return Arrays.asList(blockPicker.entityTagBlacklist.split("\\s*,\\s*"));
    }

    public double blockIdPickRange(PlayerEntity player) {
        if (idPicker.useInteractionRange) {
            return player.getBlockInteractionRange();
        }
        return idPicker.range;
    }

    public double entityIdPickRange(PlayerEntity player) {
        if (idPicker.useInteractionRange) {
            return player.getEntityInteractionRange();
        }
        return idPicker.range;
    }

    public boolean idPickBlocks() {
        return idPicker.pickBlocks;
    }

    public boolean idPickEntities() {
        return idPicker.pickEntities;
    }

    public boolean idPickFluids() {
        return idPicker.pickFluids;
    }

    public boolean addNamespace() {
        return idPicker.addNamespace;
    }

    public boolean addProperties() {
        return idPicker.addProperties;
    }

    public boolean copyToClipboard() {
        return idPicker.copyToClipboard;
    }

    public boolean tagPickerEnabled() {
        return idPicker.enableTagPicker;
    }

    public boolean prettyTagEnabled() {
        return idPicker.enablePrettyTags;
    }

    public boolean hotbarPickerEnabled() {
        return idPicker.enableHotbarPicker;
    }

    public boolean convertItemToBlock() {
        return idPicker.convertItemToBlock;
    }

    public List<String> blockStateTagIdBlacklist() {
        return Arrays.asList(idPicker.blockStateTagBlacklist.split("\\s*,\\s*"));
    }

    public List<String> blockEntityTagIdBlacklist() {
        return Arrays.asList(idPicker.blockEntityTagBlacklist.split("\\s*,\\s*"));
    }

    public List<String> entityTagIdBlacklist() {
        return Arrays.asList(idPicker.entityTagBlacklist.split("\\s*,\\s*"));
    }

    public double blockToolPickRange(PlayerEntity player) {
        if (toolPicker.useInteractionRange) {
            return player.getBlockInteractionRange();
        }
        return toolPicker.range;
    }

    public double entityToolPickRange(PlayerEntity player) {
        if (toolPicker.useInteractionRange) {
            return player.getEntityInteractionRange();
        }
        return toolPicker.range;
    }

    public int durabilityThreshold() {
        return toolPicker.durabilityThreshold;
    }

    public boolean toolPickFluids() {
        return toolPicker.pickFluids;
    }

    public boolean enchantTools() { return toolPicker.enchantTools; }

    public boolean preferSilkTouch() {
        return  toolPicker.tools.preferSilkTouch;
    }

    public boolean preferEfficiency() {
        return  toolPicker.tools.preferEfficiency;
    }

    public boolean preferSwordForBamboo() {
        return toolPicker.tools.preferSwordForBamboo;
    }

    public boolean addToolsToOpTab() {
        return toolPicker.tools.addToolsToOpTab;
    }

    public ItemStack getToolItemStack(Tools tool) {
        return switch (tool) {
            case PICKAXE -> toolPicker.tools.pickaxe.getItemStack();
            case AXE -> toolPicker.tools.axe.getItemStack();
            case SHOVEL -> toolPicker.tools.shovel.getItemStack();
            case HOE -> toolPicker.tools.hoe.getItemStack();
            case SHEARS -> toolPicker.tools.shears.getItemStack();
            case BUCKET -> Items.BUCKET.getDefaultStack();
            case FISHING_ROD -> toolPicker.tools.fishingRod.getItemStack();
            case SWORD -> toolPicker.weapons.sword.getItemStack();
        };
    }

    public boolean addWeaponsToOpTab() {
        return toolPicker.weapons.addWeaponsToOpTab;
    }

    public ItemStack getWeaponItemStack(Weapons weapon) {
        return switch (weapon) {
            case SWORD -> toolPicker.weapons.sword.getItemStack();
            case AXE -> toolPicker.tools.axe.getItemStack();
            case BOW -> toolPicker.weapons.bow.getItemStack();
            case CROSSBOW -> toolPicker.weapons.crossbow.getItemStack();
            case TRIDENT -> toolPicker.weapons.trident.getItemStack();
            case MACE -> toolPicker.weapons.mace.getItemStack();
        };
    }

    public boolean searchContainers() {
        return inventory.searchThroughContainers;
    }

    public boolean stayInSameSlot() {
        return inventory.stayInSameSlot;
    }

    public boolean isSlotLocked(int slot) {
        return switch (slot) {
            case 0 -> inventory.lockedSlots.slot1;
            case 1 -> inventory.lockedSlots.slot2;
            case 2 -> inventory.lockedSlots.slot3;
            case 3 -> inventory.lockedSlots.slot4;
            case 4 -> inventory.lockedSlots.slot5;
            case 5 -> inventory.lockedSlots.slot6;
            case 6 -> inventory.lockedSlots.slot7;
            case 7 -> inventory.lockedSlots.slot8;
            case 8 -> inventory.lockedSlots.slot9;
            default -> false;
        };
    }
}