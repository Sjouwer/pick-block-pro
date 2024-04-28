package io.github.sjouwer.pickblockpro;

import io.github.sjouwer.pickblockpro.config.FileHandler;
import io.github.sjouwer.pickblockpro.config.PickBlockOverrides;
import io.github.sjouwer.pickblockpro.picker.ToolPicker;
import io.github.sjouwer.pickblockpro.picker.WeaponPicker;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.ClientModInitializer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PickBlockPro implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Pick Block Pro");
    public static final String NAMESPACE = "pickblockpro";
    private static ConfigHolder<ModConfig> configHolder;

    public static ModConfig getConfig() {
        return configHolder.getConfig();
    }

    @Override
    public void onInitializeClient() {
        configHolder = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

        FileHandler.addFilesToConfigFolder();
        PickBlockOverrides.parseOverrides();
        KeyBindings.registerKeyBindings();
        Commands.registerCommands();

        ToolPicker.addConfiguredToolsToOpUtilities();
        WeaponPicker.addConfiguredWeaponsToOpUtilities();
    }
}