package studio.magemonkey.codex.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EntityUT {

    public static double getAttribute(@NotNull LivingEntity entity, @NotNull Attribute attribute) {
        AttributeInstance ai = entity.getAttribute(attribute);
        return ai == null ? 0D : ai.getValue();
    }

    public static double getAttributeBase(@NotNull LivingEntity entity, @NotNull Attribute attribute) {
        AttributeInstance ai = entity.getAttribute(attribute);
        return ai == null ? 0D : ai.getBaseValue();
    }

    public static ItemStack[] getEquipment(@NotNull LivingEntity entity) {
        ItemStack[] items = new ItemStack[6];

        EntityEquipment equip = entity.getEquipment();
        if (equip == null) return items;

        int aCount = 0;
        for (ItemStack armor : equip.getArmorContents()) {
            items[aCount++] = armor;
        }

        items[4] = equip.getItemInMainHand();
        items[5] = equip.getItemInOffHand();

        return items;
    }
}
