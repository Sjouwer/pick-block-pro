package io.github.sjouwer.pickblockpro.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextContent;
import net.minecraft.util.Formatting;

import static net.minecraft.text.Style.EMPTY;

public final class Chat {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private Chat() {
    }

    public static void sendMessage(TextContent messageContent) {
        MutableText message = MutableText.of(messageContent);
        message.setStyle(EMPTY.withColor(Formatting.GREEN));

        if (client.player != null) {
            client.player.sendMessage(message, false);
        }
    }

    public static void sendError(TextContent messageContent) {
        MutableText errorMessage = MutableText.of(messageContent);
        errorMessage.setStyle(EMPTY.withColor(Formatting.DARK_RED));

        if (client.player != null) {
            client.player.sendMessage(errorMessage, false);
        }
    }
}
