package com.promcteam.codex.manager;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.config.api.JYML;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class LoadableItem {

    public final CodexPlugin<?> plugin;
    protected    String         id;
    protected final String       path;
    protected final JYML         cfg;

    public LoadableItem(@NotNull CodexPlugin<?> plugin, @NotNull String path) throws InvalidConfigurationException {
        this(plugin, new JYML(new File(path)));
    }

    public LoadableItem(@NotNull CodexPlugin<?> plugin, @NotNull JYML cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.id = this.getFile().getName().replace(".yml", "").toLowerCase();
        this.path = this.getFile().getAbsolutePath();
    }

    @NotNull
    public final String getId() {
        return this.id;
    }

    @NotNull
    public final File getFile() {
        return this.getConfig().getFile();
    }

    @NotNull
    public final JYML getConfig() {
        return this.cfg;
    }

    public final void save() {
        JYML cfg = this.getConfig();
        this.save(cfg);

        cfg.save();
    }

    protected abstract void save(@NotNull JYML cfg);
}
