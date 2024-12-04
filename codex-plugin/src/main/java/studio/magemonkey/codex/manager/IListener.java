package studio.magemonkey.codex.manager;

import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexPlugin;

public abstract class IListener<P extends CodexPlugin<P>> implements AbstractListener {

    @NotNull
    public final P plugin;

    public IListener(@NotNull P plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerListeners() {
        this.plugin.getPluginManager().registerEvents(this, this.plugin);
    }
}
