package io.github.sjouwer.pickblockpro;

import io.github.sjouwer.pickblockpro.config.PickBlockOverrides;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class Commands {
    private Commands() {
    }

    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("pbp")
                        .then(literal("reload")
                                .executes(ctx -> {
                                    PickBlockOverrides.parseOverrides();
                                    return 1;
                                }))));
    }
}
