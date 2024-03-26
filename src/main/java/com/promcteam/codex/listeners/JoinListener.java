package com.promcteam.codex.listeners;

import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.api.DelayedCommand;
import com.promcteam.codex.config.api.JYML;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JoinListener implements Listener {
    private final CodexEngine          plugin;
    private final List<DelayedCommand> onFirstJoin;
    private final List<DelayedCommand> onJoin;

    @SuppressWarnings("unchecked")
    public JoinListener(CodexEngine plugin, JYML config) {
        this.plugin = plugin;
        List<Map<String, Object>> firstJoinData = (List<Map<String, Object>>) config.get("onFirstJoin");
        if (firstJoinData == null) firstJoinData = new ArrayList<>();
        this.onFirstJoin = DelayedCommand.deserializeMapList(firstJoinData);

        List<Map<String, Object>> onJoinData = (List<Map<String, Object>>) config.get("onJoin");
        if (onJoinData == null) onJoinData = new ArrayList<>();
        this.onJoin = DelayedCommand.deserializeMapList(onJoinData);
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        DelayedCommand.invoke(plugin, event.getPlayer(), onJoin);

        if (!event.getPlayer().hasPlayedBefore()) {
            DelayedCommand.invoke(plugin, event.getPlayer(), onFirstJoin);
        }
    }

}
