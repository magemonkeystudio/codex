package mc.promcteam.engine.items;

import lombok.RequiredArgsConstructor;
import mc.promcteam.engine.NexEngine;
import mc.promcteam.engine.items.exception.MissingItemException;
import mc.promcteam.engine.items.exception.MissingProviderException;
import mc.promcteam.engine.items.providers.IProItemProvider;
import mc.promcteam.engine.items.providers.ItemsAdderProvider;
import mc.promcteam.engine.items.providers.OraxenProvider;
import mc.promcteam.engine.items.providers.VanillaProvider;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class ProItemManager {
    private final NexEngine plugin;
    private       Logger    log;

    private Map<String, IProItemProvider> providers = new HashMap<>();

    public void init() {
        log = plugin.getLogger();
        registerProvider("VANILLA", new VanillaProvider(plugin));
        registerProvider("ORAXEN", new OraxenProvider());
        registerProvider("ITEMSADDER", new ItemsAdderProvider());
    }

    public void registerProvider(String namespace, IProItemProvider provider) {
        namespace = namespace.toUpperCase(Locale.US);
        if (providers.get(namespace) != null) {
            throw new IllegalArgumentException("Provider with namespace " + namespace + " already exists!");
        }

        providers.put(namespace, provider);
        log.info("[ItemManager] Successfully registered provider for " + namespace + " items");
    }

    /**
     * Get an item from the provider. The key is in the format "namespace_id", e.g. "ORAXEN_MY_ITEM".
     *
     * @param key The key of the item.
     * @return The item as retrieved from the provider
     * @throws MissingProviderException If the provider for the given namespace is not enabled/missing.
     * @throws MissingItemException     If the item with the given id does not exist.
     */
    public ItemStack getItem(String key) throws MissingProviderException, MissingItemException {
        // Namespace is the first of the key, e.g. "ORAXEN_MY_ITEM" -> "ORAXEN"
        // It's possible that the key includes no namespace, which means we have
        // a vanilla item.
        // We do need to split off the namespace, so we can isolate the item id.
        String  namespace    = key.split("_")[0];
        boolean hasNamespace = providers.containsKey(namespace);
        String  id           = hasNamespace ? key.split("_")[1] : key;

        return getItem(hasNamespace ? namespace : "VANILLA", id);
    }

    /**
     * Get an item from the provider.
     *
     * @param namespace The namespace of the provider.
     * @param id        The id of the item.
     * @return The item as retrieved from the provider.
     * @throws MissingProviderException If the provider for the given namespace is not enabled/missing.
     * @throws MissingItemException     If the item with the given id does not exist.
     */
    public ItemStack getItem(String namespace, String id) throws MissingProviderException, MissingItemException {
        IProItemProvider provider = providers.get(namespace);
        if (provider == null) { // Make sure we do, indeed, have a provider for the given namespace.
            throw new MissingProviderException("No provider found for namespace " + namespace + "!");
        }

        // Assert that the providing plugin is enabled
        // For 3rd party plugins, this is a good way to check if the plugin is installed.
        // For our own custom plugins, if they're not enabled, we shouldn't have a provider anyway.
        provider.assertEnabled();

        ItemStack item = provider.getItem(id);
        if (item == null) {
            throw new MissingItemException("No item found for key " + id + "!");
        }

        return item;
    }

    public boolean isCustomItem(ItemStack item) {
        return providers.values().stream().anyMatch(provider -> provider.isCustomItem(item));
    }

    public boolean isCustomItemOfId(ItemStack item, String id) {
        return providers.values().stream().anyMatch(provider -> provider.isCustomItemOfId(item, id));
    }

    public Collection<IProItemProvider> getProviders() {
        return providers.values();
    }
}
