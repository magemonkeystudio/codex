package mc.promcteam.engine.manager;

import mc.promcteam.engine.NexPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class IListener<P extends NexPlugin<P>> implements AbstractListener {
	
    @NotNull public final P plugin;
    
    public IListener(@NotNull P plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void registerListeners() {
        this.plugin.getPluginManager().registerEvents(this, this.plugin);
    }
}
