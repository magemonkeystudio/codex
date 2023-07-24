package mc.promcteam.engine.api.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class MenuListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Menu openMenu = Menu.getOpenMenu(event.getPlayer());
        if (openMenu != null) {openMenu.onClose();}
    }
}
