package com.promcteam.codex.api.menu;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MenuItem {

    @Getter
    @Setter
    private ItemStack  item;
    @Getter
    @Setter
    private MenuAction action;

    public MenuItem(ItemStack item, MenuAction action) {
        this.item = item;
        this.action = action;
    }

    public void click(Player player) {
        action.click(player);
    }

}
