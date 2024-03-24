package com.promcteam.codex.manager;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.manager.api.Loadable;
import org.jetbrains.annotations.NotNull;

public abstract class IManager<P extends CodexPlugin<P>> extends IListener<P> implements Loadable {

    public IManager(@NotNull P plugin) {
        super(plugin);
    }
}
