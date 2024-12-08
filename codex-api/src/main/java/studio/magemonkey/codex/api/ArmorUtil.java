package studio.magemonkey.codex.api;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;

public interface ArmorUtil {
    // Armor trims were added in 1.19.4
    default Object getTrimMaterial(NamespacedKey key) {
        return null;
    }

    default Object getTrimPattern(NamespacedKey key) {
        return null;
    }

    default void addTrim(ItemMeta meta, String material, String pattern) {
    }
}
