package mc.promcteam.engine.manager;

import mc.promcteam.engine.NexPlugin;
import mc.promcteam.engine.manager.api.Loadable;
import org.jetbrains.annotations.NotNull;

public abstract class IManager<P extends NexPlugin<P>> extends IListener<P> implements Loadable {

    public IManager(@NotNull P plugin) {
        super(plugin);
    }
}
