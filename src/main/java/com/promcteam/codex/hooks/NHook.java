package com.promcteam.codex.hooks;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.manager.IListener;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class NHook<P extends CodexPlugin<P>> extends IListener<P> {

    protected HookState state;
    String pluginName;

    public NHook(@NotNull P plugin) {
        super(plugin);
    }

    public final void hook() {
        this.state = this.setup();
    }

    public final void unhook() {
        if (this.getState() == HookState.SUCCESS) {
            this.shutdown();
        }
    }

    @NotNull
    public final String getPlugin() {
        return this.pluginName;
    }

    @NotNull
    protected abstract HookState setup();

    protected abstract void shutdown();

    @NotNull
    public final HookState getState() {
        return this.state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NHook<?> nHook = (NHook<?>) o;
        return Objects.equals(pluginName, nHook.pluginName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginName);
    }
}
