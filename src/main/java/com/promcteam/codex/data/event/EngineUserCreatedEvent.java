package com.promcteam.codex.data.event;

import com.promcteam.codex.CodexDataPlugin;
import com.promcteam.codex.data.users.IAbstractUser;
import org.jetbrains.annotations.NotNull;

public class EngineUserCreatedEvent<P extends CodexDataPlugin<P, U>, U extends IAbstractUser<P>> extends EngineUserEvent<P, U> {

    public EngineUserCreatedEvent(@NotNull P plugin, @NotNull U user) {
        super(plugin, user);
    }
}
