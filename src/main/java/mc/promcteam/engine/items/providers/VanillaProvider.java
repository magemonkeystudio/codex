package mc.promcteam.engine.items.providers;

import lombok.RequiredArgsConstructor;
import mc.promcteam.engine.NexEngine;
import mc.promcteam.engine.items.ItemType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@RequiredArgsConstructor
public class VanillaProvider implements IProItemProvider<VanillaProvider.VanillaItemType> {
    public static final String NAMESPACE = "VANILLA";

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
    public Category getCategory() {
        return Category.VANILLA;
    }

    @Override
    @Nullable
    public VanillaItemType getItem(String id) {
        Material material = Material.matchMaterial(id.replaceAll("[ -]", "_"));
        if (material == null) return null;

        return new VanillaItemType(material);
    }

    @Override
    @Nullable
    public VanillaItemType getItem(ItemStack itemStack) {
        if (itemStack == null) return new VanillaItemType(Material.AIR);
        return new VanillaItemType(itemStack.getType());
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

    public static class VanillaItemType extends ItemType {
        private final Material material;

        public VanillaItemType(Material material) {
            this.material = material;
        }

        @Override
        public String getNamespace() {
            return NAMESPACE;
        }

        @Override
        public String getID() {
            return this.material.name();
        }

        @Override
        public Category getCategory() {
            return Category.VANILLA;
        }

        @Override
        public ItemStack create() {
            return new ItemStack(this.material);
        }

        @Override
        public boolean isInstance(@Nullable ItemStack itemStack) {
            if (itemStack == null) return this.material == Material.AIR;
            return itemStack.getType() == this.material;
        }
    }
}
