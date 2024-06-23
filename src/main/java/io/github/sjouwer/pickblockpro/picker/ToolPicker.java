package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.util.*;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BrushableBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;

import static net.minecraft.entity.player.PlayerInventory.MAIN_SIZE;

public class ToolPicker {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final ModConfig config = PickBlockPro.getConfig();
    private static final List<Block> shearable = Arrays.asList(
            Blocks.COBWEB, Blocks.VINE, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT, Blocks.WEEPING_VINES,
            Blocks.TWISTING_VINES, Blocks.DEAD_BUSH, Blocks.FERN, Blocks.LARGE_FERN, Blocks.GLOW_LICHEN,
            Blocks.HANGING_ROOTS, Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS,
            Blocks.TRIPWIRE, Blocks.NETHER_SPROUTS);

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
                WeaponPicker.giveOrSwitchWeapon(livingEntity.getType());
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

        ItemStack bestTool = ItemStack.EMPTY;
        boolean foundTool = false;
        float bestToolScore = -1;
        for (int i = 0; i < MAIN_SIZE; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!isSuitableTool(stack, state)) {
                continue;
            }

            foundTool = true;
            if (stack.getDamage() > 0 && stack.getMaxDamage() - stack.getDamage() <= config.durabilityThreshold()) {
                continue;
            }

            float score = calculateToolScore(stack, state);
            if (score > bestToolScore || (!bestTool.isEmpty() && score == bestToolScore && stack.getDamage() < bestTool.getDamage())) {
                bestTool = stack;
                bestToolScore = score;
            }
        }

        if (foundTool && bestTool.isEmpty()) {
            InfoProvider.sendWarning(Text.translatable("text.pickblockpro.message.allToolsBelowThreshold"));
        }

        return bestTool;
    }

    @SuppressWarnings("deprecation")
    private static boolean isSuitableTool(ItemStack stack, BlockState state) {
        if (stack.isOf(Items.SHEARS) && (state.isIn(BlockTags.WOOL) || shearable.contains(state.getBlock()))) {
            return true;
        }
        if (stack.isIn(ItemTags.SWORDS) && state.isOf(Blocks.BAMBOO)) {
            return true;
        }
        if (stack.isOf(Items.BUCKET) && (state.isLiquid() || state.isOf(Blocks.POWDER_SNOW))) {
            return true;
        }
        if (stack.isOf(Items.BRUSH) && state.getBlock() instanceof BrushableBlock) {
            return true;
        }
        return stack.isSuitableFor(state);
    }

    private static float calculateToolScore(ItemStack stack, BlockState state) {
        float score = 0;

        if (stack.isIn(ItemTags.SWORDS) && state.isOf(Blocks.BAMBOO) && config.preferSwordForBamboo()
                || stack.isOf(Items.SHEARS) && shearable.contains(state.getBlock())
                || state.getBlock() instanceof BrushableBlock) {
            score += 100000000;
        }

        if (stack.getItem() instanceof ToolItem toolItem) {
            score += toolItem.getMaterial().getMiningSpeedMultiplier() * toolItem.getMaterial().getDurability() * 1000;
        }

        if (config.preferSilkTouch()) {
            score += EnchantmentHelper.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.SILK_TOUCH), stack) * 400;
            score += EnchantmentHelper.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.FORTUNE), stack) * 100;
        }
        else {
            score += EnchantmentHelper.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.FORTUNE), stack) * 150;
            score += EnchantmentHelper.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.SILK_TOUCH), stack) * 100;
        }

        if (config.preferEfficiency()) {
            score += EnchantmentHelper.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.EFFICIENCY), stack) * 500;
        }
        else {
            score += EnchantmentHelper.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.EFFICIENCY), stack) * 10;
        }

        score += EnchantmentHelper.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.UNBREAKING), stack);
        score += EnchantmentHelper.getLevel(EnchantmentUtil.getRegistryEntry(Enchantments.MENDING), stack) * 5;

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

        if (!bestTool.isEmpty()) {
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
        if (state.isIn(BlockTags.WOOL) || shearable.contains(state.getBlock())) {
            return Tools.SHEARS;
        }
        if (state.isOf(Blocks.BAMBOO) && config.preferSwordForBamboo()) {
            return Tools.SWORD;
        }
        if (state.getBlock() instanceof BrushableBlock) {
            return Tools.BRUSH;
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

    public static void addConfiguredToolsToOpUtilities() {
        if (config.addToolsToOpUtilities() && config.enchantTools()) {
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(entries -> {
                for(Tools tool : Tools.values()) {
                    if (tool == Tools.BUCKET || tool == Tools.SWORD && config.addWeaponsToOpUtilities()) {
                        continue;
                    }
                    entries.add(config.getToolItemStack(tool));
                }
            });
        }
    }

    public enum Tools {
        PICKAXE,
        AXE,
        SHOVEL,
        HOE,
        SWORD,
        SHEARS,
        BUCKET,
        FISHING_ROD,
        BRUSH
    }
}
