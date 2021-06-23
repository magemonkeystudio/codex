package mc.promcteam.engine.modules;

import mc.promcteam.engine.NexPlugin;
import mc.promcteam.engine.core.config.CoreConfig;
import org.jetbrains.annotations.NotNull;

public abstract class IExternalModule<P extends NexPlugin<P>> extends IModule<P> {

	public IExternalModule(@NotNull P plugin) {
		super(plugin);
	}

	@NotNull
	public abstract LoadPriority getPriority();
	
	@Override
	@NotNull
	public String getPath() {
		return CoreConfig.MODULES_PATH_EXTERNAL + this.getId() + "/";
	}
	
	public static enum LoadPriority {
		HIGH,
		LOW,
		;
	}
}
