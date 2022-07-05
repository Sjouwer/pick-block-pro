package io.github.sjouwer.pickblockpro;

import io.github.sjouwer.pickblockpro.config.FileHandler;
import io.github.sjouwer.pickblockpro.config.PickBlockOverrides;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.ClientModInitializer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PickBlockPro implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Pick Block Pro");
    private static ConfigHolder<ModConfig> configHolder;

    public static ModConfig getConfig() {
        return configHolder.getConfig();
    }

    @Override
    public void onInitializeClient() {
        configHolder = AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        FileHandler fileHandler = new FileHandler();
        fileHandler.addFilesToConfigFolder();
        PickBlockOverrides.parseOverrideLists(fileHandler.getOverridesFile());

        KeyBindings keyBindings = new KeyBindings();
        keyBindings.setKeyBindings();
    }
}