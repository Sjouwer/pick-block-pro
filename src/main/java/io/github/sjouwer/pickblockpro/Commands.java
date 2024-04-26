package io.github.sjouwer.pickblockpro;

import io.github.sjouwer.pickblockpro.config.PickBlockOverrides;
import io.github.sjouwer.pickblockpro.picker.ToolPicker;
import io.github.sjouwer.pickblockpro.picker.ToolPicker.Tools;
import io.github.sjouwer.pickblockpro.picker.WeaponPicker;
import io.github.sjouwer.pickblockpro.picker.WeaponPicker.Weapons;
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

        for (Tools tool : Tools.values()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                    dispatcher.register(literal("pbp")
                            .then(literal("give")
                                    .then(literal(tool.name().toLowerCase())
                                        .executes(ctx -> {
                                            ToolPicker.giveTool(tool);
                                            return 1;
                                        })))));
        }

        for (Weapons weapon : Weapons.values()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                    dispatcher.register(literal("pbp")
                            .then(literal("give")
                                    .then(literal(weapon.name().toLowerCase())
                                            .executes(ctx -> {
                                                WeaponPicker.giveWeapon(weapon);
                                                return 1;
                                            })))));
        }
    }
}
