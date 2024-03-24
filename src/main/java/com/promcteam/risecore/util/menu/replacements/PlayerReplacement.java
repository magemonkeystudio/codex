package com.promcteam.risecore.util.menu.replacements;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlayerReplacement extends MenuReplacement {

    @Getter
    @Setter
    private PlayerAlgorithm algo;

    public PlayerReplacement(String find, PlayerAlgorithm algo) {
        super(find);
        this.algo = algo;
    }

    @Override
    public String getValue(Player player) {
        if (algo != null) {
            if (player == null) return "Player is null";
            return algo.execute(player);
        }

        return "";
    }

    @Override
    public String getValue() {
        return ChatColor.RED + "No player supplied";
    }

    public interface PlayerAlgorithm {
        String execute(Player player);
    }
}
