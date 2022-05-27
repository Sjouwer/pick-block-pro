package io.github.sjouwer.pickblockpro.util;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.BaseText;
import net.minecraft.util.Formatting;

import static net.minecraft.text.Style.EMPTY;

public final class Chat {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private Chat() {
    }

    public static void sendMessage(BaseText message) {
        message.setStyle(EMPTY.withColor(Formatting.GREEN));
        send(message);
    }

    public static void sendError(BaseText errorMessage) {
        errorMessage.setStyle(EMPTY.withColor(Formatting.DARK_RED));
        send(errorMessage);
    }

    private static void send(BaseText message) {
        if (client.player == null) {
            PickBlockPro.LOGGER.error("Unable to send chat message/error to player; no player");
            return;
        }

        client.player.sendMessage(message, false);
    }
}
