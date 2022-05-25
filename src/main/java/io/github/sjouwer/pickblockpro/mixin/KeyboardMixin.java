package io.github.sjouwer.pickblockpro.mixin;

import io.github.sjouwer.pickblockpro.picker.IdPicker;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.glfw.GLFW;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void getHotbarItemId(long window, int key, int scancode, int action, int modifiers, final CallbackInfo info) {
        Screen screen = client.currentScreen;
        if (screen instanceof ChatScreen &&
                Screen.hasControlDown() &&
                action == GLFW.GLFW_PRESS &&
                key >= GLFW.GLFW_KEY_0 &&
                key <= GLFW.GLFW_KEY_9 &&
                client.player != null) {

            ItemStack itemStack = client.player.getInventory().getStack(key == GLFW.GLFW_KEY_0 ? PlayerInventory.OFF_HAND_SLOT : key - GLFW.GLFW_KEY_1);
            String itemId = IdPicker.getItemId(itemStack);

            if (itemId != null) {
                ((ChatScreenAccessor) screen).getChatField().write(itemId);
                info.cancel();
            }
        }
    }
}
