package io.github.sjouwer.pickblockpro.picker;

import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.util.*;
import me.shedaniel.autoconfig.AutoConfig;
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
    private final ModConfig config;
    private static final MinecraftClient minecraft = MinecraftClient.getInstance();

    public IdPicker() {
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

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
        BlockState block = minecraft.world.getBlockState(blockHit.getBlockPos());
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
        BaseText message = new LiteralText(finalId);

        if (config.copyToClipboard()){
            minecraft.keyboard.setClipboard(finalId);
            message = new TranslatableText("text.pick_block_pro.message.copied", finalId);
        }

        return message;
    }

    private BaseText getEntityId(HitResult hit) {
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
            BaseText message = new LiteralText(finalId);

            if (config.copyToClipboard()){
                minecraft.keyboard.setClipboard(finalId);
                message = new TranslatableText("text.pick_block_pro.message.copied", finalId);
            }

            return message;
        }

        return null;
    }
}
