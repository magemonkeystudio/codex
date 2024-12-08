package studio.magemonkey.codex.manager;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class IListener<P extends JavaPlugin> implements AbstractListener {
    @NotNull
    public final P plugin;

    public IListener(@NotNull P plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerListeners() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }
}
