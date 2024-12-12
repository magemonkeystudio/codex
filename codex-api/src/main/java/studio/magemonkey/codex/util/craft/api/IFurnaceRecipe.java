package studio.magemonkey.codex.util.craft.api;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class IFurnaceRecipe extends IAbstractRecipe {
    @Nullable
    private       ItemStack input;
    private final float     exp;
    private final int       time;

    public IFurnaceRecipe(@NotNull JavaPlugin plugin,
                          @NotNull String id,
                          @NotNull ItemStack result,
                          float exp,
                          double time) {
        super(plugin, id, result);
        this.exp = exp;
        this.time = (int) Math.max(1, 20D * time);
    }


    public void addIngredient(@NotNull ItemStack ing) {
        this.addIngredient(0, ing);
    }

    @Override
    public void addIngredient(int slot, @Nullable ItemStack ing) {
        if (ing == null || ing.getType() == Material.AIR) {
            throw new IllegalArgumentException("Input can not be null or AIR!");
        }

        this.input = ing;
    }

    @Override
    @NotNull
    public Recipe getRecipe() {
        NamespacedKey key    = this.getKey();
        ItemStack     input  = this.getInput();
        ItemStack     result = this.getResult();
        float         exp    = this.getExp();
        int           time   = this.getTime();

        if (input == null) {
            throw new RuntimeException("Recipe input is null. Recipe is invalid");
        }

        if (input.hasItemMeta()) {
            return new FurnaceRecipe(key, result, new RecipeChoice.ExactChoice(input), exp, time);
        } else {
            return new FurnaceRecipe(key, result, input.getType(), exp, time);
        }
    }
}
