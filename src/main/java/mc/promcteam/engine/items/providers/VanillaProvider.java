package mc.promcteam.engine.items.providers;

import mc.promcteam.engine.items.ItemType;
import mc.promcteam.engine.items.ProItemManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class VanillaProvider implements IProItemProvider<VanillaProvider.VanillaItemType> {
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
    public Category getCategory() {
        return Category.VANILLA;
    }

    @Override
    @Nullable
    public VanillaItemType getItem(String id) {
        if (id == null || id.isBlank()) return null;

        Material material = Material.matchMaterial(ProItemManager.stripPrefix(NAMESPACE, id).replaceAll("[ -]", "_"));
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
                .equalsIgnoreCase(ProItemManager.stripPrefix(NAMESPACE, id).replaceAll("[ -]", "_"));
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
