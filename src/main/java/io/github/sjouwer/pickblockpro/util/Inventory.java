package io.github.sjouwer.pickblockpro.util;

import io.github.sjouwer.pickblockpro.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;

public final class Inventory {
    private static final MinecraftClient minecraft = MinecraftClient.getInstance();

    private Inventory(){
    }

    public static void placeItemInsideInventory(ItemStack item, ModConfig config) {
        boolean isCreative = minecraft.player.getAbilities().creativeMode;
        PlayerInventory inventory = minecraft.player.getInventory();
        int stackSlot = inventory.getSlotWithStack(item);

        //Item is already in hotbar
        if (stackSlot > -1 && stackSlot < 9) {
            inventory.selectedSlot = stackSlot;
            return;
        }

        //Set hotbar slot that's empty or otherwise not locked
        if ((stackSlot > 8 || isCreative) && !setOptimalSlot(inventory, config)) {
            return;
        }

        //Item is already inside the inventory, need to swap it to the hotbar
        if (stackSlot > 8) {
            minecraft.interactionManager.pickFromInventory(stackSlot);
            return;
        }

        //Add new item when the player is in creative and the item isn't yet inside the inventory
        if (isCreative) {
            int currentSlot = inventory.selectedSlot;
            int emptySlot = inventory.getEmptySlot();
            inventory.addPickBlock(item);
            updateCreativeSlot(currentSlot);
            if (emptySlot > 8) {
                updateCreativeSlot(emptySlot);
            }
        }
    }

    private static boolean setOptimalSlot(PlayerInventory inventory, ModConfig config) {
        int slot = inventory.selectedSlot;
        int tries = 0;
        while (!inventory.getStack(slot).isEmpty() && tries < 9) {
            tries++;
            slot++;
            if (slot > 8) {
                slot = 0;
            }
        }

        if (tries == 9) {
            slot = findUnlockedSlot(inventory, config);
            if (slot > 8) {
                Chat.sendError(new TranslatableText("text.pick_block_pro.message.allSlotsLocked"));
                return false;
            }
        }

        //tick to update new selected slot
        inventory.selectedSlot = slot;
        minecraft.interactionManager.tick();
        return true;
    }

    private static int findUnlockedSlot(PlayerInventory inventory, ModConfig config) {
        int selectedSlot = inventory.selectedSlot;

        if (config.isSlotLocked(selectedSlot)) {
            selectedSlot = 0;
            while ((config.isSlotLocked(selectedSlot) && selectedSlot < 35)) {
                selectedSlot++;
            }
        }

        return selectedSlot;
    }

    public static void updateCreativeSlot(int slot) {
        if (slot < 0) {
            return;
        }

        ItemStack item = minecraft.player.getInventory().getStack(slot);
        if (slot < 9) {
            slot = 36 + slot;
        }

        minecraft.interactionManager.clickCreativeStack(item, slot);
    }
}
