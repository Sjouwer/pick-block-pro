package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.util.EnchantmentUtil;
import io.github.sjouwer.pickblockpro.util.InfoProvider;
import io.github.sjouwer.pickblockpro.util.InventoryManager;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.entity.player.PlayerInventory.MAIN_SIZE;

public class WeaponPicker {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final ModConfig config = PickBlockPro.getConfig();

    private static ItemStack findBestWeapon(EntityType<?> entityType) {
        PlayerInventory inventory = client.player.getInventory();

        ItemStack bestSword = ItemStack.EMPTY;
        boolean foundSword = false;
        double bestSwordScore = -1;
        for (int i = 0; i < MAIN_SIZE; i++) {
            ItemStack itemStack = inventory.getStack(i);
            double score = calculateWeaponScore(itemStack, entityType);
            if (score <= 0) {
                continue;
            }

            foundSword = true;
            if (itemStack.getMaxDamage() - itemStack.getDamage() <= config.durabilityThreshold()) {
                continue;
            }

            if (score > bestSwordScore || (!bestSword.isEmpty() && score == bestSwordScore && itemStack.getDamage() < bestSword.getDamage())) {
                bestSword = itemStack;
                bestSwordScore = score;
            }
        }

        if (foundSword && bestSword.isEmpty()) {
            InfoProvider.sendWarning(Text.translatable("text.pickblockpro.message.allWeaponsBelowThreshold"));
        }

        return bestSword;
    }

    private static double calculateWeaponScore(ItemStack itemStack, EntityType<?> entityType) {
        double score = 0;

        score += getBaseDamage(itemStack);
        score += EnchantmentUtil.getItemEnchantmentsTotalDamage(itemStack, entityType);

        if (config.getBowPreferenceList().contains(entityType)) {
            score += itemStack.isOf(Items.BOW) ? 200 : 0;
            score += itemStack.isOf(Items.CROSSBOW) ? 100 : 0;
        }

        if (config.getTridentPreferenceList().contains(entityType)) {
            score += itemStack.isOf(Items.TRIDENT) ? 100 : 0;
        }

        return score;
    }

    private static double getBaseDamage(ItemStack stack) {
        List<Double> baseDamage = new ArrayList<>();
        stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT).modifiers().forEach(m -> {
            if (m.matches(EntityAttributes.ATTACK_DAMAGE, Item.BASE_ATTACK_DAMAGE_MODIFIER_ID)) {
                baseDamage.add(m.modifier().value());
            }
        });

        return baseDamage.stream().reduce(0d, Double::sum);
    }

    /**
     * Give the player a fully enchanted weapon
     * Only works in creative mode
     * @param weapon Weapon type to give and enchant
     */
    public static void giveWeapon(Weapons weapon) {
        if (!client.player.isCreative()) {
            InfoProvider.sendError(Text.translatable("text.pickblockpro.message.creativeRequired"));
            return;
        }

        ItemStack toolStack = config.getWeaponItemStack(weapon);
        InventoryManager.pickOrPlaceItemInInventory(toolStack);
    }

    protected static void giveOrSwitchWeapon(EntityType<?> entityType) {
        ItemStack bestWeapon = client.player.isCreative()
                ? createBestWeapon(entityType)
                : findBestWeapon(entityType);

        if (!bestWeapon.isEmpty()) {
            InventoryManager.pickOrPlaceItemInInventory(bestWeapon);
        }
    }

    /**
     * Get the best available tool with configured enchantments of the provided tool type
     * @param entityType EntityType to determine the best weapon and enchantment to kill it
     * @return Best available tool as ItemStack
     */
    public static ItemStack createBestWeapon(EntityType<?> entityType) {
        Weapons weapon = getMostSuitableWeapon(entityType);
        if (weapon == null) {
            return ItemStack.EMPTY;
        }

        return config.getWeaponItemStack(weapon, entityType);
    }

    private static Weapons getMostSuitableWeapon(EntityType<?> entityType) {
        if (config.getBowPreferenceList().contains(entityType)) {
            return Weapons.BOW;
        }
        if (config.getTridentPreferenceList().contains(entityType)) {
            return Weapons.TRIDENT;
        }
        return Weapons.SWORD;
    }

    public static void addConfiguredWeaponsToOpUtilities() {
        if (config.addWeaponsToOpUtilities() && config.enchantWeapons()) {
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(entries -> {
                for (Weapons weapon : Weapons.values()) {
                    if (weapon == Weapons.AXE && config.addToolsToOpUtilities()) {
                        continue;
                    }
                    entries.add(config.getWeaponItemStack(weapon));
                }
            });
        }
    }

    public enum Weapons {
        SWORD,
        AXE,
        BOW,
        CROSSBOW,
        TRIDENT,
        MACE
    }
}
