package com.promcteam.codex.util.reflection;

import com.google.common.collect.Multimap;
import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.core.Version;
import com.promcteam.codex.util.Reflex;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class Reflection_1_20 extends Reflection_1_17 {

    @Override
    public Object getConnection(Player player) {
        try {
            Class craftPlayerClass = getCraftClass("entity.CraftPlayer");

            Method getHandle = Reflex.getMethod(craftPlayerClass, "getHandle");
            Object nmsPlayer = Reflex.invokeMethod(getHandle, getCraftPlayer(player));

            String fieldName = "c";
            Object con       = Reflex.getFieldValue(nmsPlayer, fieldName); //WHY must you obfuscate
            if (!con.getClass().getSimpleName().equals("PlayerConnection") && !con.getClass()
                    .getSimpleName()
                    .equals("GeneratedInterceptor")) {
                CodexEngine.get()
                        .getLogger()
                        .warning("Expected PlayerConnection, got " + con.getClass().getSimpleName() + " instead!");
                throw new ClassNotFoundException("Could not get connection from CraftPlayer using field " + fieldName +
                        "\nNMS Player: " + nmsPlayer + "\n");
            }
            return con;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Multimap<Object, Object> getAttributes(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);
            Method getItem = Reflex.getMethod(nmsItem.getClass(),
                    ReflectionManager.MINOR_VERSION > 17 ? "d" : "getItem");
            Object item = Reflex.invokeMethod(getItem, nmsItem);


            Class<Enum> enumItemSlotClass = (Class<Enum>) (
                    getClazz("net.minecraft.world.entity.EnumItemSlot")
            );
            Class<?> itemArmorClass   = getClazz("net.minecraft.world.item.ItemArmor");
            Class<?> itemToolClass    = getClazz("net.minecraft.world.item.ItemTool");
            Class<?> itemSwordClass   = getClazz("net.minecraft.world.item.ItemSword");
            Class<?> itemTridentClass = getClazz("net.minecraft.world.item.ItemTrident");

            Enum mainhand = (Enum) Reflex.invokeMethod(
                    Reflex.getMethod(enumItemSlotClass,
                            ReflectionManager.MINOR_VERSION <= 17 ? "fromName" : "a",
                            String.class),
                    null, "mainhand");

            if (itemArmorClass.isInstance(item)) {
                Object tool               = itemArmorClass.cast(item);
                Method getEquipmentSlot   = Reflex.getMethod(itemArmorClass, "b");
                Object armorEquipmentSlot = Reflex.invokeMethod(getEquipmentSlot, tool);
                if (!Version.CURRENT.isLower(Version.V1_19_R3)) {
                    // If it's 1.19.4, the 'b' method returns a different enum, so we have to get the slot out of that enum
                    armorEquipmentSlot = Reflex.invokeMethod(Reflex.getMethod(armorEquipmentSlot.getClass(), "a"),
                            armorEquipmentSlot);
                }
                Method getDefaultAttributeModifiers = Reflex.getMethod(itemArmorClass, "a", enumItemSlotClass);

                return (Multimap<Object, Object>) Reflex.invokeMethod(getDefaultAttributeModifiers,
                        tool,
                        armorEquipmentSlot);
            }

            Object tool;
            Method getDefaultAttributeModifiers;
            if (itemToolClass.isInstance(item)) {
                tool = itemToolClass.cast(item);
                getDefaultAttributeModifiers = Reflex.getMethod(itemToolClass, "a", enumItemSlotClass);
            } else if (itemSwordClass.isInstance(item)) {
                tool = itemSwordClass.cast(item);
                getDefaultAttributeModifiers = Reflex.getMethod(itemSwordClass, "a", enumItemSlotClass);
            } else if (itemTridentClass.isInstance(item)) {
                tool = itemTridentClass.cast(item);
                getDefaultAttributeModifiers = Reflex.getMethod(itemTridentClass, "a", enumItemSlotClass);
            } else {
                return null;
            }

            return (Multimap<Object, Object>) Reflex.invokeMethod(getDefaultAttributeModifiers, tool, mainhand);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
