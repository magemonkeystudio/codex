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

public class ReflectionUtil {

    private static Object newNBTTagCompound() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class nbtTagClass = getNMSClass("NBTTagCompound");
        return nbtTagClass.getConstructor().newInstance();
    }

    private static Object newNBTTagList() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class nbtTagClass = getNMSClass("NBTTagList");
        return nbtTagClass.getConstructor().newInstance();
    }

    private static Object getNMSCopy(ItemStack item) throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        Class craftItemClass = getCraftClass("inventory.CraftItemStack");
        Method asNMSCopy = craftItemClass.getMethod("asNMSCopy", ItemStack.class);

        return asNMSCopy.invoke(null, item);
    }

    private static Object save(Object nmsItem, Object nbtCompound) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method save = nmsItem.getClass().getMethod("save", nbtCompound.getClass());

        return save.invoke(nmsItem, nbtCompound);
    }

    public static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
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

    public static Object getConnection(Player player) throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method getHandle = player.getClass().getMethod("getHandle");
        Object nmsPlayer = getHandle.invoke(player);
        Field conField = nmsPlayer.getClass().getField("playerConnection");
        Object con = conField.get(nmsPlayer);
        return con;
    }

    public static Object getCraftPlayer(Player p) throws ClassNotFoundException {
        Class craftClass = getCraftClass("entity.CraftPlayer");
        return craftClass.cast(p);
    }

    public static Object getEntity(Object craftPlayer) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class craftClass = getNMSClass("Entity");

        Method getHandle = craftPlayer.getClass().getMethod("getHandle");

        return craftClass.cast(getHandle.invoke(craftPlayer));
    }

    public static Channel getChannel(Player p) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object conn = getConnection(p);
        Field networkManager = conn.getClass().getField("networkManager");
        Object manager = networkManager.get(conn);
        Object channel = manager.getClass().getField("channel").get(manager);

        return (Channel) channel;
    }

    public static void sendPacket(Player p, Object packet) {
        try {
            Object conn = getConnection(p);
            Class<?> packetClass = getNMSClass("Packet");
            Method sendMethod = conn.getClass().getMethod("sendPacket", packetClass);
            sendMethod.invoke(conn, packet);
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
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

    public static void openChestAnimation(Block chest, boolean open) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        if (chest.getState() instanceof Chest) {
            Location lo = chest.getLocation();
            World bWorld = lo.getWorld();
            if (bWorld == null) return;

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
        }
    }

    public static String toJSON(@NotNull ItemStack item) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
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
    }

    public static String toBase64(@NotNull ItemStack item) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
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
    }

    public ItemStack fromBase64(@NotNull String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());

        NBTTagCompound nbtTagCompoundRoot;
        try {
            nbtTagCompoundRoot = NBTCompressedStreamTools.a(new DataInputStream(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        net.minecraft.server.v1_14_R1.ItemStack nmsItem = net.minecraft.server.v1_14_R1.ItemStack.a(nbtTagCompoundRoot);  //.createStack(nbtTagCompoundRoot);
        ItemStack item = (ItemStack) CraftItemStack.asBukkitCopy(nmsItem);

        return item;
    }

    public String getNbtString(@NotNull ItemStack item) {
        return CraftItemStack.asNMSCopy(item).getOrCreateTag().asString();
    }

    public ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player) {
        //CraftItemStack craftItem = (CraftItemStack) item;
        net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        EntityPlayer nmsPlayer = player != null ? ((CraftPlayer) player).getHandle() : null;
        nmsStack.isDamaged(amount, Rnd.rnd, nmsPlayer);

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public String fixColors(@NotNull String str) {
        return str;
    }

    private Multimap<String, AttributeModifier> getAttributes(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        Multimap<String, AttributeModifier> attMap = null;

        if (item instanceof ItemArmor) {
            ItemArmor tool = (ItemArmor) item;
            attMap = tool.a(tool.b());
        } else if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool) item;
            attMap = tool.a(EnumItemSlot.MAINHAND);
        } else if (item instanceof ItemSword) {
            ItemSword tool = (ItemSword) item;
            attMap = tool.a(EnumItemSlot.MAINHAND);
        } else if (item instanceof ItemTrident) {
            ItemTrident tool = (ItemTrident) item;
            attMap = tool.a(EnumItemSlot.MAINHAND);
        }

        return attMap;
    }

    private double getAttributeValue(@NotNull ItemStack item, @NotNull IAttribute attackDamage) {
        Multimap<String, AttributeModifier> attMap = this.getAttributes(item);
        if (attMap == null) return 0D;

        Collection<AttributeModifier> att = attMap.get(attackDamage.getName());
        double damage = (att == null || att.isEmpty()) ? 0 : att.stream().findFirst().get().getAmount();

        return damage;// + 1;
    }

    public boolean isWeapon(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ItemSword || item instanceof ItemAxe || item instanceof ItemTrident;
    }

    public boolean isTool(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ItemTool;
    }

    public boolean isArmor(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ItemArmor;
    }

    public double getDefaultDamage(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.ATTACK_DAMAGE);
    }

    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.ATTACK_SPEED);
    }

    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.ARMOR);
    }

    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.ARMOR_TOUGHNESS);
    }

}
