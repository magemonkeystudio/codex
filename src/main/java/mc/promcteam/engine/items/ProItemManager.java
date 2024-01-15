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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class ProItemManager {
    private final NexEngine plugin;
    private       Logger    log;

    private Map<String, IProItemProvider> providers = new LinkedHashMap<>();

    public void init() {
        log = plugin.getLogger();
        registerProvider(VanillaProvider.NAMESPACE, new VanillaProvider(plugin));
        registerProvider(OraxenProvider.NAMESPACE, new OraxenProvider());
        registerProvider(ItemsAdderProvider.NAMESPACE, new ItemsAdderProvider());
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
        String[] split = key.split("_");
        if (split.length > 1) {
            String namespace = split[0];
            if (providers.containsKey(namespace)) return getItemType(namespace, String.join("", Arrays.copyOfRange(split, 1, split.length)));
        }
        return getItemType("VANILLA", key);
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
            ItemType itemType = provider.getItem(itemStack);
            if (itemType != null) set.add(itemType);
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
        return providers.values().stream().anyMatch(provider -> provider.isCustomItem(item));
    }

    public boolean isCustomItemOfId(ItemStack item, String id) {
        return providers.values().stream().anyMatch(provider -> provider.isCustomItemOfId(item, id));
    }

    public Collection<IProItemProvider> getProviders() {
        return providers.values();
    }
}
