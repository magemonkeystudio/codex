package studio.magemonkey.codex.nms.v1_20_2;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import studio.magemonkey.codex.api.ArmorUtil;
import studio.magemonkey.codex.util.random.Rnd;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class ArmorUtilImpl implements ArmorUtil {
    @Override
    public Object getTrimMaterial(NamespacedKey key) {
        return Registry.TRIM_MATERIAL.get(key);
    }

    @Override
    public Object getTrimPattern(NamespacedKey key) {
        return Registry.TRIM_PATTERN.get(key);
    }

    @Override
    public void addTrim(ItemMeta meta, String material, String pattern) {
        if (!(meta instanceof ArmorMeta)) return;
        ArmorMeta armorMeta = (ArmorMeta) meta;

        TrimMaterial trimMaterial = null;
        if (material.equals("*")) {
            int size = 0;
            for (TrimMaterial ignored : Registry.TRIM_MATERIAL) {
                size++;
            }
            int index = Rnd.get(size);
            int i     = 0;
            for (TrimMaterial next : Registry.TRIM_MATERIAL) {
                if (index == i) {
                    trimMaterial = next;
                    break;
                }
                i++;
            }
        } else {
            trimMaterial = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(material));
        }

        TrimPattern trimPattern = null;
        if (pattern.equals("*")) {
            int size = 0;
            for (TrimPattern ignored : Registry.TRIM_PATTERN) size++;

            int index = Rnd.get(size);
            int i     = 0;
            for (TrimPattern next : Registry.TRIM_PATTERN) {
                if (index == i++) {
                    trimPattern = next;
                    break;
                }
            }
        } else {
            trimPattern = Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(pattern));
        }

        ArmorTrim armorTrim =
                new ArmorTrim(Objects.requireNonNull(trimMaterial), Objects.requireNonNull(trimPattern));
        armorMeta.setTrim(armorTrim);
    }
}
