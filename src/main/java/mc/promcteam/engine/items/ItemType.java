package mc.promcteam.engine.items;

import mc.promcteam.engine.items.providers.IProItemProvider;
import mc.promcteam.engine.items.providers.VanillaProvider;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public abstract class ItemType {

    public abstract String getNamespace();

    public abstract String getID();

    public final String getNamespacedID() {
        if (this instanceof VanillaProvider.VanillaItemType) return getID();
        return getNamespace()+'_'+ getID();
    }

    public abstract IProItemProvider.Category getCategory();

    public abstract ItemStack create();

    public abstract boolean isInstance(ItemStack itemStack);

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof ItemType)) return false;
        ItemType other = ((ItemType) obj);
        return this.getNamespace().equals(other.getNamespace()) && this.getID().equals(other.getID());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{getNamespace(), getID()});
    }
}
