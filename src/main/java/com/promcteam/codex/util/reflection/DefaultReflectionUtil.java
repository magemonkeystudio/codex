package com.promcteam.codex.util.reflection;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.util.Reflex;
import com.promcteam.codex.util.random.Rnd;
import io.netty.channel.Channel;
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

public class DefaultReflectionUtil implements ReflectionUtil {

    @Override
    public Object newNBTTagCompound() {
        try {
            Class<?> nbtTagClass = getNMSClass("NBTTagCompound");
            return nbtTagClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object newNBTTagList() {
        try {
            Class<?> nbtTagClass = getNMSClass("NBTTagList");
            return nbtTagClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ItemStack toBukkitCopy(Object nmsItem) {
        try {
            Class<?> craftItem    = getCraftClass("inventory.CraftItemStack");
            Method   asBukkitCopy = Reflex.getMethod(craftItem, "asBukkitCopy", getNMSClass("ItemStack"));
            if (asBukkitCopy == null) return null;

            return (ItemStack) Reflex.invokeMethod(asBukkitCopy, null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Object save(Object nmsItem, Object nbtCompound) {
        try {
            Method save = Reflex.getMethod(nmsItem.getClass(),
                    "save",
                    nbtCompound.getClass());

            return Reflex.invokeMethod(save, nmsItem, nbtCompound);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Object getConnection(Player player) {
        try {
            Class craftPlayerClass = getCraftClass("entity.CraftPlayer");

            Method getHandle = Reflex.getMethod(craftPlayerClass, "getHandle");
            Object nmsPlayer = Reflex.invokeMethod(getHandle, getCraftPlayer(player));

            Object con = Reflex.getFieldValue(nmsPlayer, "playerConnection");
            return con;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Object getEntity(Object craftEntity) {
        try {
            Class<?> craftClass = getNMSClass("Entity");

            Method getHandle = Reflex.getMethod(craftClass, "getHandle");

            return craftClass.cast(getHandle.invoke(craftEntity));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Channel getChannel(Player p) {
        try {
            Object  conn    = getConnection(p);
            Object  manager = Reflex.getFieldValue(conn, "networkManager");
            Channel channel = (Channel) Reflex.getFieldValue(manager, "channel");

            return channel;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void sendPacket(Player p, Object packet) {
        Object   conn        = getConnection(p);
        Class<?> packetClass = null;
        try {
            packetClass = getNMSClass("Packet");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Method sendMethod = Reflex.getMethod(conn.getClass(), "sendPacket", packetClass);
        Reflex.invokeMethod(sendMethod, conn, packet);
    }

    @Override
    public void sendAttackPacket(Player p, int id) {
        try {
            Object craftPlayer = getCraftPlayer(p);
            Object entity      = getEntity(craftPlayer);

            Class<?>    packetClass = getNMSClass("PacketPlayOutAnimation");
            Constructor ctor        = Reflex.getConstructor(packetClass, entity.getClass(), int.class);
            Object      packet      = Reflex.invokeConstructor(ctor, entity, id);

            sendPacket(p, packet);
        } catch (ClassNotFoundException e) {
            CodexEngine.get().getLogger().warning("Could not send attack packet.");
            e.printStackTrace();
        }
    }

    @Override
    public void openChestAnimation(Block chest, boolean open) {
        if (chest.getState() instanceof Chest) {
            Location lo     = chest.getLocation();
            World    bWorld = lo.getWorld();
            if (bWorld == null) return;

            try {
                Class<?> worldClass    = getNMSClass("World");
                Class<?> craftWorld    = getCraftClass("CraftWorld");
                Class<?> blockClass    = getNMSClass("Block");
                Class<?> blockPosClass = getNMSClass("BlockPosition");

                Object nmsWorld = worldClass.cast(bWorld);
                Method getHandle = ReflectionManager.MINOR_VERSION >= 8
                        ? Reflex.getMethod(nmsWorld.getClass(), "getWorld")
                        : Reflex.getMethod(nmsWorld.getClass(), "getHandle");

                Object world = craftWorld.cast(Reflex.invokeMethod(getHandle, nmsWorld));
                Method playBlockAction = Reflex.getMethod(craftWorld,
                        "playBlockAction",
                        blockPosClass,
                        blockClass,
                        int.class,
                        int.class);

                Constructor ctor     = Reflex.getConstructor(blockPosClass, double.class, double.class, double.class);
                Object      position = Reflex.invokeConstructor(ctor, lo.getX(), lo.getY(), lo.getZ());

                Method   getType   = Reflex.getMethod(world.getClass(), "getType", blockPosClass);
                Class<?> blockData = getNMSClass("IBlockData");
                Object   data      = blockData.cast(Reflex.invokeMethod(getType, world, position));

                Method getBlock = Reflex.getMethod(blockData, "getBlock");

                //TileEntityChest tileChest = (TileEntityChest) world.getTileEntity(position);
                Reflex.invokeMethod(playBlockAction, world, position, getBlock.invoke(data), 1, open ? 1 : 0);
            } catch (Exception e) {
                CodexEngine.get().getLogger().warning("Problem sending chest animation");
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toBase64(@NotNull ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream      dataOutput   = new DataOutputStream(outputStream);

            Object nbtTagListItems    = newNBTTagList();
            Object nbtTagCompoundItem = newNBTTagCompound();

            Object nmsItem = getNMSCopy(item);

            save(nmsItem, nbtTagCompoundItem);

            Method add = Reflex.getMethod(AbstractList.class, "add", Object.class);
            Reflex.invokeMethod(add, nbtTagListItems, nbtTagCompoundItem);

            Class<?> compressedClass = getNMSClass("NBTCompressedStreamTools");
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

            Object   nbtTagCompoundRoot;
            Class<?> compressedClass;
            try {
                compressedClass = getNMSClass("NBTCompressedStreamTools");
            } catch (ClassNotFoundException e) {
                compressedClass = Reflex.getClass("net.minecraft.nbt", "NBTCompressedStreamTools");
            }
            Method nbtA = Reflex.getMethod(compressedClass, "a", DataInput.class);

            nbtTagCompoundRoot = Reflex.invokeMethod(nbtA, null, new DataInputStream(inputStream));

            Class<?> nmsItemClass  = getNMSClass("ItemStack");
            Class<?> compoundClass = getNMSClass("NBTTagCompound");
            Method   a             = Reflex.getMethod(nmsItemClass, "a", compoundClass);

            Object nmsItem = Reflex.invokeMethod(a, null, nbtTagCompoundRoot);

            Method asBukkitCopy =
                    Reflex.getMethod(getCraftClass("inventory.CraftItemStack"), "asBukkitCopy", nmsItemClass);

            ItemStack item = (ItemStack) Reflex.invokeMethod(asBukkitCopy, null, nmsItem);

            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player) {
        try {
            Object nmsStack = getNMSCopy(item);

            Object nmsPlayer = player != null ? getEntity(getCraftPlayer(player)) : null;

            Method isDamaged =
                    Reflex.getMethod(nmsStack.getClass(), "a", int.class, Random.class, getNMSClass("EntityPlayer"));

            Reflex.invokeMethod(isDamaged, nmsStack, amount, Rnd.rnd, nmsPlayer);

            return toBukkitCopy(nmsStack);
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
                    ReflectionManager.MINOR_VERSION > 17 ? "c" : "getItem");
            Object item = Reflex.invokeMethod(getItem, nmsItem);


            Class<Enum> enumItemSlotClass = (Class<Enum>) (
                    getNMSClass("EnumItemSlot"));
            Class<?> itemArmorClass   = getNMSClass("ItemArmor");
            Class<?> itemToolClass    = getNMSClass("ItemTool");
            Class<?> itemSwordClass   = getNMSClass("ItemSword");
            Class<?> itemTridentClass = getNMSClass("ItemTrident");

            Enum mainhand = (Enum) Reflex.invokeMethod(
                    Reflex.getMethod(enumItemSlotClass,
                            "fromName",
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

    @Override
    public double getAttributeValue(@NotNull ItemStack item, @NotNull Object attribute) {
        double value = 0;
        try {
            Class<?> attributeModifierClass = getNMSClass("AttributeModifier");
            if (attribute.getClass().getSuperclass().getSimpleName().equals("IAttribute")) {
                Class<?>                 iAttributeClass = getNMSClass("IAttribute");
                Multimap<Object, Object> attMap          = getAttributes(item);
                if (attMap == null) return 0D;
                Object atkDmg  = iAttributeClass.cast(attribute);
                Method getName = Reflex.getMethod(iAttributeClass, "getName");

                //Collection<AttributeModifier>
                Collection<Object> att = attMap.get(Reflex.invokeMethod(getName, atkDmg));
                Object mod = attributeModifierClass.cast((att == null || att.isEmpty())
                        ? 0
                        : att.stream().findFirst().get());

                Method getAmount = Reflex.getMethod(attributeModifierClass, "getAmount");
                value = (double) Reflex.invokeMethod(getAmount, mod);
            } else if (getNMSClass("AttributeBase").isInstance(attribute)) {
                Class<?>                 attributeBaseClass = getNMSClass("AttributeBase");
                Multimap<Object, Object> attMap             = getAttributes(item);
                if (attMap == null) return 0D;

                Collection<Object> att = attMap.get(attributeBaseClass.cast(attribute));
                Object mod = att != null && !att.isEmpty()
                        ? attributeModifierClass.cast(att.stream().findFirst().get())
                        : null;

                if (mod != null) {
                    Method getAmount = Reflex.getMethod(attributeModifierClass, "getAmount");
                    value = (double) Reflex.invokeMethod(getAmount, mod);
                } else value = 0;
            }
            if (attribute.equals(getGenericAttribute("ATTACK_DAMAGE"))) {
                value += 1;
            } else if (attribute.equals(getGenericAttribute("ATTACK_SPEED"))) {
                value += 4;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    @Override
    public double getDefaultDamage(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("ATTACK_DAMAGE"));
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("ATTACK_SPEED"));
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("ARMOR"));
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("ARMOR_TOUGHNESS"));
    }

    @Override
    public Object getGenericAttribute(String field) {
        try {
            Class<?> attributes = getNMSClass("GenericAttributes");
            Object   value      = Reflex.getField(attributes, field).get(null);

            //AttributeBase or IAttribute
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean isWeapon(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);

            Method getItem = Reflex.getMethod(nmsItem.getClass(), "getItem");

            Object item = Reflex.invokeMethod(getItem, nmsItem);

            Class<?> swordClass   = getNMSClass("ItemSword");
            Class<?> axeClass     = getNMSClass("ItemAxe");
            Class<?> tridentClass = getNMSClass("ItemTrident");

            return swordClass.isInstance(item) || axeClass.isInstance(item) || tridentClass.isInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isTool(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);

            Method getItem = Reflex.getMethod(nmsItem.getClass(), "getItem");

            Object item = Reflex.invokeMethod(getItem, nmsItem);

            Class<?> toolClass = getNMSClass("ItemTool");

            return toolClass.isInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isArmor(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);

            Method getItem = Reflex.getMethod(nmsItem.getClass(), "getItem");

            Object item = Reflex.invokeMethod(getItem, nmsItem);

            Class<?> toolClass = getNMSClass("ItemArmor");

            return toolClass.isInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String fixColors(@NotNull String str) {
        try {
            str = str.replace("\n", "%n%"); // CraftChatMessage wipes all lines out.

            Class<?> baseComponentClass;
            try {
                baseComponentClass = getNMSClass("IChatBaseComponent");
            } catch (ClassNotFoundException e) {
                baseComponentClass = Reflex.getClass("net.minecraft.network.chat", "IChatBaseComponent");
            }
            Class<?> chatMessageClass = getCraftClass("util.CraftChatMessage");

            Method fromComponent    = Reflex.getMethod(chatMessageClass, "fromComponent", baseComponentClass);
            Method fromStringOrNull = Reflex.getMethod(chatMessageClass, "fromStringOrNull", String.class);

            Object baseComponent = Reflex.invokeMethod(fromStringOrNull, null, str);
            String singleColor =
                    (String) Reflex.invokeMethod(fromComponent, null, baseComponentClass.cast(baseComponent));
            return singleColor.replace("%n%", "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return str;
    }

    @Override
    public float getAttackCooldown(Player p) {
        try {
            Class<?> entityPlayerClass = getNMSClass("EntityPlayer");
            Class    entityHumanClass  = getNMSClass("EntityHuman");
            Object   craftPlayer       = getCraftPlayer(p);
            Method   getHandle         = Reflex.getMethod(craftPlayer.getClass(), "getHandle");

            Object ep = entityPlayerClass.cast(Reflex.invokeMethod(getHandle, craftPlayer));

            if (ReflectionManager.MINOR_VERSION < 16) {
                Method s = Reflex.getMethod(entityHumanClass, "s", float.class);
                if (s == null)
                    throw new NullPointerException("Could not find a \"s\" method using Reflection.");

                return (float) Reflex.invokeMethod(s, entityHumanClass.cast(ep), 0f);
            } else {
                Method getAttackCooldown = Reflex.getMethod(entityHumanClass, "getAttackCooldown", float.class);
                if (getAttackCooldown == null)
                    throw new NullPointerException("Could not find a \"getAttackCooldown\" method using Reflection.");

                return (float) Reflex.invokeMethod(getAttackCooldown, entityHumanClass.cast(ep), 0f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public void changeSkull(Block b, String hash) {
        try {
            Class<?> tileSkullClass  = getNMSClass("TileEntitySkull");
            Class<?> craftWorldClass = getCraftClass("CraftWorld");
//            Class<?> worldServerClass = getNMSClass("WorldServer");
            Class<?> blockAccessClass = getNMSClass("IBlockAccess");
            Class<?> blockPosClass    = getNMSClass("BlockPosition");

            Constructor ctor = Reflex.getConstructor(blockPosClass, int.class, int.class, int.class);

            Method getHandle     = Reflex.getMethod(craftWorldClass, "getHandle");
            Method getTileEntity = Reflex.getMethod(blockAccessClass, "getTileEntity", blockPosClass);

            Object bPos = Reflex.invokeConstructor(ctor, b.getX(), b.getY(), b.getZ());

            Object worldServer = Reflex.invokeMethod(getHandle, craftWorldClass.cast(b.getWorld()));
            Object skullTile   = tileSkullClass.cast(Reflex.invokeMethod(getTileEntity, worldServer, bPos));


            Method setGameProfile = Reflex.getMethod(tileSkullClass, "setGameProfile", GameProfile.class);

            Reflex.invokeMethod(setGameProfile, skullTile, getNonPlayerProfile(hash));
            b.getState().update(true);
        } catch (Exception e) {
            CodexEngine.get().getLogger().warning("Could not update skull");
            e.printStackTrace();
        }
    }
}
