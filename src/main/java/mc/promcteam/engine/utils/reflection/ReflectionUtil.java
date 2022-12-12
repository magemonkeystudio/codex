package mc.promcteam.engine.utils.reflection;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import mc.promcteam.engine.NexEngine;
import mc.promcteam.engine.utils.Reflex;
import mc.promcteam.engine.utils.constants.JNumbers;
import mc.promcteam.engine.utils.random.Rnd;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;

public class ReflectionUtil {

    public static final String VERSION       = Bukkit.getServer().getClass().getPackage().getName().contains("mockbukkit")
            ? "testing_19"
            : Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    public static final int    MINOR_VERSION = Integer.parseInt(VERSION.split("_")[1]);

    protected static Object newNBTTagCompound() {
        if (ReflectionUtil.MINOR_VERSION >= 17) return Reflection_1_17.newNBTTagCompound();

        try {
            Class<?> nbtTagClass = getNMSClass("NBTTagCompound");
            return nbtTagClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static Object newNBTTagList() {
        if (ReflectionUtil.MINOR_VERSION >= 17) return Reflection_1_17.newNBTTagList();

        try {
            Class<?> nbtTagClass = getNMSClass("NBTTagList");
            return nbtTagClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object getNMSCopy(ItemStack item) {
        try {
            Class<?> craftItemClass = getCraftClass("inventory.CraftItemStack");
            Method   asNMSCopy      = Reflex.getMethod(craftItemClass, "asNMSCopy", ItemStack.class);

            return Reflex.invokeMethod(asNMSCopy, null, item);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected static ItemStack toBukkitCopy(Object nmsItem) {
        if (ReflectionUtil.MINOR_VERSION >= 17) return Reflection_1_17.toBukkitCopy(nmsItem);

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

    protected static Object save(Object nmsItem, Object nbtCompound) {
        try {
            Method save = Reflex.getMethod(nmsItem.getClass(),
                    ReflectionUtil.MINOR_VERSION >= 18
                            ? "b"
                            : "save",
                    nbtCompound.getClass());

            return Reflex.invokeMethod(save, nmsItem, nbtCompound);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String   version  = VERSION + ".";
        String   name     = "net.minecraft.server." + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    public static Class<?> getCraftClass(String craftClassString) throws ClassNotFoundException {
        String   version    = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String   name       = "org.bukkit.craftbukkit." + version + craftClassString;
        Class<?> craftClass = Class.forName(name);
        return craftClass;
    }

    public static Object getConnection(Player player) {
        if (ReflectionUtil.MINOR_VERSION >= 17) return Reflection_1_17.getConnection(player);

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

    public static Object getCraftPlayer(Player p) {
        try {
            Class<?> craftClass = getCraftClass("entity.CraftPlayer");
            return craftClass.cast(p);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object getEntity(Object craftEntity) {
        if (ReflectionUtil.MINOR_VERSION >= 17) return Reflection_1_17.getEntity(craftEntity);

        try {
            Class<?> craftClass = getNMSClass("Entity");

            Method getHandle = Reflex.getMethod(craftClass, "getHandle");

            return craftClass.cast(getHandle.invoke(craftEntity));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Channel getChannel(Player p) {
        if (ReflectionUtil.MINOR_VERSION >= 17) return Reflection_1_17.getChannel(p);

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

    public static void sendPackets(Player p, Collection<Object> packets) {
        for (Object packet : packets) {
            sendPacket(p, packet);
        }
    }

    public static void sendPacket(Player p, Object packet) {
        if (ReflectionUtil.MINOR_VERSION >= 17) {
            Reflection_1_17.sendPacket(p, packet);
            return;
        }

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

    public static void sendAttackPacket(Player p, int id) {
        if (ReflectionUtil.MINOR_VERSION >= 17) {
            Reflection_1_17.sendAttackPacket(p, id);
            return;
        }

        try {
            Object craftPlayer = getCraftPlayer(p);
            Object entity      = getEntity(craftPlayer);

            Class<?>    packetClass = getNMSClass("PacketPlayOutAnimation");
            Constructor ctor        = Reflex.getConstructor(packetClass, entity.getClass(), int.class);
            Object      packet      = Reflex.invokeConstructor(ctor, entity, id);

            sendPacket(p, packet);
        } catch (ClassNotFoundException e) {
            NexEngine.get().getLogger().warning("Could not send attack packet.");
            e.printStackTrace();
        }
    }

    public static void openChestAnimation(Block chest, boolean open) {
        if (ReflectionUtil.MINOR_VERSION >= 17) {
            Reflection_1_17.openChestAnimation(chest, open);
            return;
        }

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
                Method getHandle = ReflectionUtil.MINOR_VERSION >= 8
                        ? Reflex.getMethod(nmsWorld.getClass(), "getWorld")
                        : Reflex.getMethod(nmsWorld.getClass(), "getHandle");

                Object world           = craftWorld.cast(Reflex.invokeMethod(getHandle, nmsWorld));
                Method playBlockAction = Reflex.getMethod(craftWorld, "playBlockAction", blockPosClass, blockClass, int.class, int.class);

                Constructor ctor     = Reflex.getConstructor(blockPosClass, double.class, double.class, double.class);
                Object      position = Reflex.invokeConstructor(ctor, lo.getX(), lo.getY(), lo.getZ());

                Method   getType   = Reflex.getMethod(world.getClass(), "getType", blockPosClass);
                Class<?> blockData = getNMSClass("IBlockData");
                Object   data      = blockData.cast(Reflex.invokeMethod(getType, world, position));

                Method getBlock = Reflex.getMethod(blockData, "getBlock");

                //TileEntityChest tileChest = (TileEntityChest) world.getTileEntity(position);
                Reflex.invokeMethod(playBlockAction, world, position, getBlock.invoke(data), 1, open ? 1 : 0);
            } catch (Exception e) {
                NexEngine.get().getLogger().warning("Problem sending chest animation");
                e.printStackTrace();
            }
        }
    }

    public static String toJSON(@NotNull ItemStack item) {
        try {
            Object nbtCompound = newNBTTagCompound();
            Object nmsItem     = getNMSCopy(item);

            nbtCompound = save(nmsItem, nbtCompound);

            Method toString = Reflex.getMethod(nbtCompound.getClass(), "toString");

            String js = (String) Reflex.invokeMethod(toString, nbtCompound);
            if (js.length() > JNumbers.JSON_MAX) {
                ItemStack item2 = new ItemStack(item.getType());
                return toJSON(item2);
            }

            return js;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String toBase64(@NotNull ItemStack item) {
        return Reflection_1_17.toBase64(item);
    }

    public static ItemStack fromBase64(@NotNull String data) {
        return Reflection_1_17.fromBase64(data);
    }

    public static String getNbtString(@NotNull ItemStack item) {
        try {
            Object nmsCopy        = getNMSCopy(item);
            Method getOrCreateTag = Reflex.getMethod(nmsCopy.getClass(), "getOrCreateTag");
            Object tag            = Reflex.invokeMethod(getOrCreateTag, nmsCopy);
            Method asString       = Reflex.getMethod(tag.getClass(), "asString");
            return (String) Reflex.invokeMethod(asString, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player) {
        if (ReflectionUtil.MINOR_VERSION >= 17) return Reflection_1_17.damageItem(item, amount, player);

        //CraftItemStack craftItem = (CraftItemStack) item;
        try {
            Object nmsStack = getNMSCopy(item);

            Object nmsPlayer = player != null ? getEntity(getCraftPlayer(player)) : null;

            Method isDamaged = Reflex.getMethod(nmsStack.getClass(), "isDamaged", int.class, Random.class, getNMSClass("EntityPlayer"));

            Reflex.invokeMethod(isDamaged, nmsStack, amount, Rnd.rnd, nmsPlayer);

            return toBukkitCopy(nmsStack);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Multimap<Object, Object> getAttributes(@NotNull ItemStack itemStack) {
        return Reflection_1_17.getAttributes(itemStack);
    }

    public static double getAttributeValue(@NotNull ItemStack item, @NotNull Object attribute) {
        if (ReflectionUtil.MINOR_VERSION >= 17) return Reflection_1_17.getAttributeValue(item, attribute);

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
                Object mod = attributeModifierClass.cast((att == null || att.isEmpty())
                        ? 0
                        : att.stream().findFirst().get());

                Method getAmount = Reflex.getMethod(attributeModifierClass, "getAmount");
                value = (double) Reflex.invokeMethod(getAmount, mod);
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

    public static double getDefaultDamage(@NotNull ItemStack itemStack) {
        if (ReflectionUtil.MINOR_VERSION >= 17) return Reflection_1_17.getDefaultDamage(itemStack);

        return getAttributeValue(itemStack, getGenericAttribute("ATTACK_DAMAGE"));
    }

    public static double getDefaultSpeed(@NotNull ItemStack itemStack) {
        if (ReflectionUtil.MINOR_VERSION >= 17) return Reflection_1_17.getDefaultSpeed(itemStack);

        return getAttributeValue(itemStack, getGenericAttribute("ATTACK_SPEED"));
    }

    public static double getDefaultArmor(@NotNull ItemStack itemStack) {
        if (ReflectionUtil.MINOR_VERSION >= 17) return Reflection_1_17.getDefaultArmor(itemStack);

        return getAttributeValue(itemStack, getGenericAttribute("ARMOR"));
    }

    public static double getDefaultToughness(@NotNull ItemStack itemStack) {
        if (ReflectionUtil.MINOR_VERSION >= 17) return Reflection_1_17.getDefaultToughness(itemStack);

        return getAttributeValue(itemStack, getGenericAttribute("ARMOR_TOUGHNESS"));
    }

    public static Object getGenericAttribute(String field) {
        if (ReflectionUtil.MINOR_VERSION >= 17) return Reflection_1_17.getGenericAttribute(field);

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

    public static boolean isWeapon(@NotNull ItemStack itemStack) {
        return Reflection_1_17.isWeapon(itemStack);
    }

    public static boolean isTool(@NotNull ItemStack itemStack) {
        return Reflection_1_17.isTool(itemStack);
    }

    public static boolean isArmor(@NotNull ItemStack itemStack) {
        return Reflection_1_17.isArmor(itemStack);
    }

    public static String fixColors(@NotNull String str) {
        return Reflection_1_17.fixColors(str);
    }

    public static float getAttackCooldown(Player p) {
        if (ReflectionUtil.MINOR_VERSION >= 17) return Reflection_1_17.getAttackCooldown(p);

        try {
            Class<?> entityPlayerClass = getNMSClass("EntityPlayer");
            Class    entityHumanClass  = getNMSClass("EntityHuman");
            Object   craftPlayer       = getCraftPlayer(p);
            Method   getHandle         = Reflex.getMethod(craftPlayer.getClass(), "getHandle");

            Object ep = entityPlayerClass.cast(Reflex.invokeMethod(getHandle, craftPlayer));

            if (MINOR_VERSION < 16) {
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

    public static void changeSkull(Block b, String hash) {
        if (ReflectionUtil.MINOR_VERSION >= 17) {
            Reflection_1_17.changeSkull(b, hash);
            return;
        }

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
            NexEngine.get().getLogger().warning("Could not update skull");
            e.printStackTrace();
        }
    }

    protected static GameProfile getNonPlayerProfile(String hash) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", hash));
        return profile;
    }

}
