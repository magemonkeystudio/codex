package mc.promcteam.engine.data.event;

import mc.promcteam.engine.NexDataPlugin;
import mc.promcteam.engine.data.users.IAbstractUser;
import mc.promcteam.engine.manager.api.event.IEvent;
import org.jetbrains.annotations.NotNull;

public abstract class EngineUserEvent<P extends NexDataPlugin<P, U>, U extends IAbstractUser<P>> extends IEvent {

    private @NotNull P plugin;
    private @NotNull U user;

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
