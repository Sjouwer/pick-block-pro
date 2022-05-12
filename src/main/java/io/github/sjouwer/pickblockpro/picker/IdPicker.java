package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.util.*;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class IdPicker {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final ModConfig config = PickBlockPro.getConfig();

    public void pickId() {
        if (!config.idPickEntities() && !config.idPickBlocks()) {
            Chat.sendError(new TranslatableText("text.pick_block_pro.message.nothingToPick"));
            return;
        }

        HitResult hit = Raycast.getHit(config.idPickRange(), config.idFluidHandling(), !config.idPickEntities());

        BaseText message = null;
        if (hit.getType() == HitResult.Type.ENTITY) {
            message = getEntityId(hit);
        }
        else if (config.idPickBlocks()) {
            message = getBlockId(hit);
        }

        if (message != null) {
            Chat.sendMessage(message);
        }
    }

    private BaseText getBlockId(HitResult hit) {
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockState block = client.world.getBlockState(blockHit.getBlockPos());
        String fullId = block.toString();

        int namespaceStart = fullId.indexOf("{") + 1;
        int namespaceEnd = fullId.indexOf(":") + 1;
        int idEnd = fullId.indexOf("}");
        int propertiesStart = fullId.indexOf("[");
        if (namespaceStart <= 0 || namespaceEnd < namespaceStart || idEnd < namespaceEnd) {
            return null;
        }

        String id = fullId.substring(namespaceEnd, idEnd);

        String namespace = "";
        if (config.addNamespace()) {
            namespace = fullId.substring(namespaceStart, namespaceEnd);
        }

        String properties = "";
        if (config.addProperties() && propertiesStart > idEnd) {
            properties = fullId.substring(propertiesStart);
        }

        String finalId = namespace + id + properties;
        BaseText message = new LiteralText(finalId);

        if (config.copyToClipboard()){
            client.keyboard.setClipboard(finalId);
            message = new TranslatableText("text.pick_block_pro.message.copied", finalId);
        }

        return message;
    }

    private BaseText getEntityId(HitResult hit) {
        EntityHitResult entityHit = (EntityHitResult) hit;
        Entity entity = entityHit.getEntity();
        String fullId = EntityType.getId(entity.getType()).toString();

        int colonIndex = fullId.indexOf(":") + 1;
        if (colonIndex > 0) {
            String namespace = "";
            if (config.addNamespace()) {
                namespace = fullId.substring(0, colonIndex);
            }

            String id = fullId.substring(colonIndex);
            String finalId = namespace + id;
            BaseText message = new LiteralText(finalId);

            if (config.copyToClipboard()){
                client.keyboard.setClipboard(finalId);
                message = new TranslatableText("text.pick_block_pro.message.copied", finalId);
            }

            return message;
        }

        return null;
    }
}
