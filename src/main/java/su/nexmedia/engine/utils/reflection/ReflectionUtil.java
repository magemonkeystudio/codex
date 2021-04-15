package su.nexmedia.engine.utils.reflection;

import com.google.common.collect.Multimap;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.constants.JNumbers;
import su.nexmedia.engine.utils.random.Rnd;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Random;

public class ReflectionUtil {

    public static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

    private static Object newNBTTagCompound() {
        try {
            Class nbtTagClass = getNMSClass("NBTTagCompound");
            return nbtTagClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object newNBTTagList() {
        try {
            Class nbtTagClass = getNMSClass("NBTTagList");
            return nbtTagClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Object getNMSCopy(ItemStack item) {
        try {
            Class craftItemClass = getCraftClass("inventory.CraftItemStack");
            Method asNMSCopy = craftItemClass.getMethod("asNMSCopy", ItemStack.class);

            return asNMSCopy.invoke(null, item);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static ItemStack toBukkitCopy(Object nmsItem) {
        try {
            Class craftItem = getCraftClass("inventory.CraftItemStack");
            Method asBukkitCopy = craftItem.getMethod("asBukkitCopy", getNMSClass("ItemStack"));

            return (ItemStack) asBukkitCopy.invoke(null, craftItem);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Object save(Object nmsItem, Object nbtCompound) {
        try {
            Method save = nmsItem.getClass().getMethod("save", nbtCompound.getClass());

            return save.invoke(nmsItem, nbtCompound);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = VERSION + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    public static Class<?> getCraftClass(String craftClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "org.bukkit.craftbukkit." + version + craftClassString;
        Class<?> craftClass = Class.forName(name);
        return craftClass;
    }

    public static Object getConnection(Player player) {
        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object nmsPlayer = getHandle.invoke(player);
            Field conField = nmsPlayer.getClass().getField("playerConnection");
            Object con = conField.get(nmsPlayer);
            return con;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object getCraftPlayer(Player p) {
        try {
            Class craftClass = getCraftClass("entity.CraftPlayer");
            return craftClass.cast(p);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object getEntity(Object craftPlayer) {
        try {
            Class craftClass = getNMSClass("Entity");

            Method getHandle = craftPlayer.getClass().getMethod("getHandle");

            return craftClass.cast(getHandle.invoke(craftPlayer));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Channel getChannel(Player p) {
        try {
            Object conn = getConnection(p);
            Field networkManager = conn.getClass().getField("networkManager");
            Object manager = networkManager.get(conn);
            Object channel = manager.getClass().getField("channel").get(manager);

            return (Channel) channel;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void sendPacket(Player p, Object packet) {
        try {
            Object conn = getConnection(p);
            Class<?> packetClass = getNMSClass("Packet");
            Method sendMethod = conn.getClass().getMethod("sendPacket", packetClass);
            sendMethod.invoke(conn, packet);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            System.err.println("Could not send packet to player " + p.getName());
            e.printStackTrace();
        }
    }

    public static void sendAttackPacket(Player p, int id) {
        try {
            Object craftPlayer = getCraftPlayer(p);
            Object entity = getEntity(craftPlayer);

            Class packetClass = getNMSClass("PacketPlayOutAnimation");
            Constructor ctor = packetClass.getConstructor(entity.getClass(), int.class);
            Object packet = ctor.newInstance(entity, id);

            sendPacket(p, packet);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException | InstantiationException e) {
            System.err.println("Could not send attack packet.");
            e.printStackTrace();
        }
    }

    public static void openChestAnimation(Block chest, boolean open) {
        if (chest.getState() instanceof Chest) {
            Location lo = chest.getLocation();
            World bWorld = lo.getWorld();
            if (bWorld == null) return;

            try {
                Class worldClass = getNMSClass("World");
                Class craftWorld = getCraftClass("CraftWorld");
                Class blockClass = getNMSClass("Block");
                Class blockPosClass = getNMSClass("BlockPosition");

                Object nmsWorld = worldClass.cast(bWorld);
                Method getHandle = nmsWorld.getClass().getMethod("getHandle");

                Object world = craftWorld.cast(getHandle.invoke(nmsWorld));
                Method playBlockAction = craftWorld.getMethod("playBlockAction", blockPosClass, blockClass, int.class, int.class);

                Constructor ctor = blockPosClass.getConstructor(double.class, double.class, double.class);
                Object position = ctor.newInstance(lo.getX(), lo.getY(), lo.getZ());

                Method getType = world.getClass().getMethod("getType", blockPosClass);
                Class blockData = getNMSClass("IBlockData");
                Object data = blockData.cast(getType.invoke(world, position));

                Method getBlock = blockData.getMethod("getBlock");

                //TileEntityChest tileChest = (TileEntityChest) world.getTileEntity(position);
                playBlockAction.invoke(world, position, getBlock.invoke(data), 1, open ? 1 : 0);
            } catch (Exception e) {
                System.err.println("Problem sending chest animation");
                e.printStackTrace();
            }
        }
    }

    public static String toJSON(@NotNull ItemStack item) {
        try {
            Object nbtCompound = newNBTTagCompound();
            Object nmsItem = getNMSCopy(item);

            nbtCompound = save(nmsItem, nbtCompound);

            Method toString = nbtCompound.getClass().getMethod("toString");

            String js = (String) toString.invoke(nbtCompound);
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
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutput = new DataOutputStream(outputStream);

            Object nbtTagListItems = newNBTTagList();
            Object nbtTagCompoundItem = newNBTTagCompound();

            Object nmsItem = getNMSCopy(item);

            save(nmsItem, nbtTagCompoundItem);

            //TODO Not sure this will work as 'add' uses generics
            Method add = nbtTagListItems.getClass().getMethod("add", nbtTagCompoundItem.getClass());
            add.invoke(nbtTagListItems, nbtTagCompoundItem);


            Class compressedClass = getNMSClass("NBTCompressedStreamTools");
            Method a = compressedClass.getMethod("a", nbtTagCompoundItem.getClass(), DataOutput.class);

            a.invoke(null, nbtTagCompoundItem, dataOutput);

            return new BigInteger(1, outputStream.toByteArray()).toString(32);
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
                Class compressedClass = getNMSClass("NBTCompressedStreamTools");
                Method a = compressedClass.getMethod("a", DataInputStream.class);

                nbtTagCompoundRoot = a.invoke(compressedClass, new DataInputStream(inputStream));
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }

            Class nmsItemClass = getNMSClass("ItemStack");
            Method a = nmsItemClass.getMethod("a", getNMSClass("NBTTagCompound"));

            Object nmsItem = a.invoke(nbtTagCompoundRoot);

            Method asBukkitCopy = getCraftClass("inventory.CraftItemStack").getMethod("asBukkitCopy", nmsItemClass);

            ItemStack item = (ItemStack) asBukkitCopy.invoke(null, nmsItem);

            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getNbtString(@NotNull ItemStack item) {
        try {
            Object nmsCopy = getNMSCopy(item);
            Method getOrCreateTag = nmsCopy.getClass().getMethod("getOrCreateTag");
            Object tag = getOrCreateTag.invoke(nmsCopy);
            Method asString = tag.getClass().getMethod("asString");
            return (String) asString.invoke(tag);
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

            Method isDamaged = nmsStack.getClass().getMethod("isDamaged", int.class, Random.class, getNMSClass("EntityPlayer"));

            isDamaged.invoke(nmsStack, amount, Rnd.rnd, nmsPlayer);

            return toBukkitCopy(nmsStack);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Multimap<Object, Object> getAttributes(@NotNull ItemStack itemStack) {
        try {
            Multimap<Object, Object> attMap = null;
            Object nmsItem = getNMSCopy(itemStack);
            Method getItem = nmsItem.getClass().getMethod("getItem");
            Object item = getItem.invoke(nmsItem);


            Class<Enum> enumItemSlotClass = (Class<Enum>) getNMSClass("EnumItemSlot");
//            Class attributeModClass = getNMSClass("AttributeModifier");
            Class itemArmorClass = getNMSClass("ItemArmor");
            Class itemToolClass = getNMSClass("ItemTool");
            Class itemSwordClass = getNMSClass("ItemSword");
            Class itemTridentClass = getNMSClass("ItemTrident");


            if (itemArmorClass.isInstance(item)) {
                Object tool = itemArmorClass.cast(item);
                Method b = itemArmorClass.getMethod("b");
                Object bObj = b.invoke(tool);
                Method a = itemArmorClass.getMethod("a", enumItemSlotClass);

                attMap = (Multimap<Object, Object>) a.invoke(tool, bObj);
            } else if (itemToolClass.isInstance(item)) {
                Object tool = itemToolClass.cast(item);
                Method a = itemToolClass.getMethod("a", enumItemSlotClass);
                attMap = (Multimap<Object, Object>) a.invoke(tool, Enum.valueOf(enumItemSlotClass, "MAINHAND"));
            } else if (itemSwordClass.isInstance(item)) {
                Object tool = itemSwordClass.cast(item);
                Method a = itemSwordClass.getMethod("a", enumItemSlotClass);
                attMap = (Multimap<Object, Object>) a.invoke(tool, Enum.valueOf(enumItemSlotClass, "MAINHAND"));
            } else if (itemTridentClass.isInstance(item)) {
                Object tool = itemTridentClass.cast(item);
                Method a = itemTridentClass.getMethod("a", enumItemSlotClass);
                attMap = (Multimap<Object, Object>) a.invoke(tool, Enum.valueOf(enumItemSlotClass, "MAINHAND"));
            }

            return attMap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static double getAttributeValue(@NotNull ItemStack item, @NotNull Object attackDamage) {
        try {
            Class attributeModifierClass = getNMSClass("AttributeModifier");
            if (attackDamage.getClass().getSimpleName().equals("IAttribute")) {
                Class iAttributeClass = getNMSClass("IAttribute");
                Multimap<Object, Object> attMap = getAttributes(item);
                if (attMap == null) return 0D;
                Object atkDmg = iAttributeClass.cast(attackDamage);
                Method getName = iAttributeClass.getMethod("getName");

                //Collection<AttributeModifier>
                Collection<Object> att = attMap.get(getName.invoke(atkDmg));
                Object mod = attributeModifierClass.cast((att == null || att.isEmpty()) ? 0 : att.stream().findFirst().get());

                Method getAmount = attributeModifierClass.getMethod("getAmount");
                double damage = (double) getAmount.invoke(mod);

                return damage;// + 1;
            } else if (attackDamage.getClass().getSimpleName().equals("AttributeBase")) {
                Class attributeBaseClass = getNMSClass("AttributeBase");
                Multimap<Object, Object> attMap = getAttributes(item);
                if (attMap == null) return 0D;

                Collection<Object> att = attMap.get(attributeBaseClass.cast(attackDamage));
                Object mod = attributeModifierClass.cast((att == null || att.isEmpty()) ? 0 : att.stream().findFirst().get());

                Method getAmount = attributeModifierClass.getMethod("getAmount");
                double damage = (double) getAmount.invoke(mod);

                return damage;// + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static double getDefaultDamage(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("ATTACK_DAMAGE"));
    }

    public static double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("ATTACK_SPEED"));
    }

    public static double getDefaultArmor(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("ARMOR"));
    }

    public static double getDefaultToughness(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("ARMOR_TOUGHNESS"));
    }

    public static Object getGenericAttribute(String field) {
        try {
            Class attributes = getNMSClass("GenericAttributes");
            Field f = attributes.getField(field);
            Object value = f.get(null);

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

            Method getItem = nmsItem.getClass().getMethod("getItem");

            Object item = getItem.invoke(nmsItem);

            Class swordClass = getNMSClass("ItemSword");
            Class axeClass = getNMSClass("ItemAxe");
            Class tridentClass = getNMSClass("ItemTrident");

            return swordClass.isInstance(item) || axeClass.isInstance(item) || tridentClass.isInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isTool(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);

            Method getItem = nmsItem.getClass().getMethod("getItem");

            Object item = getItem.invoke(nmsItem);

            Class toolClass = getNMSClass("ItemTool");

            return toolClass.isInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isArmor(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);

            Method getItem = nmsItem.getClass().getMethod("getItem");

            Object item = getItem.invoke(nmsItem);

            Class armorClass = getNMSClass("ItemArmor");

            return armorClass.isInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String fixColors(@NotNull String str) {
//        if (VERSION.startsWith("v1_16") || VERSION.startsWith("v1_17")) {
        try {
            str = str.replace("\n", "%n%"); // CraftChatMessage wipes all lines out.

            Class baseComponentClass = getNMSClass("IChatBaseComponent");
            Class chatMessageClass = getCraftClass("util.CraftChatMessage");

            Method fromComponent = chatMessageClass.getMethod("fromComponent", baseComponentClass);
            Method fromStringOrNull = chatMessageClass.getMethod("fromStringOrNull", String.class);

            Object baseComponent = fromStringOrNull.invoke(null, str);
            String singleColor = (String) fromComponent.invoke(null, baseComponentClass.cast(baseComponent));
            return singleColor.replace("%n%", "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return str;
//        } else {
//            return str;
//        }
    }

}
