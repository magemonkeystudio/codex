package studio.magemonkey.codex.api.items.providers;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.api.items.ItemType;
import studio.magemonkey.codex.api.items.PrefixHelper;

public class NexoProvider implements ICodexItemProvider<NexoProvider.NexoItemType> {
    public static final String NAMESPACE = "NEXO";

    @Override
    public String pluginName() {
        return "Nexo";
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
    public NexoItemType getItem(String id) {
        if (id == null || id.isBlank()) return null;

        id = PrefixHelper.stripPrefix(NAMESPACE, id);

        ItemBuilder itemBuilder = NexoItems.itemFromId(id);
        if (itemBuilder == null) return null;
        return new NexoItemType(id, itemBuilder);
    }

    @Override
    @Nullable
    public NexoItemType getItem(ItemStack itemStack) {
        return getItem(NexoItems.idFromItem(itemStack));
    }

    @Override
    public boolean isCustomItem(ItemStack item) {
        return NexoItems.idFromItem(item) != null;
    }

    @Override
    public boolean isCustomItemOfId(ItemStack item, String id) {
        id = PrefixHelper.stripPrefix(NAMESPACE, id);

        if (!NexoItems.exists(id)) return false;
        String itemId = NexoItems.idFromItem(item);
        return itemId != null && itemId.equals(id);
    }

    public static class NexoItemType extends ItemType {
        private final String      id;
        private final ItemBuilder itemBuilder;

        public NexoItemType(String id, ItemBuilder itemBuilder) {
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
            String itemId = NexoItems.idFromItem(itemStack);
            return itemId != null && itemId.equals(this.id);
        }
    }
}
