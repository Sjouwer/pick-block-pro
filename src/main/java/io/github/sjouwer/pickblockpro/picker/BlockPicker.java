package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.util.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
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
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;

public class BlockPicker {
    private final ModConfig config;
    private static final MinecraftClient minecraft = MinecraftClient.getInstance();

    public BlockPicker() {
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public void pickBlock() {
        if (!config.blockPickEntities() && !config.blockPickBlocks()) {
            Chat.sendError(new TranslatableText("text.pick_block_pro.message.nothingToPick"));
            return;
        }

        HitResult hit = Raycast.getHit(config.blockPickRange(), config.blockFluidHandling(), !config.blockPickEntities());

        ItemStack item = null;
        //Check first if there is an entity in sight
        if (hit.getType() == HitResult.Type.ENTITY) {
            item = getEntityItemStack(hit);
        }
        //If there is no entity in sight check for a block instead
        else if (config.blockPickBlocks()) {
            item = getBlockItemStack(hit);
        }

        if (item != null) {
            Inventory.placeItemInsideInventory(item, config);
        }
    }

    private ItemStack getEntityItemStack(HitResult hit) {
        EntityHitResult entityHit = (EntityHitResult) hit;
        Entity entity = entityHit.getEntity();
        return entity.getPickBlockStack();
    }

    private ItemStack getBlockItemStack(HitResult hit) {
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos blockPos = blockHit.getBlockPos();
        BlockView world = minecraft.world;
        BlockState state = world.getBlockState(blockPos);

        ItemStack item;
        if (state.isAir()) {
            item = getLightFromSun();

            if (item == null) {
                return null;
            }
        }
        else {
            item = state.getBlock().getPickStack(world, blockPos, state);
        }

        if (item.isEmpty()) {
            //Check for extra pick stacks that mc doesn't include by default
            ItemStack extraItem = extraPickStackCheck(state);
            if (extraItem == null) {
                return null;
            }
            item = extraItem;
        }

        if (minecraft.player.getAbilities().creativeMode) {
            //Add BlockEntity NBT if ctrl is being held down
            if (Screen.hasControlDown() && state.hasBlockEntity()) {
                BlockEntity blockEntity = world.getBlockEntity(blockPos);
                if (blockEntity != null) {
                    addBlockEntityNbt(item, blockEntity);
                }
            }
            //Add BlockState NBT if alt is being held down
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
        if ((state.isOf(Blocks.FIRE) || (state.isOf(Blocks.SOUL_FIRE)) && config.blockPickFire())) {
            return new ItemStack(Items.FLINT_AND_STEEL);
        }

        return null;
    }

    private ItemStack getLightFromSun() {
        int viewDistance = minecraft.options.viewDistance * 32;
        HitResult hit = Raycast.getHit(viewDistance, RaycastContext.FluidHandling.ANY, false);
        if (hit.getType() == HitResult.Type.ENTITY) {
            return null;
        }

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockState state = minecraft.world.getBlockState(blockHit.getBlockPos());

        if (state.isAir()) {
            double skyAngle = minecraft.world.getSkyAngle(minecraft.getTickDelta()) + .25;
            if (skyAngle > 1) {
                skyAngle --;
            }
            skyAngle *= 360;

            Vec3d playerVector = minecraft.cameraEntity.getRotationVec(minecraft.getTickDelta());
            double playerAngle = Math.atan2(playerVector.y,playerVector.x) * 180 / Math.PI;
            if (playerAngle < 0) {
                playerAngle += 360;
            }

            double angleDifference = skyAngle - playerAngle;
            if (Math.abs(playerVector.z) < 0.076 && Math.abs(angleDifference) < 4.3) {
                return new ItemStack(Items.LIGHT);
            }
        }

        return null;
    }

    private void addBlockEntityNbt(ItemStack stack, BlockEntity blockEntity) {
        NbtCompound nbtCompound = blockEntity.writeNbt(new NbtCompound());
        NbtCompound nbtCompound3;
        if (stack.getItem() instanceof SkullItem && nbtCompound.contains("SkullOwner")) {
            nbtCompound3 = nbtCompound.getCompound("SkullOwner");
            stack.getOrCreateNbt().put("SkullOwner", nbtCompound3);
        } else {
            stack.setSubNbt("BlockEntityTag", nbtCompound);
            addNbtTag(stack);
        }
    }

    private void addBlockStateNbt(ItemStack stack, BlockState state) {
        NbtCompound nbtCompound = new NbtCompound();
        if (!state.getProperties().isEmpty()) {
            for (Property<?> property : state.getProperties()) {
                nbtCompound.putString(property.getName(), state.get(property).toString());
            }
            stack.setSubNbt("BlockStateTag", nbtCompound);
            addNbtTag(stack);
        }
    }

    private void addNbtTag(ItemStack stack) {
        NbtCompound nbtCompound = new NbtCompound();
        NbtList nbtList = new NbtList();
        nbtList.add(NbtString.of("\"(+NBT)\""));
        nbtCompound.put("Lore", nbtList);
        stack.setSubNbt("display", nbtCompound);
    }
}
