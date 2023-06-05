package io.github.sjouwer.pickblockpro.util;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class InfoProvider {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private InfoProvider() {
    }

    public static void sendMessage(Text message) {
        if (client.player == null) {
            PickBlockPro.LOGGER.info(message.getString());
            return;
        }

        client.player.sendMessage(message, false);
    }

    public static void sendWarning(MutableText warningMessage) {
        if (client.player == null) {
            PickBlockPro.LOGGER.warn(warningMessage.getString());
            return;
        }

        warningMessage.formatted(Formatting.GOLD);
        client.player.sendMessage(warningMessage, false);
    }

    public static void sendError(MutableText errorMessage) {
        if (client.player == null) {
            PickBlockPro.LOGGER.error(errorMessage.getString());
            return;
        }

        errorMessage.formatted(Formatting.DARK_RED);
        client.player.sendMessage(errorMessage, false);
    }
}
