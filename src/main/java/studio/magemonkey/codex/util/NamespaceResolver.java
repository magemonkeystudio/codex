package studio.magemonkey.codex.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

public class NamespaceResolver {

    public static PotionEffectType getPotion(String... possibleNames) {
        for (String possibleName : possibleNames) {
            PotionEffectType potion = PotionEffectType.getByName(possibleName);
            if (potion != null) {
                return potion;
            }
        }

        throw new IllegalArgumentException("Couldn't find potion effect for " + String.join(", ", possibleNames));
    }

    public static Enchantment getEnchantment(String... possibleNames) {
        for (String possibleName : possibleNames) {
            Enchantment enchantment = Enchantment.getByName(possibleName);
            if (enchantment != null) {
                return enchantment;
            }
        }

        throw new IllegalArgumentException("Couldn't find enchantment for " + String.join(", ", possibleNames));
    }

}
