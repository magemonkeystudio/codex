package com.promcteam.codex.items;

import lombok.RequiredArgsConstructor;
import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.items.exception.MissingItemException;
import com.promcteam.codex.items.exception.MissingProviderException;
import com.promcteam.codex.items.providers.IProItemProvider;
import com.promcteam.codex.items.providers.ItemsAdderProvider;
import com.promcteam.codex.items.providers.OraxenProvider;
import com.promcteam.codex.items.providers.VanillaProvider;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class ProItemManager {
    private final CodexEngine plugin;
    private       Logger      log;

    private final Map<String, IProItemProvider<?>> providers = new LinkedHashMap<>();

    /** Removes the given prefix from the provided id, if applicable.
     *
     * @param prefix the provider prefix to remove
     * @param id the item id, prefixed or not
     * @return the stripped id
     */
    public static String stripPrefix(String prefix, String id) {
        String[] split = id.split("_", 2);
        return split.length == 2 && split[0].equalsIgnoreCase(prefix) ? split[1] : id;
    }

    /** Removes any prefixes corresponding to the currently registered Item Providers, if there is any.
     *
     * @param id the item id, prefixed or not
     * @return the stripped id
     */
    public static String stripPrefix(String id) {
        String[] split = id.split("_", 2);
        return split.length == 2 && CodexEngine.getEngine().getItemManager().providers.keySet().stream().anyMatch(s -> s.equalsIgnoreCase(split[0])) ?
                split[1] : id;
    }

    public void init() {
        log = plugin.getLogger();
        registerProvider(VanillaProvider.NAMESPACE, new VanillaProvider());
        registerProvider(OraxenProvider.NAMESPACE, new OraxenProvider());
        registerProvider(ItemsAdderProvider.NAMESPACE, new ItemsAdderProvider());
    }

    public void registerProvider(String namespace, IProItemProvider<?> provider) {
        namespace = namespace.toUpperCase(Locale.US);
        if (providers.get(namespace) != null) {
            throw new IllegalArgumentException("Provider with namespace " + namespace + " already exists!");
        }

        providers.put(namespace, provider);
        log.info("[ItemManager] Successfully registered provider for " + namespace + " items");
    }

    public void unregisterProvider(Class<? extends IProItemProvider<?>> providerClass) {
        this.providers.entrySet().removeIf(entry -> entry.getValue().getClass().equals(providerClass));
    }

    /**
     * Get an item type from the provider. The key is in the format "namespace_id", e.g. "ORAXEN_MY_ITEM".
     *
     * @param key The key of the item.
     * @return The item type as retrieved from the provider
     * @throws MissingProviderException If the provider for the given namespace is not enabled/missing.
     * @throws MissingItemException     If the item with the given id does not exist.
     */
    public ItemType getItemType(String key) throws MissingProviderException, MissingItemException {
        // Namespace is the first of the key, e.g. "ORAXEN_MY_ITEM" -> "ORAXEN"
        // It's possible that the key includes no namespace, which means we have
        // a vanilla item.
        // We do need to split off the namespace, so we can isolate the item id.
        String[] split = key.split("_", 2);
        if (split.length > 1) {
            String namespace = split[0];
            if (providers.containsKey(namespace)) return getItemType(namespace, split[1]);
        }
        return getItemType(VanillaProvider.NAMESPACE, key);
    }

    /**
     * Get an item type from the provider.
     *
     * @param namespace The namespace of the provider.
     * @param id        The id of the item.
     * @return The item type as retrieved from the provider.
     * @throws MissingProviderException If the provider for the given namespace is not enabled/missing.
     * @throws MissingItemException     If the item with the given id does not exist.
     */
    public ItemType getItemType(String namespace, String id) throws MissingProviderException, MissingItemException {
        IProItemProvider<?> provider = providers.get(namespace);
        if (provider == null) { // Make sure we do, indeed, have a provider for the given namespace.
            throw new MissingProviderException("No provider found for namespace " + namespace + "!");
        }

        // Assert that the providing plugin is enabled
        // For 3rd party plugins, this is a good way to check if the plugin is installed.
        // For our own custom plugins, if they're not enabled, we shouldn't have a provider anyway.
        provider.assertEnabled();

        ItemType item = provider.getItem(id);
        if (item == null) {
            throw new MissingItemException("No item found for key " + id + "!");
        }

        return item;
    }

    /**
     * Get the ItemTypes associated to the provided ItemStack.
     *
     * @param itemStack The item to get the ItemTypes from
     * @return a Set containing the ItemTypes the provided item corresponds to. May be empty.
     */
    @NotNull
    public Set<ItemType> getItemTypes(@Nullable ItemStack itemStack)  {
        Set<ItemType> set = new HashSet<>();
        for (IProItemProvider<?> provider : this.providers.values()) {
            try {
                ItemType itemType = provider.getItem(itemStack);
                if (itemType != null) set.add(itemType);
            } catch (NoClassDefFoundError | NoSuchMethodError ignored) {}
        }
        return set;
    }

    /**
     * Get the main ItemType associated to the provided ItemStack.
     * Importance is ranked according to the {@link Enum#ordinal()} value of the provider's
     * {@link IProItemProvider#getCategory()}, with higher values meaning more importance.
     *
     * @param itemStack The item to get the ItemType from
     * @return the ItemType of more importance corresponding to the item, or null if none was found.
     */
    @Nullable
    public ItemType getMainItemType(@Nullable ItemStack itemStack)  {
        return this.getItemTypes(itemStack).stream().max(Comparator.comparing(ItemType::getCategory)).orElse(null);
    }

    public boolean isCustomItem(ItemStack item) {
        return providers.values().stream().anyMatch(provider -> {
            try {
                return provider.isCustomItem(item);
            } catch (NoClassDefFoundError | NoSuchMethodError ignored) {}
            return false;
        });
    }

    public boolean isCustomItemOfId(ItemStack item, String id) {
        return providers.values().stream().anyMatch(provider -> {
            try {
                if (!provider.getClass().equals(VanillaProvider.class) &&
                        (id.length() < provider.getNamespace().length()+1 ||
                        !id.substring(0, provider.getNamespace().length()+1).equalsIgnoreCase(provider.getNamespace()))) return false;
                return provider.isCustomItemOfId(item, id);
            } catch (NoClassDefFoundError | NoSuchMethodError ignored) {}
            return false;
        });
    }

    public Collection<IProItemProvider<?>> getProviders() {
        return providers.values();
    }
}
