package studio.magemonkey.codex.api.items.providers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.api.items.ItemType;
import studio.magemonkey.codex.api.items.PrefixHelper;

import java.util.Locale;

public class VanillaProvider implements ICodexItemProvider<VanillaProvider.VanillaItemType> {
    public static final String NAMESPACE = "VANILLA";

    @Override
    public void assertEnabled() {
        // Vanilla is always enabled.
    }

    @Override
    public String pluginName() {
        return null;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public Category getCategory() {
        return Category.VANILLA;
    }

    @Override
    @Nullable
    public VanillaItemType getItem(String id) {
        if (id == null || id.isBlank()) return null;

        id = id.toUpperCase(Locale.US);
        Material material = Material.matchMaterial(PrefixHelper.stripPrefix(NAMESPACE, id).replaceAll("[ -]", "_"));
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
        return item != null;
    }

    @Override
    public boolean isCustomItemOfId(ItemStack item, String id) {
        return item.getType()
                .name()
                .equalsIgnoreCase(PrefixHelper.stripPrefix(NAMESPACE, id).replaceAll("[ -]", "_"));
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
            return this.material.name().toLowerCase();
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
