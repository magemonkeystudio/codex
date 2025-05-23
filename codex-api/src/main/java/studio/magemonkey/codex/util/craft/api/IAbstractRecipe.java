package studio.magemonkey.codex.util.craft.api;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class IAbstractRecipe {

    protected final JavaPlugin plugin;
    protected final String     id;
    protected       ItemStack  result;

    protected final NamespacedKey key;

    public IAbstractRecipe(@NotNull JavaPlugin plugin, @NotNull String id, @NotNull ItemStack result) {
        this.plugin = plugin;
        this.id = id.toLowerCase().replace(" ", "_");
        this.result = result;

        String type = "";
        if (this instanceof ICraftRecipe) {
            type = "craft";
        } else if (this instanceof IFurnaceRecipe) {
            type = "furnace";
        }
        String key = type + "-" + this.getId();

        this.key = new NamespacedKey(plugin, key);
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public NamespacedKey getKey() {
        return this.key;
    }

    @NotNull
    public ItemStack getResult() {
        return this.result;
    }

    public void setResult(@NotNull ItemStack result) {
        this.result = result;
    }

    public abstract void addIngredient(int slot, @Nullable ItemStack item);

    @NotNull
    public abstract Recipe getRecipe();
}
