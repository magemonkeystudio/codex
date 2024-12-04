package studio.magemonkey.codex.data.event;

import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexDataPlugin;
import studio.magemonkey.codex.data.users.IAbstractUser;

public class EngineUserLoadEvent<P extends CodexDataPlugin<P, U>, U extends IAbstractUser<P>> extends EngineUserEvent<P, U> {

    public EngineUserLoadEvent(@NotNull P plugin, @NotNull U user) {
        super(plugin, user);
    }
}
