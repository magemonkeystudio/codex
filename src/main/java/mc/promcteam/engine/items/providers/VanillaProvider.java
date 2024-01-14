package mc.promcteam.engine.items.providers;

import lombok.RequiredArgsConstructor;
import mc.promcteam.engine.NexEngine;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

@RequiredArgsConstructor
public class VanillaProvider implements IProItemProvider {
    private final NexEngine plugin;

    @Override
    public void assertEnabled() {
        // Vanilla is always enabled.
    }

    @Override
    public String pluginName() {
        return null;
    }

    @Override
    public ItemStack getItem(String id) {
        Material material = Material.matchMaterial(id.replaceAll("[ -]", "_"));
        if (material == null) return null;

        return new ItemStack(material);
    }

    @Override
    public boolean isCustomItem(ItemStack item) {
        // We'll assume that if the item does not belong to any other provider,
        // it's a vanilla item.
        Collection<IProItemProvider> providers = plugin.getItemManager().getProviders();
        return providers.stream().noneMatch(provider -> provider.isCustomItem(item));
    }

    @Override
    public boolean isCustomItemOfId(ItemStack item, String id) {
        final String updatedId = id.replaceAll("[ -]", "_");
        // We'll assume that if the item does not belong to any other provider,
        // it's a vanilla item.
        Collection<IProItemProvider> providers = plugin.getItemManager().getProviders();
        return providers.stream()
                .noneMatch(provider -> provider.isCustomItemOfId(item, updatedId)) && updatedId.equalsIgnoreCase(item.getType().toString());
    }
}
