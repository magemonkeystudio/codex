package mc.promcteam.engine.items.providers;

import mc.promcteam.engine.items.ItemType;
import mc.promcteam.engine.items.exception.MissingProviderException;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IProItemProvider<T extends ItemType> {
    default void assertEnabled() throws MissingProviderException {
        if (!Bukkit.getPluginManager().isPluginEnabled(pluginName())) {
            throw new MissingProviderException(pluginName() + " is not enabled!");
        }
    }
    String pluginName();

    Category getCategory();

    /**
     * Get an item from the provider.
     *
     * @param id The id of the item.
     * @return The item with the given id.
     */
    @Nullable
    T getItem(String id);

    /**
     * Get the ItemType of this provider corresponding to the provided ItemStack.
     *
     * @param itemStack The item to get the ItemType from.
     * @return the ItemType associated to the provided ItemStack, or null if not found.
     */
    @Nullable
    T getItem(ItemStack itemStack);

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

    enum Category {
        VANILLA,
        MOD,
        EXTERNAL,
        PRO,
        ;
    }
}
