package io.github.sjouwer.pickblockpro.mixin;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.glfw.GLFW;

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

            ((ChatScreenAccessor) screen).getChatField().write(itemId);
            info.cancel();
        }
    }

    private String getItemId(int slot) {
        ModConfig config = PickBlockPro.getConfig();
        Item item = client.player.getInventory().getStack(slot).getItem();
        String id = item.toString();
        String key = item.getTranslationKey();
        String namespace = "";

        if (config.addNamespace() && key.indexOf('.') >= 0 && key.indexOf(".", key.indexOf(".") + 1) >= 0) {
            namespace = key.substring(key.indexOf('.') + 1, key.indexOf(".", key.indexOf(".") + 1)) + ":";
        }

        return namespace + id;
    }
}
