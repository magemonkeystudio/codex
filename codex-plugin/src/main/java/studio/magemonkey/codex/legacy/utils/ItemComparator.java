package studio.magemonkey.codex.legacy.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemComparator {

    private static boolean isSimilarSpigot(ItemStack stack1, ItemStack stack2) {
        if (stack1 == null) {
            return false;
        } else if (stack1 == stack2) {
            return true;
        } else {
            return stack2.getType() == stack1.getType() && stack2.getDurability() == stack1.getDurability()
                    && stack2.hasItemMeta() == stack1.hasItemMeta() && (!stack1.hasItemMeta() || Bukkit.getItemFactory()
                    .equals(stack2.getItemMeta(), stack1.getItemMeta()));
        }
    }

    public static boolean isSimilar(ItemStack stack1, ItemStack stack2) {

        return isSimilarSpigot(stack1, stack2) ||
                stack2.getType() == stack1.getType() && stack2.getDurability() == stack1.getDurability() &&
                        stack2.hasItemMeta() == stack1.hasItemMeta() &&
                        (!stack1.hasItemMeta() || checkLore(stack1.getItemMeta().getLore(),
                                stack2.getItemMeta().getLore()));

    }

    private static boolean checkLore(List<String> l1, List<String> l2) {
        boolean equal = l1.equals(l2);

        boolean fuzzyEqual = true;

        if (!equal) {
            int i1 = 0, i2 = 0;
            for (String s1 : l1) {
                boolean found = false;
                while (i2 < l2.size() && !found) {
                    if (s1.equals(l2.get(i2))) {
                        found = true;
                    }

                    i2++;
                }

                if (!found)
                    fuzzyEqual = false;

                i2 = 0;
            }
        }

        return equal || fuzzyEqual;
    }

}
