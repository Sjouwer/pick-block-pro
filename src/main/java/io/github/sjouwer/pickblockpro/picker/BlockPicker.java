package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.util.*;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.state.property.Property;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BlockPicker {
    private static BlockPicker INSTANCE;
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final ModConfig config = PickBlockPro.getConfig();

    public static BlockPicker getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new BlockPicker();
        }

        return INSTANCE;
    }

    public void pickBlock() {
        if (!config.blockPickEntities() && !config.blockPickBlocks()) {
            Chat.sendError(new TranslatableText("text.pick_block_pro.message.nothingToPick"));
            return;
        }

        HitResult hit = Raycast.getHit(config.blockPickRange(), config.blockFluidHandling(), !config.blockPickEntities());
        if (hit == null || client.world == null || client.player == null) {
            return;
        }

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
            hit = Raycast.getHit(distance, config.blockFluidHandling(), false);
            if (hit.getType() == HitResult.Type.MISS) {
                item = getLightFromSunOrMoon();
            }
        }

        if (item != null) {
            Inventory.placeItemInsideInventory(item);
        }
    }

    private ItemStack getEntityItemStack(HitResult hit) {
        Entity entity = ((EntityHitResult) hit).getEntity();
        return entity.getPickBlockStack();
    }

    private ItemStack getBlockItemStack(HitResult hit) {
        BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = client.world.getBlockState(blockPos);
        ItemStack item = state.getBlock().getPickStack(client.world, blockPos, state);

        if (item.isEmpty()) {
            ItemStack extraItem = extraPickStackCheck(state);
            if (extraItem == null) {
                return null;
            }
            item = extraItem;
        }

        if (client.player.getAbilities().creativeMode) {
            if (Screen.hasControlDown() && state.hasBlockEntity()) {
                BlockEntity blockEntity = client.world.getBlockEntity(blockPos);
                addBlockEntityNbt(item, blockEntity);
            }
            if (Screen.hasAltDown()) {
                addBlockStateNbt(item, state);
            }
        }

        return item;
    }

    private ItemStack extraPickStackCheck(BlockState state) {
        if (state.isOf(Blocks.WATER)) {
            return new ItemStack(Items.WATER_BUCKET);
        }
        if (state.isOf(Blocks.LAVA)) {
            return new ItemStack(Items.LAVA_BUCKET);
        }
        if ((state.isOf(Blocks.FIRE) || (state.isOf(Blocks.SOUL_FIRE))) && config.blockPickFire()) {
            return new ItemStack(Items.FLINT_AND_STEEL);
        }
        if (state.isOf(Blocks.SPAWNER)) {
            return new ItemStack(Items.SPAWNER);
        }

        return null;
    }

    private ItemStack getLightFromSunOrMoon() {
        double skyAngle = client.world.getSkyAngle(client.getTickDelta()) + .25;
        if (skyAngle > 1) {
            skyAngle --;
        }
        skyAngle *= 360;

        Vec3d playerVector = client.cameraEntity.getRotationVec(client.getTickDelta());
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

    private ItemStack giveOrCycleLight(int lightLvl) {
        ItemStack mainHandStack = client.player.getMainHandStack();
        if (mainHandStack.isOf(Items.LIGHT)) {
            cycleLightLevel(mainHandStack);
        }
        else {
            ItemStack light = new ItemStack(Items.LIGHT);
            NbtCompound blockStateTag = new NbtCompound();
            blockStateTag.putInt("level", lightLvl);
            light.setSubNbt("BlockStateTag", blockStateTag);
            return light;
        }

        return null;
    }

    private void cycleLightLevel(ItemStack light) {
        NbtCompound blockStateTag = light.getSubNbt("BlockStateTag");
        int newLightLvl;

        if (blockStateTag == null) {
            blockStateTag = new NbtCompound();
            newLightLvl = 0;
        }
        else {
            newLightLvl = blockStateTag.getInt("level") + 1;
        }
        if (newLightLvl == 16) {
            newLightLvl = 0;
        }

        blockStateTag.putInt("level", newLightLvl);
        light.setSubNbt("BlockStateTag", blockStateTag);

        PlayerInventory inventory = client.player.getInventory();
        inventory.setStack(inventory.selectedSlot, light);
        Inventory.updateCreativeSlot(inventory.selectedSlot);
    }


    private void addBlockEntityNbt(ItemStack stack, BlockEntity blockEntity) {
        NbtCompound nbtCompound = blockEntity.createNbtWithIdentifyingData();
        NbtCompound nbtCompound3;
        if (stack.getItem() instanceof SkullItem && nbtCompound.contains("SkullOwner")) {
            nbtCompound3 = nbtCompound.getCompound("SkullOwner");
            stack.getOrCreateNbt().put("SkullOwner", nbtCompound3);
        } else {
            stack.setSubNbt("BlockEntityTag", nbtCompound);
            addNbtTag(stack, "\"(+BlockEntity NBT)\"");
        }
    }

    private void addBlockStateNbt(ItemStack stack, BlockState state) {
        NbtCompound nbtCompound = new NbtCompound();
        if (!state.getProperties().isEmpty()) {
            for (Property<?> property : state.getProperties()) {
                nbtCompound.putString(property.getName(), state.get(property).toString());
            }
            stack.setSubNbt("BlockStateTag", nbtCompound);
            addNbtTag(stack, "\"(+BlockState NBT)\"");
        }
    }

    private void addNbtTag(ItemStack stack, String tag) {
        NbtCompound nbtCompound = stack.getOrCreateSubNbt("display");
        NbtList nbtList = nbtCompound.getList("Lore", NbtType.STRING);
        if (nbtList == null) {
            nbtList = new NbtList();
        }
        nbtList.add(NbtString.of(tag));
        nbtCompound.put("Lore", nbtList);
        stack.setSubNbt("display", nbtCompound);
    }
}
