package com.promcteam.codex.manager;

import com.promcteam.codex.CodexPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class IListener<P extends CodexPlugin<P>> implements AbstractListener {

    @NotNull
    public final P plugin;

    public IListener(@NotNull P plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerListeners() {
        this.plugin.getPluginManager().registerEvents(this, this.plugin);
    }
}
