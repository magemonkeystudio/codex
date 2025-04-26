package studio.magemonkey.codex.util.constants;

import org.bukkit.inventory.ItemStack;

public class ItemHelper {
    /**
     * A utility method to support versions that use null or air ItemStacks.
     */
    public static boolean isAirOrNull(ItemStack item) {
        return item == null || item.getType().isAir();
    }
}
