package com.promcteam.codex.items.providers;

import com.promcteam.codex.items.CodexItemManager;
import com.promcteam.codex.items.ItemType;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemsAdderProvider implements ICodexItemProvider<ItemsAdderProvider.ItemsAdderItemType> {
    public static final String NAMESPACE = "ITEMSADDER";

    @Override
    public String pluginName() {
        return "ItemsAdder";
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
    public ItemsAdderItemType getItem(String id) {
        if (id == null || id.isBlank()) return null;

        id = CodexItemManager.stripPrefix(NAMESPACE, id);

        CustomStack customStack = CustomStack.getInstance(id);
        if (customStack == null) return null;
        return new ItemsAdderItemType(customStack);
    }

    @Override
    @Nullable
    public ItemsAdderItemType getItem(ItemStack itemStack) {
        CustomStack customStack = CustomStack.byItemStack(itemStack);
        if (customStack == null) return null;
        return new ItemsAdderItemType(customStack);
    }

    @Override
    public boolean isCustomItem(ItemStack item) {
        return CustomStack.byItemStack(item) != null;
    }

    @Override
    public boolean isCustomItemOfId(ItemStack item, String id) {
        id = CodexItemManager.stripPrefix(NAMESPACE, id);

        if (!CustomStack.isInRegistry(id)) return false;
        String itemId = CustomStack.byItemStack(item).getNamespacedID();
        return itemId != null && itemId.equals(id);
    }

    public static class ItemsAdderItemType extends ItemType {
        private final CustomStack customStack;

        public ItemsAdderItemType(CustomStack customStack) {
            this.customStack = customStack;
        }

        @Override
        public String getNamespace() {
            return NAMESPACE;
        }

        @Override
        public String getID() {
            return this.customStack.getNamespacedID();
        }

        @Override
        public Category getCategory() {
            return Category.EXTERNAL;
        }

        @Override
        public ItemStack create() {
            return this.customStack.getItemStack();
        }

        @Override
        public boolean isInstance(@Nullable ItemStack itemStack) {
            if (itemStack == null) return false;
            CustomStack customStack = CustomStack.byItemStack(itemStack);
            return customStack != null && customStack.getNamespacedID().equals(this.customStack.getNamespacedID());
        }
    }
}
