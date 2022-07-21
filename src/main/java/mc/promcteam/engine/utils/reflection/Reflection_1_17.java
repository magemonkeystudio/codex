package mc.promcteam.engine.utils.reflection;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import mc.promcteam.engine.NexEngine;
import mc.promcteam.engine.core.Version;
import mc.promcteam.engine.utils.Reflex;
import mc.promcteam.engine.utils.random.Rnd;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Random;

public class Reflection_1_17 extends ReflectionUtil {

    protected static Object newNBTTagCompound() {
        try {
            Class<?> nbtTagClass = getClazz("net.minecraft.nbt.NBTTagCompound");
            return nbtTagClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static Object newNBTTagList() {
        try {
            Class<?> nbtTagClass = getClazz("net.minecraft.nbt.NBTTagList");
            return nbtTagClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected static ItemStack toBukkitCopy(Object nmsItem) {
        try {
            Class<?> craftItem    = getCraftClass("inventory.CraftItemStack");
            Method   asBukkitCopy = Reflex.getMethod(craftItem, "asBukkitCopy", getClazz("net.minecraft.world.item.ItemStack"));
            if (asBukkitCopy == null) return null;

            return (ItemStack) Reflex.invokeMethod(asBukkitCopy, null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Class<?> getClazz(String classString) throws ClassNotFoundException {
        String   name  = classString;
        Class<?> clazz = Class.forName(name);
        return clazz;
    }

    public static Object getEntity(Object craftEntity) {
        try {
            Class<?> craftClass       = getClazz("net.minecraft.world.entity.Entity");
            Class<?> craftEntityClass = getCraftClass("org.bukkit.entity.Entity");

            Method getHandle = Reflex.getMethod(craftEntityClass, "getHandle");

            return craftClass.cast(getHandle.invoke(craftEntity));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Channel getChannel(Player p) {
        try {
            Object conn = getConnection(p);
            Object manager = Reflex.getFieldValue(conn,
                    Version.CURRENT == Version.V1_19_R1 ? "b" : "a");
            String  field   = Version.CURRENT.isHigher(Version.V1_18_R1) ? "m" : "k";
            Channel channel = (Channel) Reflex.getFieldValue(manager, field);

            return channel;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object getConnection(Player player) {
        try {
            Class craftPlayerClass = getCraftClass("entity.CraftPlayer");

            Method getHandle = Reflex.getMethod(craftPlayerClass, "getHandle");
            Object nmsPlayer = Reflex.invokeMethod(getHandle, getCraftPlayer(player));

            Object con = Reflex.getFieldValue(nmsPlayer, "b"); //WHY must you obfuscate
            return con;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void sendPacket(Player p, Object packet) {
        Object   conn        = getConnection(p);
        Class<?> packetClass = null;
        try {
            packetClass = getClazz("net.minecraft.network.protocol.Packet");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Method sendPacket;
        if (ReflectionUtil.MINOR_VERSION == 17) {
            sendPacket = Reflex.getMethod(conn.getClass(), "sendPacket", packetClass);
        } else { // if (ReflectionUtil.MINOR_VERSION > 17) { // If we're newer.. we're using obfuscated methods again ;(
            sendPacket = Reflex.getMethod(conn.getClass(), "a", packetClass);
        }
        Reflex.invokeMethod(sendPacket, conn, packet);
    }

    public static void sendAttackPacket(Player p, int id) {
        try {
            Object craftPlayer = getCraftPlayer(p);
            Object entity      = getEntity(craftPlayer);

            Class<?>    packetClass = getClazz("net.minecraft.network.protocol.game.PacketPlayOutAnimation");
            Constructor ctor        = Reflex.getConstructor(packetClass, entity.getClass(), int.class);
            Object      packet      = Reflex.invokeConstructor(ctor, entity, id);

            sendPacket(p, packet);
        } catch (ClassNotFoundException e) {
            NexEngine.get().getLogger().warning("Could not send attack packet.");
            e.printStackTrace();
        }
    }

    public static void openChestAnimation(Block chest, boolean open) {
        if (chest.getState() instanceof Chest) {
            Location lo     = chest.getLocation();
            World    bWorld = lo.getWorld();
            if (bWorld == null) return;

            try {
                Class<?> worldClass    = getClazz("net.minecraft.world.level.World");
                Class<?> craftWorld    = getCraftClass("CraftWorld");
                Class<?> blockClass    = getClazz("net.minecraft.world.level.block.Block");
                Class<?> blockPosClass = getClazz("net.minecraft.core.BlockPosition");

                Object cWorld    = craftWorld.cast(bWorld);
                Method getHandle = Reflex.getMethod(cWorld.getClass(), "getHandle");

                Object world = worldClass.cast(Reflex.invokeMethod(getHandle, cWorld));
                Method playBlockAction = Reflex.getMethod(craftWorld,
                        ReflectionUtil.MINOR_VERSION == 17 ? "playBlockAction" : "a", //Curse you obfuscation
                        blockPosClass, blockClass, int.class, int.class);

                Constructor ctor     = Reflex.getConstructor(blockPosClass, double.class, double.class, double.class);
                Object      position = Reflex.invokeConstructor(ctor, lo.getX(), lo.getY(), lo.getZ());

                Method getType = Reflex.getMethod(world.getClass(),
                        ReflectionUtil.MINOR_VERSION == 17 ? "getType" : "a_",
                        blockPosClass);
                Class<?> blockData = getClazz("net.minecraft.world.level.block.state.IBlockData");
                Object   data      = blockData.cast(Reflex.invokeMethod(getType, world, position));

                Method getBlock = Reflex.getMethod(blockData,
                        ReflectionUtil.MINOR_VERSION == 17 ? "getBlock" : "b");

                //TileEntityChest tileChest = (TileEntityChest) world.getTileEntity(position);
                Reflex.invokeMethod(playBlockAction, world, position, getBlock.invoke(data), 1, open ? 1 : 0);
            } catch (Exception e) {
                NexEngine.get().getLogger().warning("Problem sending chest animation");
                e.printStackTrace();
            }
        }
    }

    public static String toBase64(@NotNull ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream      dataOutput   = new DataOutputStream(outputStream);

            Object nbtTagListItems    = ReflectionUtil.newNBTTagList();
            Object nbtTagCompoundItem = ReflectionUtil.newNBTTagCompound();

            Object nmsItem = getNMSCopy(item);

            save(nmsItem, nbtTagCompoundItem);

            Method add = Reflex.getMethod(AbstractList.class, "add", Object.class);
            Reflex.invokeMethod(add, nbtTagListItems, nbtTagCompoundItem);

            Class<?> compressedClass = ReflectionUtil.MINOR_VERSION >= 17
                    ? getClazz("net.minecraft.nbt.NBTCompressedStreamTools")
                    : getNMSClass("NBTCompressedStreamTools");
            Method a = Reflex.getMethod(compressedClass, "a", nbtTagCompoundItem.getClass(), DataOutput.class);

            Reflex.invokeMethod(a, null, nbtTagCompoundItem, dataOutput);

            String str = new BigInteger(1, outputStream.toByteArray()).toString(32);

            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ItemStack fromBase64(@NotNull String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());

            Object nbtTagCompoundRoot;
            try {
                Class<?> compressedClass = ReflectionUtil.MINOR_VERSION >= 17
                        ? getClazz("net.minecraft.nbt.NBTCompressedStreamTools")
                        : getNMSClass("NBTCompressedStreamTools");
                Method a = Reflex.getMethod(compressedClass, "a", DataInput.class);

                nbtTagCompoundRoot = Reflex.invokeMethod(a, null, new DataInputStream(inputStream));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            Class<?> nmsItemClass = ReflectionUtil.MINOR_VERSION >= 17
                    ? getClazz("net.minecraft.world.item.ItemStack")
                    : getNMSClass("ItemStack");
            Class<?> compoundClass = ReflectionUtil.MINOR_VERSION >= 17
                    ? getClazz("net.minecraft.nbt.NBTTagCompound")
                    : getNMSClass("NBTTagCompound");
            Method a = Reflex.getMethod(nmsItemClass, "a", compoundClass);

            Object nmsItem = Reflex.invokeMethod(a, null, nbtTagCompoundRoot);

            Method asBukkitCopy = Reflex.getMethod(getCraftClass("inventory.CraftItemStack"), "asBukkitCopy", nmsItemClass);

            ItemStack item = (ItemStack) Reflex.invokeMethod(asBukkitCopy, null, nmsItem);

            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player) {
        //CraftItemStack craftItem = (CraftItemStack) item;
        try {
            Object nmsStack = getNMSCopy(item);

            Object nmsPlayer = player != null ? getEntity(getCraftPlayer(player)) : null;

            if (ReflectionUtil.MINOR_VERSION == 19) {
                Class<?> randSourceClass = getClazz("net.minecraft.util.RandomSource");
                Method isDamaged = Reflex.getMethod(
                        nmsStack.getClass(),
                        "a",
                        int.class,
                        randSourceClass,
                        getClazz("net.minecraft.server.level.EntityPlayer")
                );

                Object randSource = Reflex.invokeMethod(Reflex.getMethod(randSourceClass, "c"), null);
                Reflex.invokeMethod(isDamaged, nmsStack, amount, randSource, nmsPlayer);
            } else {
                Method isDamaged = Reflex.getMethod(
                        nmsStack.getClass(),
                        ReflectionUtil.MINOR_VERSION == 17 ? "isDamaged" : "a",
                        int.class,
                        Random.class,
                        getClazz("net.minecraft.server.level.EntityPlayer")
                );

                Reflex.invokeMethod(isDamaged, nmsStack, amount, Rnd.rnd, nmsPlayer);
            }

            return toBukkitCopy(nmsStack);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Multimap<Object, Object> getAttributes(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);
            Method getItem = Reflex.getMethod(nmsItem.getClass(),
                    ReflectionUtil.MINOR_VERSION > 17 ? "c" : "getItem");
            Object item = Reflex.invokeMethod(getItem, nmsItem);


            Class<Enum> enumItemSlotClass = (Class<Enum>) (
                    ReflectionUtil.MINOR_VERSION < 17
                            ? getNMSClass("EnumItemSlot")
                            : getClazz("net.minecraft.world.entity.EnumItemSlot")
            );
            Class<?> itemArmorClass = ReflectionUtil.MINOR_VERSION < 17
                    ? getNMSClass("ItemArmor")
                    : getClazz("net.minecraft.world.item.ItemArmor");
            Class<?> itemToolClass = ReflectionUtil.MINOR_VERSION < 17
                    ? getNMSClass("ItemTool")
                    : getClazz("net.minecraft.world.item.ItemTool");
            Class<?> itemSwordClass = ReflectionUtil.MINOR_VERSION < 17
                    ? getNMSClass("ItemSword")
                    : getClazz("net.minecraft.world.item.ItemSword");
            Class<?> itemTridentClass = ReflectionUtil.MINOR_VERSION < 17
                    ? getNMSClass("ItemTrident")
                    : getClazz("net.minecraft.world.item.ItemTrident");

            Enum mainhand = (Enum) Reflex.invokeMethod(
                    Reflex.getMethod(enumItemSlotClass,
                            ReflectionUtil.MINOR_VERSION <= 17 ? "fromName" : "a",
                            String.class),
                    null, "mainhand");

            if (itemArmorClass.isInstance(item)) {
                Object tool = itemArmorClass.cast(item);
                Method b    = Reflex.getMethod(itemArmorClass, "b");
                Object bObj = Reflex.invokeMethod(b, tool);
                Method a    = Reflex.getMethod(itemArmorClass, "a", enumItemSlotClass);

                return (Multimap<Object, Object>) Reflex.invokeMethod(a, tool, bObj);
            }

            Object tool;
            Method a;
            if (itemToolClass.isInstance(item)) {
                tool = itemToolClass.cast(item);
                a = Reflex.getMethod(itemToolClass, "a", enumItemSlotClass);
            } else if (itemSwordClass.isInstance(item)) {
                tool = itemSwordClass.cast(item);
                a = Reflex.getMethod(itemSwordClass, "a", enumItemSlotClass);
            } else if (itemTridentClass.isInstance(item)) {
                tool = itemTridentClass.cast(item);
                a = Reflex.getMethod(itemTridentClass, "a", enumItemSlotClass);
            } else {
                return null;
            }

            return (Multimap<Object, Object>) Reflex.invokeMethod(a, tool, mainhand);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static double getAttributeValue(@NotNull ItemStack item, @NotNull Object attribute) {
        try {
            Class<?>                 attributeModifierClass = getClazz("net.minecraft.world.entity.ai.attributes.AttributeModifier");
            Class<?>                 attributeBaseClass     = getClazz("net.minecraft.world.entity.ai.attributes.AttributeBase");
            Multimap<Object, Object> attMap                 = getAttributes(item);
            if (attMap == null || attMap.isEmpty()) return 0D;

            Collection<Object> att = attMap.get(attributeBaseClass.cast(attribute));
            if (att == null || att.isEmpty()) return 0D;
            Object mod = attributeModifierClass.cast(att.stream().findFirst().get());

            Method getAmount = Reflex.getMethod(attributeModifierClass,
                    ReflectionUtil.MINOR_VERSION == 17 ? "getAmount" : "d");
            double damage = (double) Reflex.invokeMethod(getAmount, mod);

            return damage + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static double getDefaultDamage(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("f")); // generic.attack_damage
    }

    public static double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("h")); // generic.attack_speed
    }

    public static double getDefaultArmor(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("i")); // generic.armor
    }

    public static double getDefaultToughness(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("j")); // generic.armor_toughness
    }

    public static Object getGenericAttribute(String field) {
        try {
            Class<?> attributes = getClazz("net.minecraft.world.entity.ai.attributes.GenericAttributes");
            Object   value      = Reflex.getField(attributes, field).get(null);

            //AttributeBase or IAttribute
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isWeapon(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);

            Method getItem = Reflex.getMethod(nmsItem.getClass(), "getItem");

            Object item = Reflex.invokeMethod(getItem, nmsItem);

            Class<?> swordClass = ReflectionUtil.MINOR_VERSION < 17
                    ? getNMSClass("ItemSword")
                    : getClazz("net.minecraft.world.item.ItemSword");
            Class<?> axeClass = ReflectionUtil.MINOR_VERSION < 17
                    ? getNMSClass("ItemAxe")
                    : getClazz("net.minecraft.world.item.ItemAxe");
            Class<?> tridentClass = ReflectionUtil.MINOR_VERSION < 17
                    ? getNMSClass("ItemTrident")
                    : getClazz("net.minecraft.world.item.ItemTrident");

            return swordClass.isInstance(item) || axeClass.isInstance(item) || tridentClass.isInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isTool(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);

            Method getItem = Reflex.getMethod(nmsItem.getClass(), "getItem");

            Object item = Reflex.invokeMethod(getItem, nmsItem);

            Class<?> toolClass = ReflectionUtil.MINOR_VERSION < 17
                    ? getNMSClass("ItemTool")
                    : getClazz("net.minecraft.world.item.ItemTool");

            return toolClass.isInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isArmor(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);

            Method getItem = Reflex.getMethod(nmsItem.getClass(), "getItem");

            Object item = Reflex.invokeMethod(getItem, nmsItem);

            Class<?> armorClass = ReflectionUtil.MINOR_VERSION < 17
                    ? getNMSClass("ItemArmor")
                    : getClazz("net.minecraft.world.item.ItemArmor");

            return armorClass.isInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String fixColors(@NotNull String str) {
        try {
            str = str.replace("\n", "%n%"); // CraftChatMessage wipes all lines out.

            Class<?> baseComponentClass = ReflectionUtil.MINOR_VERSION < 17
                    ? getNMSClass("IChatBaseComponent")
                    : getClazz("net.minecraft.network.chat.IChatBaseComponent");
            Class<?> chatMessageClass = getCraftClass("util.CraftChatMessage");

            Method fromComponent    = Reflex.getMethod(chatMessageClass, "fromComponent", baseComponentClass);
            Method fromStringOrNull = Reflex.getMethod(chatMessageClass, "fromStringOrNull", String.class);

            Object baseComponent = Reflex.invokeMethod(fromStringOrNull, null, str);
            String singleColor   = (String) Reflex.invokeMethod(fromComponent, null, baseComponentClass.cast(baseComponent));
            return singleColor.replace("%n%", "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return str;
    }

    public static float getAttackCooldown(Player p) {
        try {
            Class<?> entityPlayerClass = getClazz("net.minecraft.server.level.EntityPlayer");
            Class    entityHumanClass  = getClazz("net.minecraft.world.entity.player.EntityHuman");
            Object   craftPlayer       = getCraftPlayer(p);
            Method   getHandle         = Reflex.getMethod(craftPlayer.getClass(), "getHandle");

            Object ep = entityPlayerClass.cast(Reflex.invokeMethod(getHandle, craftPlayer));

            Method getAttackCooldown = Reflex.getMethod(entityHumanClass,
                    ReflectionUtil.MINOR_VERSION == 17 ? "getAttackCooldown" : "v", float.class);
            if (getAttackCooldown == null)
                throw new NullPointerException("Could not find a \"getAttackCooldown\" method using Reflection.");

            return (float) Reflex.invokeMethod(getAttackCooldown, entityHumanClass.cast(ep), 0f);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void changeSkull(Block b, String hash) {
        try {
            Class<?> tileSkullClass  = getClazz("net.minecraft.world.level.block.entity.TileEntitySkull");
            Class<?> craftWorldClass = getCraftClass("CraftWorld");
//            Class<?> worldServerClass = getNMSClass("WorldServer");
            Class<?> blockAccessClass = getClazz("net.minecraft.world.level.IBlockAccess");
            Class<?> blockPosClass    = getClazz("net.minecraft.core.BlockPosition");

            Constructor ctor = Reflex.getConstructor(blockPosClass, int.class, int.class, int.class);

            Method getHandle = Reflex.getMethod(craftWorldClass, "getHandle");
            Method getTileEntity = Reflex.getMethod(blockAccessClass,
                    ReflectionUtil.MINOR_VERSION == 17 ? "getTileEntity" : "c_", blockPosClass);

            Object bPos = Reflex.invokeConstructor(ctor, b.getX(), b.getY(), b.getZ());

            Object worldServer = Reflex.invokeMethod(getHandle, craftWorldClass.cast(b.getWorld()));
            Object skullTile   = tileSkullClass.cast(Reflex.invokeMethod(getTileEntity, worldServer, bPos));


            Method setGameProfile = Reflex.getMethod(tileSkullClass,
                    ReflectionUtil.MINOR_VERSION == 17 ? "setGameProfile" : "a", GameProfile.class);

            Reflex.invokeMethod(setGameProfile, skullTile, getNonPlayerProfile(hash));
            b.getState().update(true);
        } catch (Exception e) {
            NexEngine.get().getLogger().warning("Could not update skull");
            e.printStackTrace();
        }
    }
}
