package io.github.sjouwer.pickblockpro;

import io.github.sjouwer.pickblockpro.picker.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    private final IdPicker idPicker = new IdPicker();
    private final ToolPicker toolPicker = new ToolPicker();
    private static final String CATEGORY = "key.categories.pick_block_pro";

    public void setKeyBindings() {
        setKeyBindingPickId();
        setKeyBindingPickTool();
    }

    private void setKeyBindingPickId() {
        KeyBinding idPickerKey = new KeyBinding("key.pick_block_pro.id_picker", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
        KeyBindingHelper.registerKeyBinding(idPickerKey);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (idPickerKey.wasPressed()) {
                idPicker.pickId();
            }
        });
    }

    private void setKeyBindingPickTool() {
        KeyBinding toolPickerKey = new KeyBinding("key.pick_block_pro.tool_picker", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
        KeyBindingHelper.registerKeyBinding(toolPickerKey);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toolPickerKey.wasPressed()) {
                toolPicker.pickTool();
            }
        });
    }
}
