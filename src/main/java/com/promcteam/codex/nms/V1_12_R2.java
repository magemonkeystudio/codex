package com.promcteam.codex.nms;

import com.google.common.collect.Multimap;
import com.promcteam.codex.util.Reflex;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Collection;

public class V1_12_R2 implements NMS {

    @Override
    public ItemStack fromBase64(@NotNull String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());

            Object nbtTagCompoundRoot;
            try {
                Class<?> compressedClass = reflectionUtil.getNMSClass("NBTCompressedStreamTools");
                Method   a               = Reflex.getMethod(compressedClass, "a", DataInput.class);

                nbtTagCompoundRoot = Reflex.invokeMethod(a, null, new DataInputStream(inputStream));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            Class<?> nmsItemClass = reflectionUtil.getNMSClass("ItemStack");
            Object   nmsItem      = reflectionUtil.getNMSCopy(new ItemStack(Material.DIRT));

            Method a = Reflex.getMethod(nmsItemClass, "load", reflectionUtil.getNMSClass("NBTTagCompound"));
            Reflex.invokeMethod(a, nmsItem, nbtTagCompoundRoot);

            Method asBukkitCopy = Reflex.getMethod(reflectionUtil.getCraftClass("inventory.CraftItemStack"),
                    "asBukkitCopy",
                    nmsItemClass);
            ItemStack item = (ItemStack) Reflex.invokeMethod(asBukkitCopy, null, nmsItem);

            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    @NotNull
    public String getNbtString(@NotNull ItemStack item) {
        try {
            Object nmsCopy        = reflectionUtil.getNMSCopy(item);
            Method getOrCreateTag = Reflex.getMethod(nmsCopy.getClass(), "getTag");
            Object tag            = Reflex.invokeMethod(getOrCreateTag, nmsCopy);
            if (tag == null)
                tag = Reflex.invokeConstructor(Reflex.getConstructor(reflectionUtil.getNMSClass("NBTTagCompound")));
            Method asString = Reflex.getMethod(tag.getClass(), "toString");
            return (String) Reflex.invokeMethod(asString, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    @NotNull
    public String fixColors(@NotNull String str) {
        //TODO Test this.
        StringBuilder sb = new StringBuilder();

        try {
            Class<?> baseComponentClass = reflectionUtil.getNMSClass("IChatBaseComponent");
            Class<?> chatMessageClass   = reflectionUtil.getCraftClass("util.CraftChatMessage");

            Method fromComponent    = Reflex.getMethod(chatMessageClass, "fromComponent", baseComponentClass);
            Method fromStringOrNull = Reflex.getMethod(chatMessageClass, "fromString", String.class, Boolean.class);

            Object[] baseComponent = (Object[]) Reflex.invokeMethod(fromStringOrNull, null, str, true);
            for (Object comp : baseComponent) {
                String singleColor = (String) Reflex.invokeMethod(fromComponent, null, baseComponentClass.cast(comp));
                sb.append(singleColor);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return str;
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, reflectionUtil.getGenericAttribute("g"));
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, reflectionUtil.getGenericAttribute("h"));
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, reflectionUtil.getGenericAttribute("i"));
    }

    @Override
    public boolean isWeapon(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = reflectionUtil.getNMSCopy(itemStack);

            Method getItem = Reflex.getMethod(nmsItem.getClass(), "getItem");

            Object item = Reflex.invokeMethod(getItem, nmsItem);

            Class<?> swordClass = reflectionUtil.getNMSClass("ItemSword");
            Class<?> axeClass   = reflectionUtil.getNMSClass("ItemAxe");

            return swordClass.isInstance(item) || axeClass.isInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private Multimap<String, Object> getAttributes(@NotNull ItemStack itemStack) {
        try {
            Multimap<String, Object> attMap  = null;
            Object                   nmsItem = reflectionUtil.getNMSCopy(itemStack);
            Method                   getItem = Reflex.getMethod(nmsItem.getClass(), "getItem");
            Object                   item    = Reflex.invokeMethod(getItem, nmsItem);


            Class<Enum> enumItemSlotClass = (Class<Enum>) reflectionUtil.getNMSClass("EnumItemSlot");
//            Class<?> attributeModClass = reflectionUtil.getNMSClass("AttributeModifier");
            Class<?> itemArmorClass   = reflectionUtil.getNMSClass("ItemArmor");
            Class<?> itemToolClass    = reflectionUtil.getNMSClass("ItemTool");
            Class<?> itemSwordClass   = reflectionUtil.getNMSClass("ItemSword");
            Class<?> itemTridentClass = reflectionUtil.getNMSClass("ItemTrident");
            Enum mainhand = (Enum) Reflex.invokeMethod(
                    Reflex.getMethod(enumItemSlotClass, "a", String.class),
                    null, "mainhand");


            if (itemArmorClass.isInstance(item)) {
                Object tool = itemArmorClass.cast(item);
                Object bObj = Reflex.getFieldValue(itemArmorClass, "c");
                Method a    = Reflex.getMethod(itemArmorClass, "a", enumItemSlotClass);

                attMap = (Multimap<String, Object>) Reflex.invokeMethod(a, tool, bObj);
            } else if (itemToolClass.isInstance(item)) {
                Object tool = itemToolClass.cast(item);
                Method a    = Reflex.getMethod(itemToolClass, "a", enumItemSlotClass);
                attMap = (Multimap<String, Object>) Reflex.invokeMethod(a, tool, mainhand);
            } else if (itemSwordClass.isInstance(item)) {
                Object tool = itemSwordClass.cast(item);
                Method a    = Reflex.getMethod(itemSwordClass, "a", enumItemSlotClass);
                attMap = (Multimap<String, Object>) Reflex.invokeMethod(a, tool, mainhand);
            }

            return attMap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private double getAttributeValue(@NotNull ItemStack item, @NotNull Object attackDamage) {
        try {
            Class<?>                 attributeModifierClass = reflectionUtil.getNMSClass("AttributeModifier");
            Class<?>                 attributeBaseClass     = reflectionUtil.getNMSClass("AttributeBase");
            Multimap<String, Object> attMap                 = getAttributes(item);
            if (attMap == null) return 0D;

            Method getName = Reflex.getMethod(attributeBaseClass, "getName");


            Collection<Object> att =
                    attMap.get((String) Reflex.invokeMethod(getName, attributeBaseClass.cast(attackDamage)));
            Object mod = attributeModifierClass.cast((att == null || att.isEmpty())
                    ? 0
                    : att.stream().findFirst().get());

            Method getAmount = Reflex.getMethod(attributeModifierClass, "d");
            double damage    = (double) Reflex.invokeMethod(getAmount, mod);

            return damage;// + 1;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
