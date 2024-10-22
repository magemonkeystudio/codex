package studio.magemonkey.codex.util.reflection;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.core.Version;
import studio.magemonkey.codex.util.Reflex;

import java.io.*;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.AbstractList;

public class Reflection_1_20 extends Reflection_1_18 {
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
            if(a == null) {
                a = Reflex.getMethod(compressedClass, "a", nbtTagCompoundItem.getClass(), OutputStream.class);
            }

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

            if (nmsItem == null) {
                return new ItemStack(Material.AIR);
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
