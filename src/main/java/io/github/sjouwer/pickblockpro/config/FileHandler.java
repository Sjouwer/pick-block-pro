package io.github.sjouwer.pickblockpro.config;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FileHandler {
    private final File listsFolder;
    private File overrides;

    public FileHandler() {
        listsFolder = new File(FabricLoader.getInstance().getConfigDir().toFile(), "pickblockpro");
        listsFolder.mkdirs();
    }

    public void addFilesToConfigFolder() {
        overrides = new File(listsFolder, "PickBlockOverrides.json");
        if (overrides.exists()) {
            return;
        }

        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("assets/pickblockpro/PickBlockOverrides.json")) {
            if (is != null) {
                Files.copy(is, overrides.toPath());
            }
        }
        catch (IOException e) {
            PickBlockPro.LOGGER.error("Failed to place the PickBlockOverrides file in the config folder");
            e.printStackTrace();
        }
    }

    public File getOverridesFile() {
        return overrides;
    }
}
