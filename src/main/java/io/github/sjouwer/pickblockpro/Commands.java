package io.github.sjouwer.pickblockpro;

import io.github.sjouwer.pickblockpro.config.PickBlockOverrides;
import io.github.sjouwer.pickblockpro.util.InfoProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class Commands {
    private Commands() {
    }

    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("pbp")
                        .then(literal("reload")
                                .executes(ctx -> {
                                    if (!PickBlockOverrides.parseOverrides()) {
                                        InfoProvider.sendError(Text.literal("Failed to reload overrides, see log file for more info"));
                                    }
                                    return 1;
                                }))));
    }
}
