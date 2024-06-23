package io.github.sjouwer.pickblockpro.config.tools;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.util.EnchantmentUtil;
import io.github.sjouwer.pickblockpro.util.InfoProvider;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Predicate;

//Field stuff needed to get it to work with Auto Config without needing to use a lot of duplicate code
public class Tool {
    public ItemEnchantmentsComponent getEnchantments(boolean allowIncompatibleEnchantments, EntityType<?> entity) {
        ItemEnchantmentsComponent.Builder enchantments = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        Field[] fields = this.getClass().getDeclaredFields();
        for(Field field: fields) {
            try {
                RegistryEntry<Enchantment> enchantment = EnchantmentUtil.getRegistryEntry(field.getName());
                if (enchantment == null) continue;

                if (field.getType().equals(int.class)) {
                    int level = Math.min(field.getInt(this), allowIncompatibleEnchantments ? 255 : enchantment.value().getMaxLevel());
                    enchantments.add(enchantment, level);
                }
                if (field.getType().equals(boolean.class) && field.getBoolean(this)) {
                    enchantments.add(enchantment, 1);
                }
            }
            catch (Exception e) {
                InfoProvider.sendError(Text.literal("Issue setting " + field.getName() + " enchantment"));
                e.printStackTrace();
            }
        }

        if(!allowIncompatibleEnchantments) {
            filterEnchantments(enchantments, entity);
        }

        return enchantments.build();
    }

    public Item getItem() {
        try {
            Field field = this.getClass().getDeclaredField("item");
            String itemId = (String) field.get(this);
            Identifier identifier = Identifier.tryParse(itemId);
            if(identifier == null) {
                InfoProvider.sendWarning(Text.literal("Failed to parse Item ID: " + itemId));
                return Items.AIR;
            }

            Item item = Registries.ITEM.get(identifier);
            if(item.equals(Items.AIR)) {
                InfoProvider.sendWarning(Text.literal("Unknown Item ID: " + itemId));
            }

            return item;
        } catch (Exception e) {
            InfoProvider.sendError(Text.literal("Pick Block Pro config error"));
            e.printStackTrace();
            return Items.AIR;
        }
    }

    public ItemStack getItemStack(boolean enchantItem, boolean allowIncompatibleEnchantments) {
        return getItemStack(enchantItem, allowIncompatibleEnchantments, null);
    }

    public ItemStack getItemStack(boolean enchantItem, boolean allowIncompatibleEnchantments, EntityType<?> entity) {
        ItemStack toolStack = getItem().getDefaultStack();

        if (enchantItem) {
            ItemEnchantmentsComponent enchantments = getEnchantments(allowIncompatibleEnchantments, entity);
            EnchantmentHelper.set(toolStack, enchantments);
        }

        return toolStack;
    }

    private void filterEnchantments(ItemEnchantmentsComponent.Builder enchantments, EntityType<?> entity) {
        if (entity != null && entity.isIn(EntityTypeTags.UNDEAD) && enchantments.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.SMITE)) > 0) {
            enchantments.remove(containsAny(
                    Enchantments.SHARPNESS,
                    Enchantments.BANE_OF_ARTHROPODS,
                    Enchantments.BREACH,
                    Enchantments.DENSITY));
        }
        else if (entity != null && entity.isIn(EntityTypeTags.ARTHROPOD) && enchantments.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.BANE_OF_ARTHROPODS)) > 0) {
            enchantments.remove(containsAny(
                    Enchantments.SHARPNESS,
                    Enchantments.SMITE,
                    Enchantments.BREACH,
                    Enchantments.DENSITY));
        }
        else {
            enchantments.remove(containsAny(
                    Enchantments.SMITE,
                    Enchantments.BANE_OF_ARTHROPODS));
        }

        if (enchantments.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.DENSITY)) > 0) {
            enchantments.remove(containsAny(Enchantments.BREACH));
        }

        if (enchantments.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.SILK_TOUCH)) > 0 && PickBlockPro.getConfig().preferSilkTouch()) {
            enchantments.remove(containsAny((Enchantments.FORTUNE)));
        }
        else if (enchantments.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.FORTUNE)) > 0) {
            enchantments.remove(containsAny(Enchantments.SILK_TOUCH));
        }

        if (enchantments.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.INFINITY)) > 0) {
            enchantments.remove(containsAny(Enchantments.MENDING));
        }

        if (enchantments.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.MULTISHOT)) > 0) {
            enchantments.remove(containsAny(Enchantments.PIERCING));
        }

        if (enchantments.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.CHANNELING)) > 0 || enchantments.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.LOYALTY)) > 0) {
            enchantments.remove(containsAny(Enchantments.RIPTIDE));
        }
    }

    @SafeVarargs
    private Predicate<RegistryEntry<Enchantment>> containsAny(RegistryKey<Enchantment>... enchantments) {
        return e -> e.getKey().isPresent() && Arrays.asList(enchantments).contains(e.getKey().get());
    }
}
