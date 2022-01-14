package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.util.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;

public class ToolPicker {
    private final ModConfig config;
    private static final MinecraftClient minecraft = MinecraftClient.getInstance();

    public ToolPicker() {
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public void pickTool() {
        HitResult hit = Raycast.getHit(config.toolPickRange(), RaycastContext.FluidHandling.ANY, false);

        if (hit.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hit).getEntity();
            if (entity.isLiving()) {
                giveOrSwitchTool(Tools.SWORD, entity);
            }
        }
        else {
            BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
            BlockState state = minecraft.world.getBlockState(blockPos);
            pickMostSuitableTool(state);
        }
    }

    private void pickMostSuitableTool(BlockState state) {
        if (state.isIn(BlockTags.WOOL) || state.isOf(Blocks.COBWEB)) {
            giveOrSwitchTool(Tools.SHEARS);
            return;
        }
        if (state.isOf(Blocks.BAMBOO) && config.preferSwordForBamboo()) {
            giveOrSwitchTool(Tools.SWORD);
            return;
        }
        if (state.getMaterial().isLiquid() || state.isOf(Blocks.POWDER_SNOW)) {
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

    private void giveOrSwitchTool(Tools tool) {
        this.giveOrSwitchTool(tool, null);
    }

    private void giveOrSwitchTool(Tools tool, Entity entity) {
        ItemStack bestTool;
        if (minecraft.player.getAbilities().creativeMode) {
            bestTool = createBestTool(tool);
        }
        else {
            bestTool = findBestTool(tool, entity);
        }

        if (bestTool != null) {
            Inventory.placeItemInsideInventory(bestTool, config);
        }
    }

    private ItemStack findBestTool(Tools tool, Entity entity) {
        PlayerInventory inventory = minecraft.player.getInventory();
        if (tool == Tools.BUCKET) {
            ItemStack bucket = new ItemStack(Items.BUCKET);
            if (inventory.contains(bucket)) {
                return bucket;
            }
        }

        ItemStack bestTool = null;
        int bestToolScore = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = inventory.getStack(i);
            if (!tool.getClassType().isInstance(itemStack.getItem())) {
                continue;
            }

            int score;
            if (entity == null) {
                score = calculateToolScore(itemStack);
            }
            else {
                score = calculateSwordScore(itemStack, entity);
            }
            if (score > bestToolScore || (bestTool != null && score == bestToolScore && itemStack.getDamage() < bestTool.getDamage())) {
                bestTool = itemStack;
                bestToolScore = score;
            }
        }

        return bestTool;
    }

    private int calculateToolScore(ItemStack item) {
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

    private int calculateSwordScore(ItemStack item, Entity entity) {
        int score = 0;
        if (item.getItem() instanceof SwordItem swordItem && entity instanceof LivingEntity livingEntity) {
            score += swordItem.getAttackDamage();
            score += EnchantmentHelper.getAttackDamage(item, livingEntity.getGroup());
        }

        return score;
    }

    private ItemStack createBestTool(Tools tool) {
        switch (tool) {
            case PICKAXE:
                ItemStack pickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
                if (config.enchantTools()) {
                    pickaxe.addEnchantment(Enchantments.MENDING, 1);
                    pickaxe.addEnchantment(Enchantments.UNBREAKING, 3);
                    pickaxe.addEnchantment(Enchantments.EFFICIENCY, 5);
                    pickaxe.addEnchantment(Enchantments.SILK_TOUCH, 1);
                }
                return pickaxe;
            case AXE:
                ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
                if (config.enchantTools()) {
                    axe.addEnchantment(Enchantments.MENDING, 1);
                    axe.addEnchantment(Enchantments.UNBREAKING, 3);
                    axe.addEnchantment(Enchantments.SHARPNESS, 5);
                    axe.addEnchantment(Enchantments.EFFICIENCY, 5);
                    axe.addEnchantment(Enchantments.SILK_TOUCH, 1);
                }
                return axe;
            case SHOVEL:
                ItemStack shovel = new ItemStack(Items.NETHERITE_SHOVEL);
                if (config.enchantTools()) {
                    shovel.addEnchantment(Enchantments.MENDING, 1);
                    shovel.addEnchantment(Enchantments.UNBREAKING, 3);
                    shovel.addEnchantment(Enchantments.EFFICIENCY, 5);
                    shovel.addEnchantment(Enchantments.SILK_TOUCH, 1);
                }
                return shovel;
            case HOE:
                ItemStack hoe = new ItemStack(Items.NETHERITE_HOE);
                if (config.enchantTools()) {
                    hoe.addEnchantment(Enchantments.MENDING, 1);
                    hoe.addEnchantment(Enchantments.UNBREAKING, 3);
                    hoe.addEnchantment(Enchantments.EFFICIENCY, 5);
                    hoe.addEnchantment(Enchantments.SILK_TOUCH, 1);
                }
                return hoe;
            case SWORD:
                ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
                if (config.enchantTools()) {
                    sword.addEnchantment(Enchantments.MENDING, 1);
                    sword.addEnchantment(Enchantments.UNBREAKING, 3);
                    sword.addEnchantment(Enchantments.SHARPNESS, 5);
                    sword.addEnchantment(Enchantments.SWEEPING, 3);
                    sword.addEnchantment(Enchantments.LOOTING, 3);
                }
                return sword;
            case SHEARS:
                ItemStack shears = new ItemStack(Items.SHEARS);
                if (config.enchantTools()) {
                    shears.addEnchantment(Enchantments.MENDING, 1);
                    shears.addEnchantment(Enchantments.UNBREAKING, 3);
                    shears.addEnchantment(Enchantments.EFFICIENCY, 5);
                }
                return shears;
            case BUCKET:
                return new ItemStack(Items.BUCKET);
            default:
                return null;
        }
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
