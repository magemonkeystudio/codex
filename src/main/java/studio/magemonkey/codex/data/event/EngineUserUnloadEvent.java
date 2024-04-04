package studio.magemonkey.codex.data.event;

import studio.magemonkey.codex.CodexDataPlugin;
import studio.magemonkey.codex.data.users.IAbstractUser;
import org.jetbrains.annotations.NotNull;

public class EngineUserUnloadEvent<P extends CodexDataPlugin<P, U>, U extends IAbstractUser<P>> extends EngineUserEvent<P, U> {

    public EngineUserUnloadEvent(@NotNull P plugin, @NotNull U user) {
        super(plugin, user);
    }
}
