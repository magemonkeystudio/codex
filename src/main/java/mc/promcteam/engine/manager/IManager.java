package mc.promcteam.engine.manager;

import mc.promcteam.engine.NexPlugin;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.manager.api.Loadable;

public abstract class IManager<P extends NexPlugin<P>> extends IListener<P> implements Loadable {

	public IManager(@NotNull P plugin) {
		super(plugin);
	}
}
