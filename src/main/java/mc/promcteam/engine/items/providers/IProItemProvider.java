package mc.promcteam.engine.items.providers;

import mc.promcteam.engine.items.exception.MissingProviderException;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public interface IProItemProvider {
    default void assertEnabled() throws MissingProviderException {
        if (!Bukkit.getPluginManager().isPluginEnabled(pluginName())) {
            throw new MissingProviderException(pluginName() + " is not enabled!");
        }
    }
    String pluginName();
    /**
     * Get an item from the provider.
     *
     * @param id The id of the item.
     * @return The item with the given id.
     */
    ItemStack getItem(String id);

    /**
     * Check if the given item is from this provider.
     *
     * @param item The item to check.
     * @return Whether the item is from this provider.
     */
    boolean isCustomItem(ItemStack item);

    /**
     * Check if the given item is from this provider and has the given id.
     * @param item The item to check.
     * @param id The id to check.
     * @return Whether the item is from this provider and has the given id.
     */
    boolean isCustomItemOfId(ItemStack item, String id);
}
