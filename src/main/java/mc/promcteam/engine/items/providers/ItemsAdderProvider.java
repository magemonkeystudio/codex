package mc.promcteam.engine.items.providers;

import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.inventory.ItemStack;

public class ItemsAdderProvider implements IProItemProvider {
    @Override
    public String pluginName() {
        return "ItemsAdder";
    }

    @Override
    public ItemStack getItem(String id) {
        if (!CustomStack.isInRegistry(id)) return null;

        return CustomStack.getInstance(id).getItemStack();
    }

    @Override
    public boolean isCustomItem(ItemStack item) {
        return CustomStack.byItemStack(item) != null;
    }

    @Override
    public boolean isCustomItemOfId(ItemStack item, String id) {
        if (!CustomStack.isInRegistry(id)) return false;
        String itemId = CustomStack.byItemStack(item).getNamespacedID();
        return itemId != null && itemId.equals(id);
    }
}
