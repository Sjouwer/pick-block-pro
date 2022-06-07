package io.github.sjouwer.pickblockpro.util;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;

public final class InventoryManager {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final ModConfig config = PickBlockPro.getConfig();

    private InventoryManager(){
    }

    /**
     * Places an item inside the player's inventory
     * Makes sure to not use locked slots and to sync with the server
     * @param item Item to give the player
     */
    public static void placeItemInsideInventory(ItemStack item) {
        if (client.interactionManager == null || client.player == null) {
            PickBlockPro.LOGGER.error("Unable to place item inside inventory; no player and/or interaction manager");
            return;
        }

        boolean isCreative = client.player.getAbilities().creativeMode;
        PlayerInventory inventory = client.player.getInventory();
        int stackSlot = inventory.getSlotWithStack(item);

        //Item is not inside the inventory, if in survival search through item-containers to see if they contain the item
        if (config.searchContainers() && stackSlot == -1 && !isCreative) {
            stackSlot = searchThroughContainers(inventory, item.getItem());
        }

        //Item is already in hotbar
        if (stackSlot > -1 && stackSlot < 9) {
            inventory.selectedSlot = stackSlot;
            return;
        }

        //Set hotbar slot that's empty or otherwise not locked
        if ((stackSlot > 8 || isCreative) && !setOptimalSlot(inventory)) {
            return;
        }

        //Item is already inside the inventory, need to swap it to the hotbar
        if (stackSlot > 8) {
            client.interactionManager.pickFromInventory(stackSlot);
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

    /**
     * Searches through item-containers like Shulkers
     * @param inventory Player's inventory
     * @param item Item to find inside the container
     * @return Slot with the container that has most of the item stored or -1 if none were found
     */
    private static int searchThroughContainers(PlayerInventory inventory, Item item) {
        int slot = -1;
        int highestAmount = 0;

        for (int i = 0; i < inventory.main.size(); ++i) {
            ItemStack invStack = inventory.getStack(i);
            if (!invStack.isEmpty()) {
                int storedAmount = NbtUtil.getAmountStored(invStack, item);
                if (storedAmount > highestAmount) {
                    slot = i;
                    highestAmount = storedAmount;
                }
            }
        }

        return slot;
    }

    private static boolean setOptimalSlot(PlayerInventory inventory) {
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
            slot = findUnlockedSlot(inventory);
            if (slot > 8) {
                Chat.sendError(new TranslatableText("text.pick_block_pro.message.allSlotsLocked"));
                return false;
            }
        }

        //tick to update new selected slot
        inventory.selectedSlot = slot;
        client.interactionManager.tick();
        return true;
    }

    private static int findUnlockedSlot(PlayerInventory inventory) {
        ModConfig config = PickBlockPro.getConfig();
        int selectedSlot = inventory.selectedSlot;
        if (config.isSlotLocked(selectedSlot)) {
            selectedSlot = 0;
            while ((config.isSlotLocked(selectedSlot) && selectedSlot < 35)) {
                selectedSlot++;
            }
        }

        return selectedSlot;
    }

    /**
     * Update the (internal)Server about an inventory slot change
     * @param slot Slot that has been changed
     */
    public static void updateCreativeSlot(int slot) {
        if (client.interactionManager == null || client.player == null) {
            PickBlockPro.LOGGER.error("Unable to update inventory slot; no player and/or interaction manager");
            return;
        }

        if (slot < 0) {
            return;
        }

        ItemStack item = client.player.getInventory().getStack(slot);
        if (slot < 9) {
            slot = 36 + slot;
        }

        client.interactionManager.clickCreativeStack(item, slot);
    }
}
