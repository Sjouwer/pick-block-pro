package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.config.PickBlockOverrides;
import io.github.sjouwer.pickblockpro.util.*;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockApplyCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
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
        if (hit == null) {
            return;
        }

        ItemStack item = ClientPickBlockGatherCallback.EVENT.invoker().pick(client.player, hit);
        if (hit.getType() == HitResult.Type.ENTITY) {
            item = getEntityItemStack(hit, item);
        }

        if (hit.getType() == HitResult.Type.BLOCK && config.blockPickBlocks()) {
            item = getBlockItemStack(hit, item);
        }

        if (item.isEmpty() && hit.getType() == HitResult.Type.MISS && config.blockPickLight()) {
            //Do another raycast with a longer reach to make sure there is nothing in the way of the sun or moon
            int distance = client.options.getViewDistance().getValue() * 32;
            hit = Raycast.getHit(distance, !config.blockPickFluids(), false);
            if (hit.getType() == HitResult.Type.MISS) {
                item = getLightFromSunOrMoon();
            }
        }

        if (!item.isEmpty()) {
            item = ClientPickBlockApplyCallback.EVENT.invoker().pick(client.player, hit, item);
        }

        if (item.isEmpty()) {
            return;
        }

        InventoryManager.pickOrPlaceItemInInventory(item);
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
                NbtUtil.addEntityNbt(item, entity, true);
            }
        }

        if (entity instanceof PlayerEntity player) {
            item = new ItemStack(Items.PLAYER_HEAD);
            NbtUtil.setSkullOwner(item, player);
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
        itemFrameStack.setCustomName(name);

        NbtUtil.addEntityNbt(itemFrameStack, itemFrame, false);
        return itemFrameStack;
    }

    private static ItemStack createPaintingVariantStack(PaintingEntity painting) {
        ItemStack paintingStack = new ItemStack(Items.PAINTING);
        Optional<RegistryKey<PaintingVariant>> key = painting.getVariant().getKey();
        if (key.isPresent()) {
            String translationKey = key.get().getValue().toTranslationKey("painting", "title");
            MutableText name = Text.translatable(translationKey);
            name.setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.YELLOW));
            paintingStack.setCustomName(name);
        }

        NbtUtil.addEntityNbt(paintingStack, painting, false);
        return paintingStack;
    }

    private static ItemStack getBlockItemStack(HitResult hit, ItemStack item) {
        BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = client.world.getBlockState(blockPos);

        ItemStack override = PickBlockOverrides.getBlockOverride(state.getBlock());
        if (override != null) {
            item = override;
        }

        if (item.isEmpty()) {
            item = state.getBlock().getPickStack(client.world, blockPos, state);
        }

        if (item.isEmpty()) {
            return item;
        }

        if (client.player.getAbilities().creativeMode) {
            if (Screen.hasControlDown() && state.hasBlockEntity()) {
                BlockEntity blockEntity = client.world.getBlockEntity(blockPos);
                NbtUtil.addBlockEntityNbt(item, blockEntity, true);
            }
            if (Screen.hasAltDown()) {
                NbtUtil.addBlockStateNbt(item, state, true);
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

        return ItemStack.EMPTY;
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
