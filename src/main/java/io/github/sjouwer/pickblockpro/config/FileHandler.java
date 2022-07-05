package io.github.sjouwer.pickblockpro.config;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FileHandler {
    private static File configFolder;
    private static File overrides;

    private FileHandler() {
    }

    private static void loadOrCreateConfigFolder() {
        configFolder = new File(FabricLoader.getInstance().getConfigDir().toFile(), "pickblockpro");
        configFolder.mkdirs();
    }

    public static void addFilesToConfigFolder() {
        loadOrCreateConfigFolder();
        overrides = new File(configFolder, "PickBlockOverrides.json");
        if (overrides.exists()) {
            return;
        }

        try (InputStream is = PickBlockPro.class.getClassLoader().getResourceAsStream("assets/pickblockpro/PickBlockOverrides.json")) {
            if (is != null) {
                Files.copy(is, overrides.toPath());
            }
        }
        catch (IOException e) {
            PickBlockPro.LOGGER.error("Failed to place the PickBlockOverrides file in the config folder");
            e.printStackTrace();
        }
    }

    public static File getOverridesFile() {
        return overrides;
    }
}
