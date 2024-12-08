package studio.magemonkey.codex.manager;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public interface AbstractListener extends Listener {

    void registerListeners();

    default void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }
}
