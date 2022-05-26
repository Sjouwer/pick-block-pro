package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.util.*;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.registry.Registry;

import java.util.StringJoiner;

public class IdPicker {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final ModConfig config = PickBlockPro.getConfig();

    private IdPicker() {
    }

    /**
     * Provide to the player with the configured ID of the block or entity they are looking at
     */
    public static void pickId() {
        if (!config.idPickEntities() && !config.idPickBlocks()) {
            Chat.sendError(new TranslatableText("text.pick_block_pro.message.nothingToPick"));
            return;
        }

        HitResult hit = Raycast.getHit(config.idPickRange(), !config.idPickFluids(), !config.idPickEntities());

        String id = "";
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            id = getEntityId(entity);
        }

        if (hit instanceof BlockHitResult blockHit && config.idPickBlocks() && client.world != null) {
            BlockState state = client.world.getBlockState(blockHit.getBlockPos());
            id = getBlockId(state);
        }

        if (id.isEmpty()) {
            return;
        }

        BaseText message;
        if (config.copyToClipboard()){
            client.keyboard.setClipboard(id);
            message = new TranslatableText("text.pick_block_pro.message.copied", id);
        }
        else {
            message = new LiteralText(id);
        }

        Chat.sendMessage(message);
    }

    /**
     * Method to get the configured ID of a block
     * @param blockState Block to get the ID from
     * @return ID as String
     */
    public static String getBlockId(BlockState blockState) {
        if (blockState == null) {
            return "";
        }

        StringBuilder fullId = new StringBuilder();
        fullId.append(Registry.BLOCK.getId(blockState.getBlock()));

        if (!config.addNamespace()) {
            fullId.delete(0, fullId.indexOf(":") + 1);
        }

        if (config.addProperties() && !blockState.getProperties().isEmpty()) {
            String tmp = blockState.toString();
            fullId.append(tmp.substring(tmp.indexOf("[")));
        }

        return fullId.toString();
    }

    /**
     * Method to get the configured ID of an entity
     * @param entity Entity to get the ID from
     * @return ID as String
     */
    public static String getEntityId(Entity entity) {
        if (entity == null) {
            return "";
        }

        String fullId = Registry.ENTITY_TYPE.getId(entity.getType()).toString();

        if (!config.addNamespace() && fullId.contains(":")) {
            fullId = fullId.substring(fullId.indexOf(":") + 1);
        }

        return fullId;
    }

    /**
     * Method to get the configured ID of an item
     * @param itemStack Item to get the ID from
     * @return ID as String
     */
    public static String getItemId(ItemStack itemStack) {
        if (itemStack == null) {
            return "";
        }

        ModConfig config = PickBlockPro.getConfig();
        StringBuilder fullId = new StringBuilder();

        if (config.addNamespace()) {
            fullId.append(Registry.ITEM.getId(itemStack.getItem()).getNamespace());
            fullId.append(":");
        }

        fullId.append(itemStack.getItem());

        if (config.addProperties()) {
            NbtCompound statesTag = itemStack.getSubNbt("BlockStateTag");
            if (statesTag != null) {
                StringJoiner stateJoiner = new StringJoiner(",");
                for (String key : statesTag.getKeys()) {
                    stateJoiner.add(key + "=" + statesTag.getString(key));
                }
                fullId.append("[");
                fullId.append(stateJoiner);
                fullId.append("]");
            }
        }

        return fullId.toString();
    }
}
