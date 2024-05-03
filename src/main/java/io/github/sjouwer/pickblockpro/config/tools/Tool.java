package io.github.sjouwer.pickblockpro.config.tools;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.util.InfoProvider;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;

//Field stuff needed to get it to work with Auto Config without needing to use a lot of duplicate code
public class Tool {
    public void getEnchantments(ItemEnchantmentsComponent.Builder enchantments) {
        Field[] fields = this.getClass().getDeclaredFields();
        for(Field field: fields) {
            try {
                if (field.getType().equals(int.class)) {
                    enchantments.add(getEnchantment(field.getName()), Math.min(field.getInt(this), 255));
                }
                if (field.getType().equals(boolean.class) && field.getBoolean(this)) {
                    enchantments.add(getEnchantment(field.getName()), 1);
                }
            }
            catch (Exception e) {
                InfoProvider.sendError(Text.literal("Issue setting " + field.getName() + " enchantment"));
                e.printStackTrace();
            }
        }
    }

    private Enchantment getEnchantment(String name) {
        Identifier identifier = Identifier.tryParse(name);
        return Registries.ENCHANTMENT.get(identifier);
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

    public ItemStack getItemStack(boolean enchantItem) {
        ItemStack toolStack = getItem().getDefaultStack();

        if (enchantItem) {
            ItemEnchantmentsComponent.Builder enchantments = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            getEnchantments(enchantments);
            EnchantmentHelper.set(toolStack, enchantments.build());
        }

        return toolStack;
    }
}
