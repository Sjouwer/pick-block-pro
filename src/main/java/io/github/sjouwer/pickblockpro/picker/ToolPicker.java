package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.util.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.entity.player.PlayerInventory.MAIN_SIZE;

public class ToolPicker {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final ModConfig config = PickBlockPro.getConfig();

    private ToolPicker() {
    }

    /**
     * Provide the player with the best tool to break the block or kill the entity they are looking at
     */
    public static void pickTool() {
        if (client.player == null || client.world == null) {
            PickBlockPro.LOGGER.error("Pick Tool called outside of play; no world and/or player");
            return;
        }

        HitResult hit = RaycastUtil.getHit(config.blockToolPickRange(client.player), config.entityToolPickRange(client.player), !config.toolPickFluids(), false);
        if (hit == null || hit.getType() == HitResult.Type.MISS) {
            return;
        }

        if (hit.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hit).getEntity();
            if (entity instanceof LivingEntity livingEntity) {
                WeaponPicker.giveOrSwitchWeapon(livingEntity);
            }
        }
        else {
            BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
            BlockState state = client.world.getBlockState(blockPos);
            giveOrSwitchTool(state);
        }
    }

    private static ItemStack findBestTool(BlockState state) {
        PlayerInventory inventory = client.player.getInventory();

        ItemStack bestTool = null;
        boolean foundTool = false;
        float bestToolScore = -1;
        for (int i = 0; i < MAIN_SIZE; i++) {
            ItemStack itemStack = inventory.getStack(i);
            if (!itemStack.isSuitableFor(state)) {
                continue;
            }

            foundTool = true;
            if (itemStack.getMaxDamage() - itemStack.getDamage() <= config.durabilityThreshold()) {
                continue;
            }

            float score = calculateToolScore(itemStack);
            if (score > bestToolScore || (bestTool != null && score == bestToolScore && itemStack.getDamage() < bestTool.getDamage())) {
                bestTool = itemStack;
                bestToolScore = score;
            }
        }

        if (foundTool && bestTool == null) {
            InfoProvider.sendWarning(Text.translatable("text.pickblockpro.message.allToolsBelowThreshold"));
        }

        return bestTool;
    }

    private static float calculateToolScore(ItemStack item) {
        float score = 0;
        if (item.getItem() instanceof ToolItem toolItem) {
            score += toolItem.getMaterial().getMiningSpeedMultiplier() * toolItem.getMaterial().getDurability() * 10000;
        }

        if (config.preferSilkTouch()) {
            score += EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, item) * 400;
            score += EnchantmentHelper.getLevel(Enchantments.FORTUNE, item) * 100;
        }
        else {
            score += EnchantmentHelper.getLevel(Enchantments.FORTUNE, item) * 150;
            score += EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, item) * 100;
        }

        if (config.preferEfficiency()) {
            score += EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, item) * 500;
        }
        else {
            score += EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, item) * 10;
        }

        score += EnchantmentHelper.getLevel(Enchantments.UNBREAKING, item);
        score += EnchantmentHelper.getLevel(Enchantments.MENDING, item) * 5;

        return score;
    }

    /**
     * Give the player a fully enchanted tool
     * Only works in creative mode
     * @param tool Tool type to give and enchant
     */
    public static void giveTool(Tools tool) {
        if (!client.player.isCreative()) {
            InfoProvider.sendError(Text.translatable("text.pickblockpro.message.creativeRequired"));
            return;
        }

        ItemStack toolStack = config.getToolItemStack(tool);
        InventoryManager.pickOrPlaceItemInInventory(toolStack);
    }

    private static void giveOrSwitchTool(BlockState state) {
        ItemStack bestTool = client.player.isCreative()
                ? createBestTool(state)
                : findBestTool(state);

        if (bestTool != null && !bestTool.isEmpty()) {
            InventoryManager.pickOrPlaceItemInInventory(bestTool);
        }
    }

    /**
     * Get the best available tool with configured enchantments of the provided tool type
     * @param state BlockState
     * @return Best available tool as ItemStack
     */
    public static ItemStack createBestTool(BlockState state) {
        Tools tool = getMostSuitableTool(state);
        if (tool == null) {
            return ItemStack.EMPTY;
        }

        return config.getToolItemStack(tool);
    }

    @SuppressWarnings("deprecation")
    private static Tools getMostSuitableTool(BlockState state) {
        if (state.isIn(BlockTags.WOOL) || state.isOf(Blocks.COBWEB)) {
            return Tools.SHEARS;
        }
        if (state.isOf(Blocks.BAMBOO) && config.preferSwordForBamboo()) {
            return Tools.SWORD;
        }
        if (state.isLiquid() || state.isOf(Blocks.POWDER_SNOW)) {
            return Tools.BUCKET;
        }
        if (state.isIn(BlockTags.PICKAXE_MINEABLE)) {
            return Tools.PICKAXE;
        }
        if (state.isIn(BlockTags.AXE_MINEABLE)) {
            return Tools.AXE;
        }
        if (state.isIn(BlockTags.SHOVEL_MINEABLE)) {
            return Tools.SHOVEL;
        }
        if (state.isIn(BlockTags.HOE_MINEABLE)) {
            return Tools.HOE;
        }

        return null;
    }

    public enum Tools {
        PICKAXE,
        AXE,
        SHOVEL,
        HOE,
        SWORD,
        SHEARS,
        BUCKET,
        FISHING_ROD
    }
}
