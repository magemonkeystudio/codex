package com.promcteam.risecore.listeners;

import com.promcteam.risecore.Core;
import com.promcteam.risecore.legacy.cmds.DelayedCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void join(PlayerJoinEvent event) {
        DelayedCommand.invoke(Core.getInstance(), event.getPlayer(), Core.getOnJoin());

        if (!event.getPlayer().hasPlayedBefore()) {
            DelayedCommand.invoke(Core.getInstance(), event.getPlayer(), Core.getFirstJoin());
        }
    }

}
