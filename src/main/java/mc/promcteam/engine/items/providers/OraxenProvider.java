package mc.promcteam.engine.items.providers;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.inventory.ItemStack;

public class OraxenProvider implements IProItemProvider {
    @Override
    public String pluginName() {
        return "Oraxen";
    }

    @Override
    public ItemStack getItem(String id) {
        if (!OraxenItems.exists(id)) return null;

        return OraxenItems.getItemById(id).build();
    }

    @Override
    public boolean isCustomItem(ItemStack item) {
        return OraxenItems.getIdByItem(item) != null;
    }

    @Override
    public boolean isCustomItemOfId(ItemStack item, String id) {
        if (!OraxenItems.exists(id)) return false;
        String itemId = OraxenItems.getIdByItem(item);
        return itemId != null && itemId.equals(id);
    }
}
