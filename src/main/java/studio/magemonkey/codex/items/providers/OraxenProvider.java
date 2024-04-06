package studio.magemonkey.codex.items.providers;

import studio.magemonkey.codex.items.CodexItemManager;
import studio.magemonkey.codex.items.ItemType;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class OraxenProvider implements ICodexItemProvider<OraxenProvider.OraxenItemType> {
    public static final String NAMESPACE = "ORAXEN";

    @Override
    public String pluginName() {
        return "Oraxen";
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public Category getCategory() {
        return Category.EXTERNAL;
    }

    @Override
    @Nullable
    public OraxenItemType getItem(String id) {
        if (id == null || id.isBlank()) return null;

        id = CodexItemManager.stripPrefix(NAMESPACE, id);

        ItemBuilder itemBuilder = OraxenItems.getItemById(id);
        if (itemBuilder == null) return null;
        return new OraxenItemType(id, itemBuilder);
    }

    @Override
    @Nullable
    public OraxenItemType getItem(ItemStack itemStack) {
        return getItem(OraxenItems.getIdByItem(itemStack));
    }

    @Override
    public boolean isCustomItem(ItemStack item) {
        return OraxenItems.getIdByItem(item) != null;
    }

    @Override
    public boolean isCustomItemOfId(ItemStack item, String id) {
        id = CodexItemManager.stripPrefix(NAMESPACE, id);

        if (!OraxenItems.exists(id)) return false;
        String itemId = OraxenItems.getIdByItem(item);
        return itemId != null && itemId.equals(id);
    }

    public static class OraxenItemType extends ItemType {
        private final String      id;
        private final ItemBuilder itemBuilder;

        public OraxenItemType(String id, ItemBuilder itemBuilder) {
            this.id = id;
            this.itemBuilder = itemBuilder;
        }

        @Override
        public String getNamespace() {
            return NAMESPACE;
        }

        @Override
        public String getID() {
            return this.id;
        }

        @Override
        public Category getCategory() {
            return Category.EXTERNAL;
        }

        @Override
        public ItemStack create() {
            return this.itemBuilder.build();
        }

        @Override
        public boolean isInstance(@Nullable ItemStack itemStack) {
            String itemId = OraxenItems.getIdByItem(itemStack);
            return itemId != null && itemId.equals(this.id);
        }
    }
}
