package com.promcteam.risecore.util.menu.replacements;

import com.promcteam.risecore.util.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class MenuReplacement {

    @Getter
    @Setter
    protected String find;

    public MenuReplacement(String find) {
        this.find = find;
    }

    public abstract String getValue(Player player);

    public abstract String getValue();

    public ItemStack replace(ItemStack input) {
        ItemMeta im = input.getItemMeta();

        if (im.hasDisplayName()) {
            //Replace in the display name
            String name = im.getDisplayName();
            im.setDisplayName(name.replace(find, getValue()));
        }

        if (im.hasLore()) {
            //Replace in the lore.
            List<String> lore = im.getLore();
            for (int i = 0; i < lore.size(); i++) {
                String line = lore.get(i);
                lore.set(i, line.replace(find, getValue()));
            }
        }

        input.setItemMeta(im);
        return input;
    }

    public ItemStack replace(ItemStack input, Player player) {
        ItemMeta im = input.getItemMeta();

        if (im.hasDisplayName()) {
            //Replace in the display name
            String name = im.getDisplayName();
            im.setDisplayName(name.replace(find, getValue(player)));
        }

        if (im.hasLore()) {
            //Replace in the lore.
            List<String> lore = im.getLore();
            for (int i = 0; i < lore.size(); i++) {
                String line = lore.get(i);
                lore.set(i, line.replace(find, getValue(player)));
            }
        }

        input.setItemMeta(im);
        return input;
    }

    public static class DataBank {
        public static List<MenuReplacement> defaultReplacements = new ArrayList<>();

        static {
            defaultReplacements.add(new PlayerReplacement("$<player.name>", (player) -> player.getName()));
            defaultReplacements.add(new PlayerReplacement("$<player.location>",
                    (player) -> Utils.locToString(player.getLocation())));
        }
    }

}
