package studio.magemonkey.codex.data.event;

import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexDataPlugin;
import studio.magemonkey.codex.data.users.IAbstractUser;
import studio.magemonkey.codex.api.events.IEvent;

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
