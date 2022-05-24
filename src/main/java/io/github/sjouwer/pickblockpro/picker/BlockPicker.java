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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

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
            Chat.sendError(Text.translatable("text.pick_block_pro.message.nothingToPick"));
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
        ItemStack item = entity.getPickBlockStack();
        if (item != null && client.player.getAbilities().creativeMode && Screen.hasControlDown()) {
            if (entity instanceof ItemFrameEntity) {
                ItemStack itemFrame = new ItemStack(Items.ITEM_FRAME);
                itemFrame.setCustomName(Text.translatable("text.pick_block_pro.name.framed", item.getName()));
                item = itemFrame;
            }

            if (entity instanceof PaintingEntity paintingEntity) {
                String key = "painting." + Registry.PAINTING_VARIANT.getId(paintingEntity.getVariant().value()).toString().replace(":", ".");
                item.setCustomName(Text.translatable(key));
            }

            addEntityNbt(item, entity);
        }

        if (entity instanceof PlayerEntity player) {
            item = new ItemStack(Items.PLAYER_HEAD);
            item.getOrCreateNbt().putString("SkullOwner", player.getEntityName());
        }

        return item;
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

    private void addEntityNbt(ItemStack stack, Entity entity) {
        NbtCompound entityCompound = entity.writeNbt(new NbtCompound());
        entityCompound.remove("UUID");
        entityCompound.remove("Pos");
        entityCompound.remove("TileX");
        entityCompound.remove("TileY");
        entityCompound.remove("TileZ");
        entityCompound.remove("Facing");
        entityCompound.remove("facing");
        entityCompound.remove("Rotation");
        entityCompound.remove("Leash");

        if (entity instanceof HorseEntity horse && horse.hasArmorInSlot()) {
            NbtCompound armorCompound = new NbtCompound();
            armorCompound.putString("id", horse.getArmorType().getItem().toString());
            armorCompound.putInt("Count", 1);
            entityCompound.put("ArmorItem", armorCompound);
        }

        if (entity instanceof Saddleable saddleable && saddleable.isSaddled()) {
            NbtCompound saddleCompound = new NbtCompound();
            saddleCompound.putString("id", Items.SADDLE.toString());
            saddleCompound.putInt("Count", 1);
            entityCompound.put("SaddleItem", saddleCompound);
        }

        if (entity instanceof LlamaEntity llama && llama.getCarpetColor() != null) {
            NbtCompound decorCompound = new NbtCompound();
            decorCompound.putString("id", llama.getCarpetColor() + "_carpet");
            decorCompound.putInt("Count", 1);
            entityCompound.put("DecorItem", decorCompound);
        }

        Identifier identifier = EntityType.getId(entity.getType());
        entityCompound.putString("id", identifier.toString());
        stack.setSubNbt("EntityTag", entityCompound);
        addNbtLore(stack, "\"(+Entity NBT)\"");
    }

    private void addBlockEntityNbt(ItemStack stack, BlockEntity blockEntity) {
        NbtCompound nbtCompound = blockEntity.createNbtWithIdentifyingData();
        if (stack.getItem() instanceof SkullItem && nbtCompound.contains("SkullOwner")) {
            NbtCompound skullCompound = nbtCompound.getCompound("SkullOwner");
            stack.getOrCreateNbt().put("SkullOwner", skullCompound);
        } else {
            stack.setSubNbt("BlockEntityTag", nbtCompound);
            addNbtLore(stack, "\"(+BlockEntity NBT)\"");
        }
    }

    private void addBlockStateNbt(ItemStack stack, BlockState state) {
        NbtCompound nbtCompound = new NbtCompound();
        if (!state.getProperties().isEmpty()) {
            for (Property<?> property : state.getProperties()) {
                nbtCompound.putString(property.getName(), state.get(property).toString());
            }
            stack.setSubNbt("BlockStateTag", nbtCompound);
            addNbtLore(stack, "\"(+BlockState NBT)\"");
        }
    }

    private void addNbtLore(ItemStack stack, String tag) {
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
