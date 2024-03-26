package com.promcteam.codex.api.menu.replacements;

import com.promcteam.codex.legacy.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public abstract class MenuReplacement {
    protected String find;

    public abstract String getValue(Player player);

    public abstract String getValue();

    public ItemStack replace(ItemStack input) {
        ItemMeta im = input.getItemMeta();
        if (im == null) return input;

        if (im.hasDisplayName()) {
            //Replace in the display name
            String name = im.getDisplayName();
            im.setDisplayName(name.replace(find, getValue()));
        }

        if (im.hasLore()) {
            //Replace in the lore.
            List<String> lore = im.getLore();
            if (lore != null) lore.replaceAll(s -> s.replace(find, getValue()));
        }

        input.setItemMeta(im);
        return input;
    }

    public ItemStack replace(ItemStack input, Player player) {
        ItemMeta im = input.getItemMeta();
        if (im == null) return input;

        if (im.hasDisplayName()) {
            // Replace in the display name
            String name = im.getDisplayName();
            im.setDisplayName(name.replace(find, getValue(player)));
        }

        if (im.hasLore()) {
            // Replace in the lore.
            List<String> lore = im.getLore();
            if (lore != null) lore.replaceAll(s -> s.replace(find, getValue(player)));
        }

        input.setItemMeta(im);
        return input;
    }

    public static class DataBank {
        public static List<MenuReplacement> defaultReplacements = new ArrayList<>();

        static {
            defaultReplacements.add(new PlayerReplacement("$<player.name>", Player::getName));
            defaultReplacements.add(new PlayerReplacement("$<player.location>",
                    (player) -> Utils.locToString(player.getLocation())));
        }
    }

}
