package studio.magemonkey.codex.util;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;

public class NamespaceResolver {

    public static PotionEffectType getPotion(String... possibleNames) {
        for (String possibleName : possibleNames) {
            PotionEffectType potion;

            try {
                potion = Registry.EFFECT.get(NamespacedKey.minecraft(possibleName.toLowerCase(Locale.US)));
                if (potion != null) return potion;
            } catch (Throwable ignored) {
            }

            try {
                potion = PotionEffectType.getByKey(NamespacedKey.minecraft(possibleName.toLowerCase(Locale.US)));
                if (potion != null) return potion;
            } catch (NoSuchMethodError ignored) {
            }

            potion = PotionEffectType.getByName(possibleName.toLowerCase(Locale.US));
            if (potion == null) {
                potion = PotionEffectType.getByName(possibleName.toUpperCase(Locale.US));
            }
            if (potion != null) {
                return potion;
            }
        }

        throw new IllegalArgumentException("Couldn't find potion effect for " + String.join(", ", possibleNames));
    }

    public static Enchantment getEnchantment(String... possibleNames) {
        for (String possibleName : possibleNames) {
            Enchantment enchantment;

            try {
                enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(possibleName.toLowerCase(Locale.US)));
                if (enchantment != null) return enchantment;
            } catch (Throwable ignored) {
            }

            enchantment = Enchantment.getByKey(NamespacedKey.minecraft(possibleName.toLowerCase(Locale.US)));
            if (enchantment != null) {
                return enchantment;
            }
        }

        throw new IllegalArgumentException("Couldn't find enchantment for " + String.join(", ", possibleNames));
    }

}
