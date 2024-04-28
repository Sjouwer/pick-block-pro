package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.util.InfoProvider;
import io.github.sjouwer.pickblockpro.util.InventoryManager;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.text.Text;

import static net.minecraft.entity.player.PlayerInventory.MAIN_SIZE;

public class WeaponPicker {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final ModConfig config = PickBlockPro.getConfig();

    private static ItemStack findBestWeapon(Entity entity) {
        PlayerInventory inventory = client.player.getInventory();

        ItemStack bestSword = null;
        boolean foundSword = false;
        float bestSwordScore = -1;
        for (int i = 0; i < MAIN_SIZE; i++) {
            ItemStack itemStack = inventory.getStack(i);
            if (!(itemStack.getItem() instanceof SwordItem)) {
                continue;
            }

            foundSword = true;
            if (itemStack.getMaxDamage() - itemStack.getDamage() <= config.durabilityThreshold()) {
                continue;
            }

            float score = calculateWeaponScore(itemStack, entity);
            if (score > bestSwordScore || (bestSword != null && score == bestSwordScore && itemStack.getDamage() < bestSword.getDamage())) {
                bestSword = itemStack;
                bestSwordScore = score;
            }
        }

        if (foundSword && bestSword == null) {
            InfoProvider.sendWarning(Text.translatable("text.pickblockpro.message.allToolsBelowThreshold"));
        }

        return bestSword;
    }

    private static float calculateWeaponScore(ItemStack item, Entity entity) {
        float score = 0;
        score += entity != null ? EnchantmentHelper.getAttackDamage(item, entity.getType()) : 0;
        if (item.getItem() instanceof ToolItem toolItem) {
            score += toolItem.getMaterial().getAttackDamage();
        }

        return score;
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

    protected static void giveOrSwitchWeapon(Entity entity) {
        ItemStack bestWeapon = client.player.isCreative()
                ? createBestWeapon(entity)
                : findBestWeapon(entity);

        if (bestWeapon != null && !bestWeapon.isEmpty()) {
            InventoryManager.pickOrPlaceItemInInventory(bestWeapon);
        }
    }

    /**
     * Get the best available tool with configured enchantments of the provided tool type
     * @param entity Entity to determine the best weapon and enchantment to kill it
     * @return Best available tool as ItemStack
     */
    public static ItemStack createBestWeapon(Entity entity) {
        Weapons weapon = getMostSuitableWeapon(entity);
        if (weapon == null) {
            return ItemStack.EMPTY;
        }

        return config.getWeaponItemStack(weapon);
    }

    private static Weapons getMostSuitableWeapon(Entity entity) {
        return Weapons.SWORD;
    }

    public static void addConfiguredWeaponsToOpUtilities() {
        if (config.addWeaponsToOpTab() && config.enchantTools()) {
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(entries -> {
                for(Weapons weapon : Weapons.values()) {
                    if (weapon == Weapons.AXE && config.addToolsToOpTab()) {
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
