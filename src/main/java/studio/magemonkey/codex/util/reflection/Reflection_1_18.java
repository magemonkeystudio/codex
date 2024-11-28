package studio.magemonkey.codex.util.reflection;

import com.google.common.collect.Multimap;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.util.AttributeUT;
import studio.magemonkey.codex.util.Reflex;

import java.lang.reflect.Method;
import java.util.Collection;

public class Reflection_1_18 extends Reflection_1_17 {
    @Override
    public Object save(Object nmsItem, Object nbtCompound) {
        try {
            Method save = Reflex.getMethod(nmsItem.getClass(), "b", nbtCompound.getClass());

            return Reflex.invokeMethod(save, nmsItem, nbtCompound);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Object getGenericAttribute(String name) {
        try {
            return AttributeUT.resolve(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Multimap getAttributes(@NotNull ItemStack itemStack) {
        EquipmentSlot slot = itemStack.getType().getEquipmentSlot();
        return itemStack.getType().getDefaultAttributeModifiers(slot);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public double getAttributeValue(@NotNull ItemStack item, @NotNull Object attribute) {
        Multimap                      modifiers = getAttributes(item);
        Collection<AttributeModifier> mod       = modifiers.get(attribute);
        if (mod.isEmpty()) return 0;

        return mod.stream().mapToDouble(AttributeModifier::getAmount).sum();
    }

    @Override
    public double getDefaultDamage(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, AttributeUT.resolve("GENERIC_ATTACK_DAMAGE")); // generic.attack_damage
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, AttributeUT.resolve("GENERIC_ATTACK_SPEED")); // generic.attack_speed
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, AttributeUT.resolve("GENERIC_ARMOR")); // generic.armor
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, AttributeUT.resolve("GENERIC_ARMOR_TOUGHNESS")); // generic.armor_toughness
    }
}
