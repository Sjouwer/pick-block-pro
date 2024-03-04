package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.config.PickBlockOverrides;
import io.github.sjouwer.pickblockpro.util.*;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockApplyCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

public class BlockPicker {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final ModConfig config = PickBlockPro.getConfig();

    private BlockPicker() {
    }

    /**
     * Provide the player with the item of the block or entity they are looking at
     */
    public static void pickBlock() {
        PlayerEntity player = client.player;
        if (player == null || client.world == null) {
            PickBlockPro.LOGGER.error("Pick Block called outside of play; no world and/or player");
            return;
        }

        if (!config.blockPickEntities() && !config.blockPickBlocks()) {
            InfoProvider.sendError(Text.translatable("text.pick_block_pro.message.nothingToPick"));
            return;
        }

        HitResult hit = RaycastUtil.getHit(config.blockBlockPickRange(player), config.entityBlockPickRange(player), !config.blockPickFluids(), !config.blockPickEntities());
        if (hit == null) {
            return;
        }

        ItemStack item = ClientPickBlockGatherCallback.EVENT.invoker().pick(player, hit);
        if (hit.getType() == HitResult.Type.ENTITY) {
            item = getEntityItemStack(hit, item);
        }

        if (hit.getType() == HitResult.Type.BLOCK && config.blockPickBlocks()) {
            item = getBlockItemStack(hit, item);
        }

        if (item.isEmpty() && hit.getType() == HitResult.Type.MISS && config.blockPickLight()) {
            item = getLightFromSunOrMoon();
        }

        if (!item.isEmpty()) {
            item = ClientPickBlockApplyCallback.EVENT.invoker().pick(player, hit, item);
        }

        if (!item.isEmpty()) {
            InventoryManager.pickOrPlaceItemInInventory(item);
        }
    }

    private static ItemStack getEntityItemStack(HitResult hit, ItemStack item) {
        Entity entity = ((EntityHitResult) hit).getEntity();

        ItemStack override = PickBlockOverrides.getEntityOverride(entity.getType());
        if (override != null) {
            item = override;
        }

        if (item.isEmpty()) {
            item = entity.getPickBlockStack();
        }

        if (item != null && client.player.getAbilities().creativeMode && Screen.hasControlDown()) {
            if (entity instanceof ItemFrameEntity itemFrame) {
                item = createFramedItemStack(itemFrame);
            }
            else if (entity instanceof PaintingEntity paintingEntity) {
                item = createPaintingVariantStack(paintingEntity);
            }
            else {
                DataComponentUtil.setEntityData(item, entity, true);
            }
        }

        if (entity instanceof PlayerEntity player) {
            item = new ItemStack(Items.PLAYER_HEAD);
            DataComponentUtil.setSkullOwner(item, player);
        }

        if (entity instanceof FallingBlockEntity fallingBlock) {
            item = getFallingBlockItemStack(fallingBlock);
        }

        return item == null ? ItemStack.EMPTY : item;
    }

