package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.util.*;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
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
        if (hit == null || client.world == null) {
            return;
        }

        MutableText message = null;
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

    private MutableText getBlockId(HitResult hit) {
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockState block = client.world.getBlockState(blockHit.getBlockPos());
        String fullId = block.toString();

        String namespace = "";
        if (config.addNamespace() && fullId.indexOf('{') >= 0 && fullId.indexOf(':') >= 0) {
            namespace = fullId.substring(fullId.indexOf('{') + 1, fullId.indexOf(':') + 1);
        }

        String id = "";
        if (fullId.indexOf(':') >= 0 && fullId.indexOf('}') >= 0) {
            id = fullId.substring(fullId.indexOf(':') + 1, fullId.indexOf('}'));
        }

        String properties = "";
        if (config.addProperties() && fullId.indexOf('[') >= 0) {
            properties = fullId.substring(fullId.indexOf('['));
        }

        String finalId = namespace + id + properties;
        MutableText message = MutableText.method_43477(new LiteralText(finalId));

        if (config.copyToClipboard()){
            client.keyboard.setClipboard(finalId);
            message = MutableText.method_43477(new TranslatableText("text.pick_block_pro.message.copied", finalId));
        }

        return message;
    }

    private MutableText getEntityId(HitResult hit) {
        EntityHitResult entityHit = (EntityHitResult) hit;
        Entity entity = entityHit.getEntity();
        String fullId = EntityType.getId(entity.getType()).toString();

        if (fullId.indexOf(':') >= 0) {
            String namespace = "";
            if (config.addNamespace()) {
                namespace = fullId.substring(0, fullId.indexOf(':') + 1);
            }

            String id = fullId.substring(fullId.indexOf(':') + 1);
            String finalId = namespace + id;
            MutableText message = MutableText.method_43477(new LiteralText(finalId));

            if (config.copyToClipboard()){
                client.keyboard.setClipboard(finalId);
                message = MutableText.method_43477(new TranslatableText("text.pick_block_pro.message.copied", finalId));
            }

            return message;
        }

        return null;
    }
}
