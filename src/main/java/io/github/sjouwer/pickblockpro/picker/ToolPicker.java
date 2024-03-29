package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.util.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

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
        if (hit == null) {
            return;
        }

        if (hit.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hit).getEntity();
            if (entity instanceof LivingEntity livingEntity) {
                giveOrSwitchTool(Tools.SWORD, livingEntity);
            }
        }
        else {
            BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
            BlockState state = client.world.getBlockState(blockPos);
            pickMostSuitableTool(state);
        }
    }

    @SuppressWarnings("deprecation")
    private static void pickMostSuitableTool(BlockState state) {
        if (state.isIn(BlockTags.WOOL) || state.isOf(Blocks.COBWEB)) {
            giveOrSwitchTool(Tools.SHEARS);
            return;
        }
        if (state.isOf(Blocks.BAMBOO) && config.preferSwordForBamboo()) {
            giveOrSwitchTool(Tools.SWORD);
            return;
        }
        if (state.isLiquid() || state.isOf(Blocks.POWDER_SNOW)) {
            giveOrSwitchTool(Tools.BUCKET);
            return;
        }
        if (state.isIn(BlockTags.PICKAXE_MINEABLE)) {
            giveOrSwitchTool(Tools.PICKAXE);
            return;
        }
        if (state.isIn(BlockTags.AXE_MINEABLE)) {
            giveOrSwitchTool(Tools.AXE);
            return;
        }
        if (state.isIn(BlockTags.SHOVEL_MINEABLE)) {
            giveOrSwitchTool(Tools.SHOVEL);
            return;
        }
        if (state.isIn(BlockTags.HOE_MINEABLE)) {
            giveOrSwitchTool(Tools.HOE);
        }
    }

    private static void giveOrSwitchTool(Tools tool) {
        giveOrSwitchTool(tool, null);
    }

    private static void giveOrSwitchTool(Tools tool, Entity entity) {
        ItemStack bestTool = client.player.getAbilities().creativeMode ? createBestTool(tool, entity) : findBestTool(client.player, tool, entity);
        if (bestTool != null) {
            InventoryManager.pickOrPlaceItemInInventory(bestTool);
        }
    }

    /**
     * Find the best available tool inside the player's inventory of the provided tool type
     * @param tool Tool type
     * @param entity Should only be provided with a sword to find the best enchantment to kill the entity
     * @return Player's best available tool as ItemStack or null if none are found
     */
    public static ItemStack findBestTool(PlayerEntity player, Tools tool, Entity entity) {
        PlayerInventory inventory = player.getInventory();
        if (tool.equals(Tools.BUCKET)) {
            ItemStack bucket = Items.BUCKET.getDefaultStack();
            if (inventory.contains(bucket)) {
                return bucket;
            }
        }

        ItemStack bestTool = null;
        boolean foundTool = false;
        int bestToolScore = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = inventory.getStack(i);
            if (!tool.getClassType().isInstance(itemStack.getItem())) {
                continue;
            }

            foundTool = true;
            if (itemStack.getMaxDamage() - itemStack.getDamage() <= config.durabilityThreshold()) {
                continue;
            }

            int score = tool.equals(Tools.SWORD) ? calculateSwordScore(itemStack, entity) : calculateToolScore(itemStack);
            if (score > bestToolScore || (bestTool != null && score == bestToolScore && itemStack.getDamage() < bestTool.getDamage())) {
                bestTool = itemStack;
                bestToolScore = score;
            }
        }

        if (foundTool && bestTool == null) {
            InfoProvider.sendWarning(Text.translatable("text.pick_block_pro.message.allToolsBelowThreshold"));
        }

        return bestTool;
    }

    private static int calculateToolScore(ItemStack item) {
        int score = 0;
        if (item.getItem() instanceof ToolItem toolItem) {
            score += toolItem.getMaterial().getMiningLevel() * 10000;
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

    private static int calculateSwordScore(ItemStack item, Entity entity) {
        int score = 0;
        if (item.getItem() instanceof SwordItem swordItem) {
            score += swordItem.getMaxDamage();
            score += EnchantmentHelper.getAttackDamage(item, entity.getType());
        }

        return score;
    }

    /**
     * Get the best available tool with configured enchantments of the provided tool type
     * @param tool Tool type
     * @param entity Should only be provided with a sword to determine the best enchantment to kill the entity
     * @return Best available tool as ItemStack
     */
    public static ItemStack createBestTool(Tools tool, Entity entity) {
        ItemStack bestTool = switch (tool) {
            case PICKAXE -> Items.NETHERITE_PICKAXE.getDefaultStack();
            case AXE -> Items.NETHERITE_AXE.getDefaultStack();
            case SHOVEL -> Items.NETHERITE_SHOVEL.getDefaultStack();
            case HOE -> Items.NETHERITE_HOE.getDefaultStack();
            case SWORD -> Items.NETHERITE_SWORD.getDefaultStack();
            case SHEARS -> Items.SHEARS.getDefaultStack();
            case BUCKET -> Items.BUCKET.getDefaultStack();
        };

        ItemEnchantmentsComponent enchantments = config.getEnchantments(tool, entity);
        EnchantmentHelper.set(bestTool, enchantments);

        return bestTool;
    }

    public enum Tools {
        BUCKET(BucketItem.class),
        PICKAXE(PickaxeItem.class),
        AXE(AxeItem.class),
        SHOVEL(ShovelItem.class),
        HOE(HoeItem.class),
        SWORD(SwordItem.class),
        SHEARS(ShearsItem.class);


        private final Class<?> classObject;
        Tools(Class<?> classObj) {
            this.classObject = classObj;
        }

        public Class<?> getClassType() {
            return this.classObject;
        }
    }
}
