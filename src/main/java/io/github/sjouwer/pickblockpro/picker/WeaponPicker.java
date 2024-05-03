package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.util.InfoProvider;
import io.github.sjouwer.pickblockpro.util.InventoryManager;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.Optional;

import static net.minecraft.entity.player.PlayerInventory.MAIN_SIZE;
import static net.minecraft.item.Item.ATTACK_DAMAGE_MODIFIER_ID;

public class WeaponPicker {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final ModConfig config = PickBlockPro.getConfig();

    private static ItemStack findBestWeapon(EntityType<?> entityType) {
        PlayerInventory inventory = client.player.getInventory();

        ItemStack bestSword = null;
        boolean foundSword = false;
        float bestSwordScore = -1;
        for (int i = 0; i < MAIN_SIZE; i++) {
            ItemStack itemStack = inventory.getStack(i);
            float score = calculateWeaponScore(itemStack, entityType);
            if (score <= 0) {
                continue;
            }

            foundSword = true;
            if (itemStack.getMaxDamage() - itemStack.getDamage() <= config.durabilityThreshold()) {
                continue;
            }

            if (score > bestSwordScore || (bestSword != null && score == bestSwordScore && itemStack.getDamage() < bestSword.getDamage())) {
                bestSword = itemStack;
                bestSwordScore = score;
            }
        }

        if (foundSword && bestSword == null) {
            InfoProvider.sendWarning(Text.translatable("text.pickblockpro.message.allWeaponsBelowThreshold"));
        }

        return bestSword;
    }

    private static float calculateWeaponScore(ItemStack item, EntityType<?> entityType) {
        float score = 0;

        AttributeModifiersComponent component = item.getComponents().get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (component != null) {
            Optional<Double> weaponDamage = component.modifiers().stream()
                    .filter(m -> m.modifier().uuid().equals(ATTACK_DAMAGE_MODIFIER_ID)
                            && m.modifier().name().equals("Weapon modifier"))
                    .map(m -> m.modifier().value())
                    .findAny();
            score += weaponDamage.map(Double::floatValue).orElse(0F);
        }

        score += EnchantmentHelper.getAttackDamage(item, entityType);

        if (config.getBowPreferenceList().contains(entityType)) {
            score += item.isOf(Items.BOW) ? 200 : 0;
            score += item.isOf(Items.CROSSBOW) ? 100 : 0;
        }

        if (config.getTridentPreferenceList().contains(entityType)) {
            score += item.isOf(Items.TRIDENT) ? 100 : 0;
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

    protected static void giveOrSwitchWeapon(EntityType<?> entityType) {
        ItemStack bestWeapon = client.player.isCreative()
                ? createBestWeapon(entityType)
                : findBestWeapon(entityType);

        if (bestWeapon != null && !bestWeapon.isEmpty()) {
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

        return config.getWeaponItemStack(weapon);
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
