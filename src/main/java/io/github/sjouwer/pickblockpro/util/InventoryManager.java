package io.github.sjouwer.pickblockpro.util;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import static net.minecraft.entity.player.PlayerInventory.MAIN_SIZE;

public final class InventoryManager {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final ModConfig config = PickBlockPro.getConfig();
    public static final int HOTBAR_SIZE = PlayerInventory.getHotbarSize();

    private InventoryManager(){
    }

    /**
     * Picks or places an item inside the player's inventory
     * Makes sure to not use locked slots and to sync with the server
     * @param item Item to give the player
     */
    public static void pickOrPlaceItemInInventory(ItemStack item) {
        if (client.player == null) {
            PickBlockPro.LOGGER.error("Unable to place item inside inventory; no player found");
            return;
        }

        PlayerInventory inventory = client.player.getInventory();
        int selectedSlot = inventory.selectedSlot;
        pickOrPlaceItemInInventory(item, inventory);
        int newSelectedSlot = inventory.selectedSlot;

        if (config.stayInSameSlot() &&
                selectedSlot != newSelectedSlot &&
                !isHotbarSlotLocked(selectedSlot) &&
                !isHotbarSlotLocked(newSelectedSlot)) {
            swapSlot(selectedSlot, newSelectedSlot);
            inventory.selectedSlot = selectedSlot;
        }
    }

    private static void pickOrPlaceItemInInventory(ItemStack item, PlayerInventory inventory) {
        boolean isCreative = client.player.getAbilities().creativeMode;
        int stackSlot = inventory.getSlotWithStack(item);

        //Item is not inside the inventory, if in survival search through item-containers to see if they contain the item
        if (config.searchContainers() && stackSlot == -1 && !isCreative) {
            stackSlot = searchThroughContainers(inventory, item.getItem());
        }

        //Item is already in hotbar
        if (PlayerInventory.isValidHotbarIndex(stackSlot)) {
            inventory.selectedSlot = stackSlot;
            return;
        }

        //Set hotbar slot that's empty or otherwise not locked
        if ((stackSlot >= HOTBAR_SIZE || isCreative) && !setOptimalSlot(inventory)) {
            return;
        }

        //Item is already inside the inventory, need to swap it to the hotbar
        if (stackSlot >= HOTBAR_SIZE) {
            swapSlot(inventory.selectedSlot, stackSlot);
            return;
        }

        //Add new item when the player is in creative and the item isn't yet inside the inventory
        if (isCreative) {
            int currentSlot = inventory.selectedSlot;
            int emptySlot = inventory.getEmptySlot();
            inventory.addPickBlock(item);
            updateCreativeSlot(currentSlot);
            if (emptySlot >= HOTBAR_SIZE) {
                updateCreativeSlot(emptySlot);
            }
        }
    }

    private static void swapSlot(int firstSlot, int secondSlot) {
        if (client.interactionManager == null || client.player == null) {
            PickBlockPro.LOGGER.error("Unable to swap inventory slot; no player and/or interaction manager");
            return;
        }

        client.interactionManager.clickSlot(
                client.player.playerScreenHandler.syncId,
                MAIN_SIZE + firstSlot,
                secondSlot,
                SlotActionType.SWAP,
                client.player);
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

        for (int i = 0; i < MAIN_SIZE; ++i) {
            ItemStack invStack = inventory.getStack(i);
            if (invStack.isEmpty()) continue;

            int storedAmount = NbtUtil.getAmountStored(invStack, item);
            if (storedAmount > highestAmount) {
                slot = i;
                highestAmount = storedAmount;
            }
        }

        return slot;
    }

    private static boolean setOptimalSlot(PlayerInventory inventory) {
        int slot = findEmptyHotbarSlot();
        if (slot == -1) slot = findUnlockedHotbarSlot();

        if (slot == -1) {
            InfoProvider.sendError(Text.translatable("text.pick_block_pro.message.allSlotsLocked"));
            return false;
        }

        inventory.selectedSlot = slot;
        return true;
    }

    /**
     * Tries to find an empty hotbar slot.
     * Starts with the players selected inventory slot.
     * @return Index of an empty slot or -1 if there are none
     */
    public static int findEmptyHotbarSlot() {
        if (client.player == null) return -1;

        PlayerInventory inventory = client.player.getInventory();
        int slot = inventory.selectedSlot;
        int tries = 0;
        while (!inventory.getStack(slot).isEmpty() && tries < HOTBAR_SIZE) {
            tries++; slot++;
            if (slot >= HOTBAR_SIZE) {
                slot = 0;
            }
        }

        return tries >= HOTBAR_SIZE ? -1 : slot;
    }

    /**
     * Tries to find an unlocked hotbar slot.
     * Starts with the players selected inventory slot.
     * @return Index of an unlocked slot or -1 if there are none
     */
    public static int findUnlockedHotbarSlot() {
        if (client.player == null) return -1;

        int slot = client.player.getInventory().selectedSlot;
        int tries = 0;
        while (isHotbarSlotLocked(slot) && tries < HOTBAR_SIZE) {
            tries++; slot++;
            if (slot >= HOTBAR_SIZE) {
                slot = 0;
            }
        }

        return tries >= HOTBAR_SIZE ? -1 : slot;
    }

    /**
     * Checks if the hotbar slot is locked.
     * Non hotbar slots will return false by default.
     * @return True if slot is locked
     */
    public static boolean isHotbarSlotLocked(int slot) {
        return config.isSlotLocked(slot);
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

        if (slot < 0) return;

        ItemStack item = client.player.getInventory().getStack(slot);
        if (PlayerInventory.isValidHotbarIndex(slot)) {
            slot = MAIN_SIZE + slot;
        }

        client.interactionManager.clickCreativeStack(item, slot);
    }
}