    private static ItemStack createFramedItemStack(ItemFrameEntity itemFrame) {
        ItemStack itemFrameStack = new ItemStack(itemFrame instanceof GlowItemFrameEntity ? Items.GLOW_ITEM_FRAME : Items.ITEM_FRAME);
        ItemStack framedItem = itemFrame.getHeldItemStack();
        if (framedItem.isOf(Items.AIR)) {
            return itemFrameStack;
        }

        MutableText name = Text.translatable("text.pick_block_pro.name.framed", framedItem.getName());
        name.setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.YELLOW));
        itemFrameStack.set(DataComponentTypes.CUSTOM_NAME, name);

        DataComponentUtil.setEntityData(itemFrameStack, itemFrame, false);
        return itemFrameStack;
    }

    private static ItemStack createPaintingVariantStack(PaintingEntity painting) {
        ItemStack paintingStack = new ItemStack(Items.PAINTING);
        Optional<RegistryKey<PaintingVariant>> key = painting.getVariant().getKey();
        if (key.isPresent()) {
            String translationKey = key.get().getValue().toTranslationKey("painting", "title");
            MutableText name = Text.translatable(translationKey);
            name.setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.YELLOW));
            paintingStack.set(DataComponentTypes.CUSTOM_NAME, name);
        }

        DataComponentUtil.setEntityData(paintingStack, painting, false);
        return paintingStack;
    }

    private static ItemStack getFallingBlockItemStack(FallingBlockEntity fallingBlock) {
        ItemStack item = new ItemStack(fallingBlock.getBlockState().getBlock());
        if (client.player.getAbilities().creativeMode && Screen.hasAltDown()) {
            DataComponentUtil.setBlockStateData(item, fallingBlock.getBlockState(), true);
        }

        return item;
    }

    private static ItemStack getBlockItemStack(HitResult hit, ItemStack item) {
        BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = client.world.getBlockState(blockPos);
        Block block = state.getBlock();

        ItemStack override = PickBlockOverrides.getBlockOverride(block);
        if (override != null) {
            item = override;
        }

        if (item.isEmpty() && block instanceof FluidBlock) {
            item = state.getFluidState().getFluid().getBucketItem().getDefaultStack();
        }

        if (item.isEmpty()) {
            item = block.getPickStack(client.world, blockPos, state);
        }

        if (!item.isEmpty() && client.player.getAbilities().creativeMode) {
            if (Screen.hasControlDown() && state.hasBlockEntity()) {
                BlockEntity blockEntity = client.world.getBlockEntity(blockPos);
                DataComponentUtil.setBlockEntityData(item, blockEntity, client.world.getRegistryManager(), true);
            }
            if (Screen.hasAltDown()) {
                DataComponentUtil.setBlockStateData(item, state, true);
            }
        }

        return item;
    }

    private static ItemStack getLightFromSunOrMoon() {
        //Make sure we're in the overworld
        if (client.world.getRegistryKey() != World.OVERWORLD) {
            return ItemStack.EMPTY;
        }

        //Do another raycast with a longer reach to make sure there is nothing in the way of the sun or moon
        int distance = client.options.getViewDistance().getValue() * 32;
        HitResult hit = RaycastUtil.getHit(distance, distance, false, false);
        if (hit == null || hit.getType() != HitResult.Type.MISS) {
            return ItemStack.EMPTY;
        }

        double skyAngle = client.world.getSkyAngle(client.getTickDelta()) + .25;
        if (skyAngle > 1) {
            skyAngle --;
        }
        skyAngle *= 360;

        Vec3d playerVector = client.player.getRotationVec(client.getTickDelta());
        double playerAngle = Math.atan2(playerVector.y, playerVector.x) * 180 / Math.PI;
        if (playerAngle < 0) {
            playerAngle += 360;
        }

        double angleDifference = skyAngle - playerAngle;

        //Sun
        if (Math.abs(playerVector.z) < 0.076 && Math.abs(angleDifference) < 4.3) {
            return giveOrCycleLight(15);
        }

        //Moon
        if (Math.abs(playerVector.z) < 0.051 && Math.abs(angleDifference - 180) < 3) {
            return giveOrCycleLight(7);
        }

        return ItemStack.EMPTY;
    }

    private static ItemStack giveOrCycleLight(int lightLvl) {
        PlayerEntity player = client.player;
        ItemStack mainHandStack = player.getMainHandStack();
        if (mainHandStack.isOf(Items.LIGHT) && player.getAbilities().creativeMode) {
            DataComponentUtil.cycleLightLevel(mainHandStack);
            InventoryManager.updateCreativeSlot(player.getInventory().selectedSlot);
            return ItemStack.EMPTY;
        }

        ItemStack light = new ItemStack(Items.LIGHT);
        DataComponentUtil.setLightLevel(light, lightLvl);
        return light;
    }
}
