package mc.promcteam.engine.manager;

import java.io.File;

import mc.promcteam.engine.NexPlugin;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.config.api.JYML;

public abstract class LoadableItem {

	public final NexPlugin<?> plugin;
	protected String id;
	protected final String path;
	protected final JYML cfg;
	
	public LoadableItem(@NotNull NexPlugin<?> plugin, @NotNull String path) {
		this(plugin, new JYML(new File(path)));
	}

	public LoadableItem(@NotNull NexPlugin<?> plugin, @NotNull JYML cfg) {
		this.plugin = plugin;
		this.cfg = cfg;
		this.id = this.getFile().getName().replace(".yml", "").toLowerCase();
		this.path = this.getFile().getAbsolutePath();
	}
	
	@NotNull
	public final String getId() {
		return this.id;
	}
	
	@NotNull
	public final File getFile() {
		return this.getConfig().getFile();
	}
	
	@NotNull
	public final JYML getConfig() {
		return this.cfg;
	}
	
	public final void save() {
		JYML cfg = this.getConfig();
		this.save(cfg);
		
		cfg.save();
	}
	
	protected abstract void save(@NotNull JYML cfg);
}
