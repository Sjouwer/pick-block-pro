package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.config.PickBlockOverrides;
import io.github.sjouwer.pickblockpro.util.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
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
        if (client.player == null || client.world == null) {
            PickBlockPro.LOGGER.error("Pick Block called outside of play; no world and/or player");
            return;
        }

        if (!config.blockPickEntities() && !config.blockPickBlocks()) {
            InfoProvider.sendError(Text.translatable("text.pick_block_pro.message.nothingToPick"));
            return;
        }

        HitResult hit = Raycast.getHit(config.blockPickRange(), !config.blockPickFluids(), !config.blockPickEntities());

        ItemStack item = null;
        if (hit.getType() == HitResult.Type.ENTITY) {
            item = getEntityItemStack(hit);
        }
        if (hit.getType() == HitResult.Type.BLOCK && config.blockPickBlocks()) {
            item = getBlockItemStack(hit);
        }
        if (hit.getType() == HitResult.Type.MISS && config.blockPickLight()) {
            //Do another raycast with a longer reach to make sure there is nothing in the way of the sun or moon
            int distance = client.options.getViewDistance().getValue() * 32;
            hit = Raycast.getHit(distance, !config.blockPickFluids(), false);
            if (hit.getType() == HitResult.Type.MISS) {
                item = getLightFromSunOrMoon();
            }
        }

        if (item != null) {
            InventoryManager.placeItemInsideInventory(item);
        }
    }

    private static ItemStack getEntityItemStack(HitResult hit) {
        Entity entity = ((EntityHitResult) hit).getEntity();
        ItemStack item = PickBlockOverrides.getEntityOverride(entity.getType());
        if (item == null) {
            item = entity.getPickBlockStack();
        }

        if (item != null && client.player.getAbilities().creativeMode && Screen.hasControlDown()) {
            if (entity instanceof ItemFrameEntity) {
                ItemStack itemFrame = new ItemStack(Items.ITEM_FRAME);
                MutableText name = Text.translatable("text.pick_block_pro.name.framed", item.getName());
                name.setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.YELLOW));
                itemFrame.setCustomName(name);
                item = itemFrame;
            }

            if (entity instanceof PaintingEntity paintingEntity) {
                Optional<RegistryKey<PaintingVariant>> key = paintingEntity.getVariant().getKey();
                if (key.isPresent()) {
                    String translationKey = key.get().getValue().toTranslationKey("painting", "title");
                    MutableText name = Text.translatable(translationKey);
                    name.setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.YELLOW));
                    item.setCustomName(name);
                }
            }

            NbtUtil.addEntityNbt(item, entity);
        }

        if (entity instanceof PlayerEntity player) {
            item = new ItemStack(Items.PLAYER_HEAD);
            NbtUtil.setSkullOwner(item, player);
        }

        return item;
    }

    private static ItemStack getBlockItemStack(HitResult hit) {
        BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = client.world.getBlockState(blockPos);
        ItemStack item = PickBlockOverrides.getBlockOverride(state.getBlock());
        if (item == null) {
            item = state.getBlock().getPickStack(client.world, blockPos, state);
        }

        if (item.isEmpty()) {
            return null;
        }

        if (client.player.getAbilities().creativeMode) {
            if (Screen.hasControlDown() && state.hasBlockEntity()) {
                BlockEntity blockEntity = client.world.getBlockEntity(blockPos);
                NbtUtil.addBlockEntityNbt(item, blockEntity);
            }
            if (Screen.hasAltDown()) {
                NbtUtil.addBlockStateNbt(item, state);
            }
        }

        return item;
    }

    private static ItemStack getLightFromSunOrMoon() {
        double skyAngle = client.world.getSkyAngle(client.getTickDelta()) + .25;
        if (skyAngle > 1) {
            skyAngle --;
        }
        skyAngle *= 360;

        Vec3d playerVector = client.player.getRotationVec(client.getTickDelta());
        double playerAngle = Math.atan2(playerVector.y,playerVector.x) * 180 / Math.PI;
        if (playerAngle < 0) {
            playerAngle += 360;
        }

        //Sun
        double angleDifference = skyAngle - playerAngle;
        if (Math.abs(playerVector.z) < 0.076 && Math.abs(angleDifference) < 4.3) {
            return giveOrCycleLight(15);
        }

        //Moon
        if (Math.abs(playerVector.z) < 0.051 && Math.abs(angleDifference - 180) < 3) {
            return giveOrCycleLight(7);
        }

        return null;
    }

    private static ItemStack giveOrCycleLight(int lightLvl) {
        ItemStack mainHandStack = client.player.getMainHandStack();
        if (mainHandStack.isOf(Items.LIGHT)) {
            NbtUtil.cycleLightLevel(mainHandStack);
            PlayerInventory inventory = client.player.getInventory();
            inventory.setStack(inventory.selectedSlot, mainHandStack);
            InventoryManager.updateCreativeSlot(inventory.selectedSlot);
            return null;
        }
        else {
            ItemStack light = new ItemStack(Items.LIGHT);
            NbtUtil.setLightLevel(light, lightLvl);
            return light;
        }
    }
}
