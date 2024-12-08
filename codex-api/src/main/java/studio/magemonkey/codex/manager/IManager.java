package studio.magemonkey.codex.manager;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.manager.api.Loadable;

public abstract class IManager<P extends JavaPlugin> extends IListener<P> implements Loadable {
    public IManager(@NotNull P plugin) {
        super(plugin);
    }
}
