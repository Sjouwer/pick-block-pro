package io.github.sjouwer.pickblockpro.util;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public final class Chat {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private Chat() {
    }

    public static void sendMessage(MutableText message) {
        message.setStyle(Style.EMPTY.withColor(Formatting.GREEN));
        send(message);
    }

    public static void sendError(MutableText errorMessage) {
        errorMessage.setStyle(Style.EMPTY.withColor(Formatting.DARK_RED));
        send(errorMessage);
    }

    private static void send(MutableText message) {
        if (client.player == null) {
            PickBlockPro.LOGGER.error("Unable to send chat message/error to player; no player");
            return;
        }

        client.player.sendMessage(message, false);
    }
}
