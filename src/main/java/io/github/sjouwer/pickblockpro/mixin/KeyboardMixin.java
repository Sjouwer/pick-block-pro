package io.github.sjouwer.pickblockpro.mixin;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.glfw.GLFW;

import java.util.StringJoiner;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int key, int scancode, int action, int modifiers, final CallbackInfo info) {
        Screen screen = client.currentScreen;
        if (screen instanceof ChatScreen && Screen.hasControlDown() && action == GLFW.GLFW_PRESS && key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
            String itemId;
            if (key == GLFW.GLFW_KEY_0) {
                itemId = getItemId(PlayerInventory.OFF_HAND_SLOT);
            }
            else {
                itemId = getItemId(key - GLFW.GLFW_KEY_1);
            }

            if (itemId != null) {
                ((ChatScreenAccessor) screen).getChatField().write(itemId);
                info.cancel();
            }
        }
    }

    private String getItemId(int slot) {
        if (client.player == null) {
            return null;
        }

        ModConfig config = PickBlockPro.getConfig();
        ItemStack itemStack = client.player.getInventory().getStack(slot);
        String id = itemStack.getItem().toString();

        String namespace = "";
        if (config.addNamespace()) {
            String key = itemStack.getItem().getTranslationKey();
            int namespaceStart = key.indexOf(".") + 1;
            int namespaceEnd = key.indexOf(".", namespaceStart);
            if (namespaceStart > 0 && namespaceEnd > namespaceStart) {
                namespace = key.substring(namespaceStart, namespaceEnd) + ":";
            }
        }

        String properties = "";
        if (config.addProperties()) {
            NbtCompound statesTag = itemStack.getSubNbt("BlockStateTag");
            if(statesTag != null) {
                StringJoiner stateJoiner = new StringJoiner(",");
                for (String key : statesTag.getKeys()) {
                    stateJoiner.add(key + "=" + statesTag.getString(key));
                }
                properties = "[" + stateJoiner + "]";
            }
        }

        return namespace + id + properties;
    }
}