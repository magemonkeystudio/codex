package studio.magemonkey.codex.manager;

import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.manager.api.Loadable;
import org.jetbrains.annotations.NotNull;

public abstract class IManager<P extends CodexPlugin<P>> extends IListener<P> implements Loadable {

    public IManager(@NotNull P plugin) {
        super(plugin);
    }
}
