package studio.magemonkey.codex.util.reflection;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.core.Version;
import studio.magemonkey.codex.util.Reflex;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reflection_1_20 extends Reflection_1_17 {

    @Override
    public Object getConnection(Player player) {
        try {
            Class craftPlayerClass = getCraftClass("entity.CraftPlayer");

            Method getHandle = Reflex.getMethod(craftPlayerClass, "getHandle");
            Object nmsPlayer = Reflex.invokeMethod(getHandle, getCraftPlayer(player));

            String fieldName = "c";
            Object con       = Reflex.getFieldValue(nmsPlayer, fieldName); //WHY must you obfuscate
            if (!con.getClass().getSimpleName().equals("PlayerConnection")
                    && !con.getClass().getSimpleName().equals("ServerGamePacketListenerImpl")
                    && !con.getClass().getSimpleName().equals("GeneratedInterceptor")) {
                CodexEngine.get()
                        .getLogger()
                        .warning("Expected PlayerConnection, got " + con.getClass().getSimpleName() + " instead!");
                throw new ClassNotFoundException(
                        "Could not get connection from CraftPlayer using field " + fieldName + "\nNMS Player: "
                                + nmsPlayer + "\n");
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
            Object nmsItem       = getNMSCopy(itemStack);
            String getItemMethod = "d";
            if (ReflectionManager.MINOR_VERSION < 17) {
                getItemMethod = "getItem";
            } else if (ReflectionManager.MINOR_VERSION >= 20 && Version.CURRENT.isAtLeast(Version.V1_20_R4)) {
                getItemMethod = "g";
            }

            Method getItem =
                    Reflex.getMethod(nmsItem.getClass(), getItemMethod);
            Object item = Reflex.invokeMethod(getItem, nmsItem);


            Class<Enum> enumItemSlotClass = (Class<Enum>) (getClazz("net.minecraft.world.entity.EnumItemSlot"));
            Class<?>    itemArmorClass    = getClazz("net.minecraft.world.item.ItemArmor");
            Class<?>    itemToolClass     = getClazz("net.minecraft.world.item.ItemTool");
            Class<?>    itemSwordClass    = getClazz("net.minecraft.world.item.ItemSword");
            Class<?>    itemTridentClass  = getClazz("net.minecraft.world.item.ItemTrident");

            Enum slot = (Enum) Reflex.invokeMethod(Reflex.getMethod(enumItemSlotClass,
                    ReflectionManager.MINOR_VERSION <= 17 ? "fromName" : "a",
                    String.class), null, "mainhand");

            Object tool;
            Method getDefaultAttributeModifiers;
            String modifierMethodName = Version.CURRENT.isAtLeast(Version.V1_20_R4) ? "j" : "a";
            if (itemArmorClass.isInstance(item)) {
                tool = itemArmorClass.cast(item);
                Method getEquipmentSlot =
                        Reflex.getMethod(itemArmorClass, Version.CURRENT.isAtLeast(Version.V1_20_R4) ? "m" : "b");
                Object armorEquipmentSlot = Reflex.invokeMethod(getEquipmentSlot, tool);
                if (!Version.CURRENT.isLower(Version.V1_19_R3)) {
                    // If it's 1.19.4, the 'b' method returns a different enum, so we have to get the slot out of that enum
                    armorEquipmentSlot = Reflex.invokeMethod(Reflex.getMethod(armorEquipmentSlot.getClass(), "a"),
                            armorEquipmentSlot);
                }

                slot = (Enum<?>) armorEquipmentSlot;
                getDefaultAttributeModifiers = Reflex.getMethod(itemArmorClass, modifierMethodName, enumItemSlotClass);
            } else if (itemToolClass.isInstance(item)) {
                tool = itemToolClass.cast(item);
                getDefaultAttributeModifiers = Reflex.getMethod(itemToolClass, modifierMethodName, enumItemSlotClass);
            } else if (itemSwordClass.isInstance(item)) {
                tool = itemSwordClass.cast(item);
                getDefaultAttributeModifiers = Reflex.getMethod(itemSwordClass, modifierMethodName, enumItemSlotClass);
            } else if (itemTridentClass.isInstance(item)) {
                tool = itemTridentClass.cast(item);
                getDefaultAttributeModifiers =
                        Reflex.getMethod(itemTridentClass, modifierMethodName, enumItemSlotClass);
            } else {
                return null;
            }

            if (Version.CURRENT.isAtLeast(Version.V1_20_R4)) {
                // Gets a DataComponentMap
                Method   components             = Reflex.getMethod(tool.getClass(), "p");
                Object   dataComponent          = Reflex.invokeMethod(components, tool);
                Class<?> dataComponentsClass    = Reflex.getClass("net.minecraft.core.component.DataComponents");
                Class<?> dataComponentTypeClass = Reflex.getClass("net.minecraft.core.component.DataComponentType");
                Field    attrField              = dataComponentsClass.getDeclaredField("n");
                Object   attributeComponentType = attrField.get(null);
                Object attributeModifiers = Reflex.invokeMethod
                        (Reflex.getMethod(dataComponent.getClass(),
                                        "a", dataComponentTypeClass),
                                dataComponent, attributeComponentType);
                Method       getList        = Reflex.getMethod(attributeModifiers.getClass(), "b");
                List<Object> attrCollection = (List<Object>) Reflex.invokeMethod(getList, attributeModifiers);

                Class<?> modifierHolderClass =
                        Reflex.getClass("net.minecraft.world.item.component.ItemAttributeModifiers$b");
                Method   getBaseHolder           = Reflex.getMethod(modifierHolderClass, "a");
                Method   getEquipmentSlotGroup   = Reflex.getMethod(modifierHolderClass, "c");
                Method   getAttributeModifier    = Reflex.getMethod(modifierHolderClass, "b");
                Class<?> equipmentSlotGroupClass = Reflex.getClass("net.minecraft.world.entity.EquipmentSlotGroup");
                Method   isSlotApplicable        = Reflex.getMethod(equipmentSlotGroupClass, "b", enumItemSlotClass);

                Map<Object, Object> attMap = new HashMap<>();
                for (Object mod : attrCollection) {
                    Object  slotGroup    = Reflex.invokeMethod(getEquipmentSlotGroup, mod);
                    boolean isApplicable = (boolean) Reflex.invokeMethod(isSlotApplicable, slotGroup, slot);
                    if (isApplicable) {
                        Object baseHolder        = Reflex.invokeMethod(getBaseHolder, mod);
                        Object attributeModifier = Reflex.invokeMethod(getAttributeModifier, mod);
                        Object attributeBase =
                                Reflex.invokeMethod(Reflex.getMethod(baseHolder.getClass(), "a"), baseHolder);
                        attMap.put(attributeBase, attributeModifier);
                    }
                }
                Multimap<Object, Object> map = Multimaps.forMap(attMap);

                return map;
            } else {
                return (Multimap<Object, Object>) Reflex.invokeMethod(getDefaultAttributeModifiers, tool, slot);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Object save(Object nmsItem, Object nbtCompound) {
        try {
            if (Version.CURRENT.isAtLeast(Version.V1_20_R4)) {
                Class<?> mcServerClass = getClazz("net.minecraft.server.MinecraftServer");
                Object   serverInst    = Reflex.invokeMethod(Reflex.getMethod(mcServerClass, "getServer"), null);
                Class<?> providerClass = getClazz("net.minecraft.core.HolderLookup$a");
                Class<?> nbtBaseClass  = getClazz("net.minecraft.nbt.NBTBase");
                Method registryAccess =
                        Reflex.getMethod(serverInst.getClass(), getRegistryAccessMethodName());
                Method save = Reflex.getMethod(nmsItem.getClass(), "b", providerClass, nbtBaseClass);

                return Reflex.invokeMethod(save,
                        nmsItem,
                        Reflex.invokeMethod(registryAccess, serverInst),
                        nbtCompound);
            } else {
                Method save = Reflex.getMethod(nmsItem.getClass(), "b", nbtCompound.getClass());
                return Reflex.invokeMethod(save, nmsItem, nbtCompound);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String toBase64(@NotNull ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream      dataOutput   = new DataOutputStream(outputStream);

            Object nbtTagListItems    = newNBTTagList();
            Object nbtTagCompoundItem = newNBTTagCompound();

            if (item != null && !item.getType().isAir()) {
                Object nmsItem = getNMSCopy(item);

                Object newCompound = save(nmsItem, nbtTagCompoundItem);
                if (newCompound != null) nbtTagCompoundItem = newCompound;
            }

            Method add = Reflex.getMethod(AbstractList.class, "add", Object.class);
            Reflex.invokeMethod(add, nbtTagListItems, nbtTagCompoundItem);

            Class<?> compressedClass = getClazz("net.minecraft.nbt.NBTCompressedStreamTools");
            Method a =
                    Reflex.getMethod(compressedClass, "a", nbtTagCompoundItem.getClass(), DataOutput.class);

            Reflex.invokeMethod(a, null, nbtTagCompoundItem, dataOutput);

            String str = new BigInteger(1, outputStream.toByteArray()).toString(32);

            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ItemStack fromBase64(@NotNull String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());

            Object nbtTagCompoundRoot;
            try {
                Class<?> compressedClass = getClazz("net.minecraft.nbt.NBTCompressedStreamTools");
                Method   a               = Reflex.getMethod(compressedClass, "a", DataInput.class);

                nbtTagCompoundRoot = Reflex.invokeMethod(a, null, new DataInputStream(inputStream));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }


            Class<?> nmsItemClass  = getClazz("net.minecraft.world.item.ItemStack");
            Class<?> compoundClass = getClazz("net.minecraft.nbt.NBTTagCompound");

            Object nmsItem;
            if (Version.CURRENT.isAtLeast(Version.V1_20_R4)) {
                // item.parseOptional(MinecraftServer.getServer().registryAccess(), (CompoundTag) nbtTagCompoundRoot);

                // Provider class is located at net.minecraft.core.HolderLookup in a subclass called "a"
                Class<?> mcServerClass = getClazz("net.minecraft.server.MinecraftServer");
                Object   serverInst    = Reflex.invokeMethod(Reflex.getMethod(mcServerClass, "getServer"), null);
                Class<?> providerClass = getClazz("net.minecraft.core.HolderLookup$a");
                Method   parseOptional = Reflex.getMethod(nmsItemClass, "a", providerClass, compoundClass);
                Method registryAccess =
                        Reflex.getMethod(serverInst.getClass(), getRegistryAccessMethodName());
                nmsItem = Reflex.invokeMethod(parseOptional,
                        null,
                        Reflex.invokeMethod(registryAccess, serverInst),
                        nbtTagCompoundRoot);
            } else {
                Method parse = Reflex.getMethod(nmsItemClass, "a", compoundClass);
                nmsItem = Reflex.invokeMethod(parse, null, nbtTagCompoundRoot);
            }

            Method asBukkitCopy =
                    Reflex.getMethod(getCraftClass("inventory.CraftItemStack"), "asBukkitCopy", nmsItemClass);

            ItemStack item = (ItemStack) Reflex.invokeMethod(asBukkitCopy, null, nmsItem);

            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
