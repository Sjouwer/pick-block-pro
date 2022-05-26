package io.github.sjouwer.pickblockpro.config;

import io.github.sjouwer.pickblockpro.picker.ToolPicker;
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
@Config(name = "pick_block_pro")
public class ModConfig implements ConfigData {
    @Tooltip @Category("block_picker_settings")
    private int blockPickRange = 100;
    @Tooltip @Category("block_picker_settings")
    private boolean blockPickBlocks = true;
    @Tooltip @Category("block_picker_settings")
    private boolean blockPickEntities = true;
    @Tooltip @Category("block_picker_settings")
    private boolean blockPickFluids = false;
    @Tooltip @Category("block_picker_settings")
    private boolean blockPickFire = true;
    @Tooltip @Category("block_picker_settings")
    private boolean blockPickLight = true;

    @Tooltip @Category("id_picker_settings")
    private int idPickRange = 100;
    @Tooltip @Category("id_picker_settings")
    private boolean idPickFluids = false;
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
    private boolean toolPickFluids = false;
    @Tooltip @Category("tool_picker_settings")
    private boolean preferSilkTouch = true;
    @Tooltip @Category("tool_picker_settings")
    private boolean preferEfficiency = false;
    @Tooltip @Category("tool_picker_settings")
    private boolean preferSwordForBamboo = true;
    @Tooltip @Category("tool_picker_settings")
    private boolean enchantTools = true;

    static class BaseEnch {
        private int unbreaking = 3;
        private int efficiency = 5;
        private boolean mending = true;
    }
    static class ExtendedEnch {
        @TransitiveObject
        private BaseEnch base = new BaseEnch();
        @Tooltip
        private boolean silkTouch = true;
        @Tooltip
        private int fortune = 3;
    }
    @CollapsibleObject @Category("tool_picker_settings")
    private ExtendedEnch pickEnchantments = new ExtendedEnch();
    @CollapsibleObject @Category("tool_picker_settings")
    private ExtendedEnch axeEnchantments = new ExtendedEnch();
    @CollapsibleObject @Category("tool_picker_settings")
    private ExtendedEnch shovelEnchantments = new ExtendedEnch();
    @CollapsibleObject @Category("tool_picker_settings")
    private ExtendedEnch hoeEnchantments = new ExtendedEnch();
    @CollapsibleObject @Category("tool_picker_settings")
    private BaseEnch shearEnchantments = new BaseEnch();

    static class SwordEnch {
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
    @CollapsibleObject @Category("tool_picker_settings")
    private SwordEnch swordEnchantments = new SwordEnch();

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

    public boolean blockPickFluids() {
        return blockPickFluids;
    }

    public boolean idPickFluids() {
        return idPickFluids;
    }

    public boolean toolPickFluids() {
        return toolPickFluids;
    }

    public boolean blockPickFire() {
        return blockPickFire;
    }

    public boolean blockPickLight() {
        return blockPickLight;
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

    public Map<Enchantment, Integer> getEnchantments(ToolPicker.Tools tool, EntityGroup eGroup) {
        if (!enchantTools) {
            return Collections.emptyMap();
        }

        HashMap<Enchantment, Integer> enchantments = new HashMap<>();
        switch (tool) {
            case PICKAXE -> getExtendedEnchantments(pickEnchantments, enchantments);
            case AXE -> getExtendedEnchantments(axeEnchantments, enchantments);
            case SHOVEL -> getExtendedEnchantments(shovelEnchantments, enchantments);
            case HOE -> getExtendedEnchantments(hoeEnchantments, enchantments);
            case SHEARS -> getBaseEnchantments(shearEnchantments, enchantments);
            case SWORD -> getSwordEnchantments(swordEnchantments, enchantments, eGroup);
        }

        return enchantments;
    }

    private void getBaseEnchantments(BaseEnch base, HashMap<Enchantment, Integer> enchantments) {
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

    private void getExtendedEnchantments(ExtendedEnch extended, HashMap<Enchantment, Integer> enchantments) {
        getBaseEnchantments(extended.base, enchantments);
        if (extended.silkTouch && (extended.fortune <= 0 || preferSilkTouch)) {
            enchantments.put(Enchantments.SILK_TOUCH, 1);
        }
        else if (extended.fortune > 0) {
            enchantments.put(Enchantments.FORTUNE, Math.min(extended.fortune, 255));
        }
    }

    private void getSwordEnchantments(SwordEnch sword, HashMap<Enchantment, Integer> enchantments, EntityGroup eGroup) {
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