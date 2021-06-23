package mc.promcteam.engine.hooks;

import mc.promcteam.engine.NexPlugin;
import mc.promcteam.engine.manager.IListener;
import org.jetbrains.annotations.NotNull;

public abstract class NHook<P extends NexPlugin<P>> extends IListener<P> {

	protected HookState state;
	String pluginName;
	
	public NHook(@NotNull P plugin) {
		super(plugin);
	}
	
	public final void hook() {
		this.state = this.setup();
	}
	
	public final void unhook() {
		if (this.getState() == HookState.SUCCESS) {
			this.shutdown();
		}
	}
	
	@NotNull
	public final String getPlugin() {
		return this.pluginName;
	}
	
	@NotNull
	protected abstract HookState setup();
	
	protected abstract void shutdown();
	
	@NotNull
	public final HookState getState() {
		return this.state;
	}
}
