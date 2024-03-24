package com.promcteam.codex.data.event;

import com.promcteam.codex.CodexDataPlugin;
import com.promcteam.codex.data.users.IAbstractUser;
import com.promcteam.codex.manager.api.event.IEvent;
import org.jetbrains.annotations.NotNull;

public abstract class EngineUserEvent<P extends CodexDataPlugin<P, U>, U extends IAbstractUser<P>> extends IEvent {

    private final @NotNull P plugin;
    private final @NotNull U user;

    public EngineUserEvent(@NotNull P plugin, @NotNull U user) {
        this.plugin = plugin;
        this.user = user;
    }

    @NotNull
    public P getPlugin() {
        return this.plugin;
    }

    @NotNull
    public U getUser() {
        return this.user;
    }
}
