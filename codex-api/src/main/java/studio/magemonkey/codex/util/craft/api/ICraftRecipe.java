package studio.magemonkey.codex.util.craft.api;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class ICraftRecipe extends IAbstractRecipe {
    private final boolean     shaped;
    @NotNull
    private final String[]    shape;
    private final ItemStack[] ingredients;

    public ICraftRecipe(@NotNull JavaPlugin plugin,
                        @NotNull String id,
                        @NotNull ItemStack result,
                        boolean shaped) {
        super(plugin, id, result);
        this.shaped = shaped;
        this.shape = new String[]{"ABC", "DEF", "GHI"};
        this.ingredients = new ItemStack[(int) Math.pow(this.shape.length, 2)];
        for (int i = 0; i < this.ingredients.length; i++) {
            this.ingredients[i] = new ItemStack(Material.AIR);
        }
    }

    @Override
    public void addIngredient(int pos, @Nullable ItemStack item) {
        if (pos >= Math.pow(shape.length, 2)) {
            throw new IllegalArgumentException("Ingredient slot is out of shape size!");
        }

        if (item == null) item = new ItemStack(Material.AIR);
        this.ingredients[pos] = item;
    }

    @Override
    @NotNull
    public Recipe getRecipe() {
        ItemStack     result      = this.getResult();
        NamespacedKey key         = this.getKey();
        ItemStack[]   ingredients = this.getIngredients();

        if (this.isShaped()) {
            char[] shapeChars = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};

            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape(this.getShape());

            for (int pos = 0; pos < ingredients.length; pos++) {
                char      letter = shapeChars[pos];
                ItemStack ing    = ingredients[pos];
                if (ing.hasItemMeta()) {
                    recipe.setIngredient(letter, new RecipeChoice.ExactChoice(ing));
                } else {
                    recipe.setIngredient(letter, ing.getType());
                }
            }
            return recipe;
        }

        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        for (ItemStack ing : ingredients) {
            if (ing.hasItemMeta()) {
                recipe.addIngredient(new RecipeChoice.ExactChoice(ing));
            } else {
                recipe.addIngredient(ing.getType());
            }
        }
        return recipe;
    }
}
