package io.github.sjouwer.pickblockpro.config;

import io.github.sjouwer.pickblockpro.picker.ToolPicker.Tools;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.CollapsibleObject;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.TransitiveObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityGroup;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("FieldMayBeFinal")
@Config(name = "pickblockpro/config")
public class ModConfig implements ConfigData {
    static class BlockPicker {
        @Tooltip
        private int range = 100;
        @Tooltip
        private boolean pickBlocks = true;
        @Tooltip
        private boolean pickEntities = true;
        @Tooltip
        private boolean pickFluids = false;
        @Tooltip
        private boolean pickLight = true;
    }

    static class IdPicker {
        @Tooltip
        private int range = 100;
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
    }

    static class BaseEnchantments {
        private int unbreaking = 3;
        private int efficiency = 5;
        private boolean mending = true;
    }

    static class ExtendedEnchantments {
        @TransitiveObject
        private BaseEnchantments base = new BaseEnchantments();
        @Tooltip
        private boolean silkTouch = true;
        @Tooltip
        private int fortune = 3;
    }

    static class SwordEnchantments {
        private int unbreaking = 3;
        private int sharpness = 5;
        @Tooltip
        private int bane = 5;
        @Tooltip
        private int smite = 5;
        private int sweeping = 3;
        private int looting = 3;
        private int fireAspect = 0;
        private int knockback = 0;
        private boolean mending = true;
    }

    static class ToolPicker {
        @Tooltip
        private int range = 100;
        @Tooltip
        private boolean pickFluids = false;
        @Tooltip
        private boolean preferSilkTouch = true;
        @Tooltip
        private boolean preferEfficiency = false;
        @Tooltip
        private boolean preferSwordForBamboo = true;
        @Tooltip
        private boolean enchantTools = true;
        @CollapsibleObject
        private ExtendedEnchantments pickaxeEnchantments = new ExtendedEnchantments();
        @CollapsibleObject
        private ExtendedEnchantments axeEnchantments = new ExtendedEnchantments();
        @CollapsibleObject
        private ExtendedEnchantments shovelEnchantments = new ExtendedEnchantments();
        @CollapsibleObject
        private ExtendedEnchantments hoeEnchantments = new ExtendedEnchantments();
        @CollapsibleObject
        private BaseEnchantments shearEnchantments = new BaseEnchantments();
        @CollapsibleObject
        private SwordEnchantments swordEnchantments = new SwordEnchantments();
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

    static class Inventory {
        @Tooltip
        private boolean searchThroughContainers = true;
        @CollapsibleObject(startExpanded=true) @Tooltip
        private LockedSlots lockedSlots = new LockedSlots();
    }

    @TransitiveObject @Category("block_picker_settings")
    private BlockPicker blockPicker = new BlockPicker();
    @TransitiveObject @Category("id_picker_settings")
    private IdPicker idPicker = new IdPicker();
    @TransitiveObject @Category("tool_picker_settings")
    private ToolPicker toolPicker = new ToolPicker();
    @TransitiveObject @Category("inventory_settings")
    private Inventory inventory = new Inventory();

    public int blockPickRange() {
        return blockPicker.range;
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

    public int idPickRange() {
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

    public boolean hotbarPickerEnabled() {
        return idPicker.enableHotbarPicker;
    }

    public int toolPickRange() {
        return toolPicker.range;
    }

    public boolean toolPickFluids() {
        return toolPicker.pickFluids;
    }

    public boolean preferSilkTouch() {
        return  toolPicker.preferSilkTouch;
    }

    public boolean preferEfficiency() {
        return  toolPicker.preferEfficiency;
    }

    public boolean preferSwordForBamboo() {
        return toolPicker.preferSwordForBamboo;
    }

    public Map<Enchantment, Integer> getEnchantments(Tools tool, EntityGroup eGroup) {
        if (!toolPicker.enchantTools) {
            return Collections.emptyMap();
        }

        HashMap<Enchantment, Integer> enchantments = new HashMap<>();
        switch (tool) {
            case PICKAXE -> getExtendedEnchantments(toolPicker.pickaxeEnchantments, enchantments);
            case AXE -> getExtendedEnchantments(toolPicker.axeEnchantments, enchantments);
            case SHOVEL -> getExtendedEnchantments(toolPicker.shovelEnchantments, enchantments);
            case HOE -> getExtendedEnchantments(toolPicker.hoeEnchantments, enchantments);
            case SHEARS -> getBaseEnchantments(toolPicker.shearEnchantments, enchantments);
            case SWORD -> getSwordEnchantments(toolPicker.swordEnchantments, enchantments, eGroup);
        }

        return enchantments;
    }

    private void getBaseEnchantments(BaseEnchantments base, HashMap<Enchantment, Integer> enchantments) {
        if (base.efficiency > 0) {
            enchantments.put(Enchantments.EFFICIENCY, Math.min(base.efficiency, 255));
        }
        if (base.unbreaking > 0) {
            enchantments.put(Enchantments.UNBREAKING, Math.min(base.unbreaking, 255));
        }
        if (base.mending) {
            enchantments.put(Enchantments.MENDING, 1);
        }
    }

    private void getExtendedEnchantments(ExtendedEnchantments extended, HashMap<Enchantment, Integer> enchantments) {
        getBaseEnchantments(extended.base, enchantments);
        if (extended.silkTouch && (extended.fortune <= 0 || toolPicker.preferSilkTouch)) {
            enchantments.put(Enchantments.SILK_TOUCH, 1);
        }
        else if (extended.fortune > 0) {
            enchantments.put(Enchantments.FORTUNE, Math.min(extended.fortune, 255));
        }
    }

    private void getSwordEnchantments(SwordEnchantments sword, HashMap<Enchantment, Integer> enchantments, EntityGroup eGroup) {
        if (sword.unbreaking > 0) {
            enchantments.put(Enchantments.UNBREAKING, Math.min(sword.unbreaking, 255));
        }
        if (sword.sweeping > 0) {
            enchantments.put(Enchantments.SWEEPING, Math.min(sword.sweeping, 255));
        }
        if (sword.looting > 0) {
            enchantments.put(Enchantments.LOOTING, Math.min(sword.looting, 255));
        }
        if (sword.fireAspect > 0) {
            enchantments.put(Enchantments.FIRE_ASPECT, Math.min(sword.fireAspect, 255));
        }
        if (sword.knockback > 0) {
            enchantments.put(Enchantments.KNOCKBACK, Math.min(sword.knockback, 255));
        }
        if (sword.mending) {
            enchantments.put(Enchantments.MENDING, 1);
        }
        if (sword.bane > 0 && eGroup != null && eGroup.equals(EntityGroup.ARTHROPOD)) {
            enchantments.put(Enchantments.BANE_OF_ARTHROPODS, Math.min(sword.bane, 255));
        }
        else if (sword.smite > 0 && eGroup != null && eGroup.equals(EntityGroup.UNDEAD)) {
            enchantments.put(Enchantments.SMITE, Math.min(sword.smite, 255));
        }
        else if (sword.sharpness > 0) {
            enchantments.put(Enchantments.SHARPNESS, Math.min(sword.sharpness, 255));
        }
    }

    public boolean searchContainers() {
        return inventory.searchThroughContainers;
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