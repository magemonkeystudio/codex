package com.promcteam.codex.config.legacy;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LegacyConfigManager {

    /**
     * Loads a given file as a {@link FileConfiguration} object.
     * If the file doesn't exist, one will be created and the default values will be added.
     *
     * @param file        The File to load.
     * @param defaultFile An InputStream representing the default values for the file. Can be null
     * @return The FileConfiguration parsed from the file.
     */
    public static FileConfiguration loadConfigFile(File file, @Nullable InputStream defaultFile) {
        return loadConfigFile(file, defaultFile, true);
    }

    /**
     * Loads a given file as a {@link FileConfiguration} object.
     * If the file doesn't exist, one will be created and the default values will be added. <br/>
     * This method is special because you can specify whether missing fields will be inserted.
     *
     * @param file        The File to load.
     * @param defaultFile An InputStream representing the default values for the file. Can be null
     * @param copyMissing Whether missing default values from the defaultFile should be copied to the file.
     * @return The FileConfiguration parsed from the file.
     */
    public static FileConfiguration loadConfigFile(File file, InputStream defaultFile, boolean copyMissing) {
        YamlConfiguration defConfig = new YamlConfiguration();
        if (defaultFile != null) {
            try {
                defConfig.load(new InputStreamReader(defaultFile));
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }


        file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
                defConfig.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        YamlConfiguration conf = new YamlConfiguration();
        try {
            conf.load(file);

            if (copyMissing) {
                conf.addDefaults(defConfig.getConfigurationSection("").getValues(true));
                conf.options().copyDefaults(true);
                conf.save(file);
            }
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return conf;
    }

}
