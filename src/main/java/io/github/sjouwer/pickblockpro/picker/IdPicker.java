package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.mixin.BucketItemAccessor;
import io.github.sjouwer.pickblockpro.mixin.VerticallyAttachableBlockItemAccessor;
import io.github.sjouwer.pickblockpro.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.EntityBucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.VerticallyAttachableBlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class IdPicker {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final ModConfig config = PickBlockPro.getConfig();

    private IdPicker() {
    }

    /**
     * Provide the player with the configured ID of the block or entity they are looking at
     */
    public static void pickId() {
        if (client.player == null || client.world == null) {
            PickBlockPro.LOGGER.error("Pick ID called outside of play; no world and/or player");
            return;
        }

        if (!config.idPickEntities() && !config.idPickBlocks()) {
            InfoProvider.sendError(Text.translatable("text.pick_block_pro.message.nothingToPick"));
            return;
        }

        HitResult hit = RaycastUtil.getHit(config.blockIdPickRange(client.player), config.entityIdPickRange(client.player), !config.idPickFluids(), !config.idPickEntities());
        if (hit == null) {
            return;
        }

        Text id = Text.empty();
        if (hit instanceof EntityHitResult entityHit) {
            id = getEntityDataAsText(entityHit.getEntity());
        }

        if (hit instanceof BlockHitResult blockHit && config.idPickBlocks()) {
            id = getBlockDataAsText(blockHit.getBlockPos());
        }

        if (id.getString().isEmpty()) {
            return;
        }

        if (config.copyToClipboard()){
            client.keyboard.setClipboard(id.getString());
            InfoProvider.sendMessage(Text.translatable("text.pick_block_pro.message.copied").formatted(Formatting.DARK_GREEN));
        }

        InfoProvider.sendMessage(id);
    }

    private static Text getEntityDataAsText(Entity entity) {
        return (Screen.hasControlDown() && config.tagPickerEnabled()) ? getEntityTag(entity) : getEntityId(entity);
    }

    private static Text getBlockDataAsText(BlockPos blockPos) {
        BlockState blockState = client.world.getBlockState(blockPos);
        if (Screen.hasControlDown() && config.tagPickerEnabled() && blockState.hasBlockEntity()) {
            BlockEntity blockEntity = client.world.getBlockEntity(blockPos);
            return getBlockEntityTag(blockEntity);
        }

        return getBlockId(blockState);
    }

    /**
     * Method to get the configured ID of a block
     * @param blockState Block to get the ID from
     * @return ID as Text
     */
    public static Text getBlockId(BlockState blockState) {
        String fullId = Registries.BLOCK.getId(blockState.getBlock()).toString();

        if (!config.addNamespace()) {
            fullId = fullId.substring(fullId.indexOf(":") + 1);
        }

        if (config.addProperties() && !blockState.getProperties().isEmpty()) {
            return Text.literal(fullId).append(getBlockStateTag(blockState));
        }

        return Text.literal(fullId);
    }

    /**
     * Method to get the configured tag of a BlockState
     * @param blockState BlockState to get the tag from
     * @return Tag as Text
     */
    public static Text getBlockStateTag(BlockState blockState) {
        Map<String, String> properties = new HashMap<>();
        for (Property<?> property : blockState.getProperties()) {
            String key = property.getName();
            if (!config.blockStateTagIdBlacklist().contains(key)) {
                String value = blockState.get(property).toString();
                properties.put(key, value);
            }
        }

        return Text.literal(convertBlockState(new BlockStateComponent(properties)));
    }

    /**
     * Method to get the configured tag of a BlockEntity
     * @param blockEntity BlockEntity to get the tag from
     * @return Tag as Text
     */
    public static Text getBlockEntityTag(BlockEntity blockEntity) {
        NbtCompound tag = blockEntity.createNbt(client.world.getRegistryManager());
        config.blockEntityTagIdBlacklist().forEach(tag::remove);

        return config.prettyTagEnabled() ? NbtHelper.toPrettyPrintedText(tag) : Text.literal(tag.toString());
    }

    /**
     * Method to get the configured ID of an entity
     * @param entity Entity to get the ID from
     * @return ID as Text
     */
    public static Text getEntityId(Entity entity) {
        String fullId = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
        if (!config.addNamespace() && fullId.contains(":")) {
            fullId = fullId.substring(fullId.indexOf(":") + 1);
        }

        return Text.literal(fullId);
    }

    /**
     * Method to get the configured tag of an entity
     * @param entity Entity to get the tag from
     * @return Tag as Text
     */
    public static Text getEntityTag(Entity entity) {
        NbtCompound entityTag = DataComponentUtil.getEntityNbt(entity);
        config.entityTagIdBlacklist().forEach(entityTag::remove);
        return config.prettyTagEnabled() ? NbtHelper.toPrettyPrintedText(entityTag) : Text.literal(entityTag.toString());
    }

    /**
     * Method to get the configured ID of an item
     * @param itemStack Item to get the ID from
     * @return ID as String
     */
    public static String getItemId(ItemStack itemStack) {
        BlockStateComponent blockState = itemStack.getComponents().get(DataComponentTypes.BLOCK_STATE);

        String fullId = "";
        if (config.convertItemToBlock()) {
            fullId = convertToBlockId(itemStack, blockState);
        }

        if (fullId.isEmpty()) {
            fullId = Registries.ITEM.getId(itemStack.getItem()).toString();
        }

        if (!config.addNamespace()) {
            fullId = fullId.substring(fullId.indexOf(":") + 1);
        }

        if (config.addProperties() && blockState != null) {
            fullId = fullId + convertBlockState(blockState);
        }

        return fullId;
    }

    private static String convertBlockState(BlockStateComponent blockState) {
        StringBuilder properties = new StringBuilder();
        StringJoiner stateJoiner = new StringJoiner(",");

        blockState.properties().forEach((key, value) -> stateJoiner.add(key + "=" + value));

        properties.append("[");
        properties.append(stateJoiner);
        properties.append("]");

        return properties.toString();
    }

    private static String convertToBlockId(ItemStack itemStack, BlockStateComponent blockState) {
        String id = "";

        Item item = itemStack.getItem();
        if (item instanceof BucketItem && !(item instanceof EntityBucketItem)) {
            Fluid fluid = ((BucketItemAccessor) item).getFluid();
            id = Registries.FLUID.getId(fluid).toString();
        }
        else {
            Block block;
            if (blockState != null && blockState.properties().containsKey("facing") && item instanceof VerticallyAttachableBlockItem) {
                block = ((VerticallyAttachableBlockItemAccessor) item).getWallBlock();
            }
            else {
                block = Block.getBlockFromItem(item);
            }

            if (block != Blocks.AIR) {
                id = Registries.BLOCK.getId(block).toString();
            }
        }

        return id;
    }
}
