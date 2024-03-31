package io.github.sjouwer.pickblockpro;

import io.github.sjouwer.pickblockpro.picker.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    private static final String CATEGORY = "key.categories." + PickBlockPro.NAMESPACE;
    private static final String BASE_KEY = "key." + PickBlockPro.NAMESPACE;

    private KeyBindings() {
    }

    public static void registerKeyBindings() {
        registerPickIdKey();
        registerPickToolKey();
    }

    private static void registerPickIdKey() {
        KeyBinding idPickerKey = new KeyBinding(BASE_KEY + ".idPicker", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
        KeyBindingHelper.registerKeyBinding(idPickerKey);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (idPickerKey.wasPressed()) {
                IdPicker.pickId();
            }
        });
    }

    private static void registerPickToolKey() {
        KeyBinding toolPickerKey = new KeyBinding(BASE_KEY + ".toolPicker", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
        KeyBindingHelper.registerKeyBinding(toolPickerKey);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toolPickerKey.wasPressed()) {
                ToolPicker.pickTool();
            }
        });
    }
}
