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
import org.bukkit.block.Skull;
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

public class Reflection_1_17 implements ReflectionUtil {

    private static final String DAMAGE_ATTRIBUTE = Version.CURRENT.isAtLeast(Version.V1_20_R3)
        ? "c" : "f";
    private static final String SPEED_ATTRIBUTE = Version.CURRENT.isAtLeast(Version.V1_20_R3)
        ? "e" : "h";
    private static final String ARMOR_ATTRIBUTE = Version.CURRENT.isAtLeast(Version.V1_20_R3)
        ? "a" : "i";
    private static final String TOUGHNESS_ATTRIBUTE = Version.CURRENT.isAtLeast(Version.V1_20_R3)
        ? "b" : "j";

    @Override
    public Object newNBTTagCompound() {
        try {
            Class<?> nbtTagClass = getClazz("net.minecraft.nbt.NBTTagCompound");
            return nbtTagClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object newNBTTagList() {
        try {
            Class<?> nbtTagClass = getClazz("net.minecraft.nbt.NBTTagList");
            return nbtTagClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ItemStack toBukkitCopy(Object nmsItem) {
        try {
            Class<?> craftItem = getCraftClass("inventory.CraftItemStack");
            Method asBukkitCopy =
                    Reflex.getMethod(craftItem, "asBukkitCopy", getClazz("net.minecraft.world.item.ItemStack"));
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
                    ReflectionManager.MINOR_VERSION >= 18
                            ? "b"
                            : "save",
                    nbtCompound.getClass());

            return Reflex.invokeMethod(save, nmsItem, nbtCompound);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Class<?> getClazz(String classString) throws ClassNotFoundException {
        String   name  = classString;
        Class<?> clazz = Class.forName(name);
        return clazz;
    }

    @Override
    public Object getEntity(Object craftEntity) {
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

    @Override
    public Channel getChannel(Player p) {
        String managerFieldName = getNetworkManagerFieldName();
        String channelFieldName = getChannelFieldName();
        Object conn = null,
                manager = null,
                channel = null;
        try {
            conn = getConnection(p);
            manager = Reflex.getFieldValue(conn, managerFieldName);
            channel = Reflex.getFieldValue(manager, channelFieldName);

            return (Channel) channel;
        } catch (ClassCastException e) {
            NexEngine.get().error("Could not setup Channel for player." +
                    "\n\nConnection: " + conn.toString() +
                    "\nUsing Manager Field Name: " + managerFieldName +
                    "\nManager: " + manager.toString() +
                    "\nChannel Field name: " + channelFieldName +
                    "\nChannel: " + channel.toString() + "\n"
            );
            e.printStackTrace();
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

            String fieldName = "b";
            Object con       = Reflex.getFieldValue(nmsPlayer, fieldName); //WHY must you obfuscate
            if (!con.getClass().getSimpleName().equals("PlayerConnection") && !con.getClass()
                    .getSimpleName()
                    .equals("GeneratedInterceptor")) {
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
    public void sendPacket(Player p, Object packet) {
        Object   conn        = getConnection(p);
        Class<?> packetClass = null;
        try {
            packetClass = getClazz("net.minecraft.network.protocol.Packet");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Method sendPacket;
        if (ReflectionManager.MINOR_VERSION == 17) {
            sendPacket = Reflex.getMethod(conn.getClass(), "sendPacket", packetClass);
        } else { // if (ReflectionManager.MINOR_VERSION > 17) { // If we're newer.. we're using obfuscated methods again ;(
            sendPacket = Reflex.getMethod(conn.getClass(), "a", packetClass);
        }
        Reflex.invokeMethod(sendPacket, conn, packet);
    }

    @Override
    public void sendAttackPacket(Player p, int id) {
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

    @Override
    public void openChestAnimation(Block chest, boolean open) {
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
                Method playBlockAction = Reflex.getMethod(worldClass,
                        ReflectionManager.MINOR_VERSION == 17 ? "playBlockAction" : "a", //Curse you obfuscation
                        blockPosClass, blockClass, int.class, int.class);

                Constructor ctor = Reflex.getConstructor(blockPosClass, int.class, int.class, int.class);
                Object position =
                        Reflex.invokeConstructor(ctor, (int) lo.getX(), (int) lo.getY(), (int) lo.getZ());

                Method getType = Reflex.getMethod(world.getClass(),
                        ReflectionManager.MINOR_VERSION == 17 ? "getType" : "a_",
                        blockPosClass);
                Class<?> blockData = getClazz("net.minecraft.world.level.block.state.IBlockData");
                Object   data      = blockData.cast(Reflex.invokeMethod(getType, world, position));

                Method getBlock = Reflex.getMethod(blockData,
                        ReflectionManager.MINOR_VERSION == 17 ? "getBlock" : "b");

                //TileEntityChest tileChest = (TileEntityChest) world.getTileEntity(position);
                Reflex.invokeMethod(playBlockAction, world, position, getBlock.invoke(data), 1, open ? 1 : 0);
            } catch (Exception e) {
                NexEngine.get().getLogger().warning("Problem sending chest animation");
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

            if (ReflectionManager.MINOR_VERSION == 19) {
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
                        ReflectionManager.MINOR_VERSION == 17 ? "isDamaged" : "a",
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

    @Override
    public Multimap<Object, Object> getAttributes(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);
            Method getItem = Reflex.getMethod(nmsItem.getClass(),
                    ReflectionManager.MINOR_VERSION > 17 ? "c" : "getItem");
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
                if (Version.V1_19_R3.isCurrent()) {
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

    @Override
    public double getAttributeValue(@NotNull ItemStack item, @NotNull Object attribute) {
        try {
            Class<?> attributeModifierClass =
                    getClazz("net.minecraft.world.entity.ai.attributes.AttributeModifier");
            Class<?> attributeBaseClass =
                    getClazz("net.minecraft.world.entity.ai.attributes.AttributeBase");
            Multimap<Object, Object> attMap = getAttributes(item);
            if (attMap == null || attMap.isEmpty()) return 0D;

            Collection<Object> att = attMap.get(attributeBaseClass.cast(attribute));
            if (att == null || att.isEmpty()) return 0D;
            Object mod = attributeModifierClass.cast(att.stream().findFirst().get());

            Method getAmount = Reflex.getMethod(attributeModifierClass,
                    ReflectionManager.MINOR_VERSION == 17 ? "getAmount" :
                            (Version.CURRENT.isAtLeast(Version.V1_20_R3) ? "c" : "d"));
            double value = (double) Reflex.invokeMethod(getAmount, mod);
            if (attribute.equals(getGenericAttribute(DAMAGE_ATTRIBUTE))) { // Damage
                value += 1;
            } else if (attribute.equals(getGenericAttribute(SPEED_ATTRIBUTE))) { // Attack Speed
                value += 4;
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public double getDefaultDamage(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute(DAMAGE_ATTRIBUTE)); // generic.attack_damage
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute(SPEED_ATTRIBUTE)); // generic.attack_speed
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute(ARMOR_ATTRIBUTE)); // generic.armor
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute(TOUGHNESS_ATTRIBUTE)); // generic.armor_toughness
    }

    @Override
    public Object getGenericAttribute(String field) {
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

    @Override
    public boolean isWeapon(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);

            Method getItem = Reflex.getMethod(nmsItem.getClass(), "getItem");

            Object item = Reflex.invokeMethod(getItem, nmsItem);

            Class<?> swordClass   = getClazz("net.minecraft.world.item.ItemSword");
            Class<?> axeClass     = getClazz("net.minecraft.world.item.ItemAxe");
            Class<?> tridentClass = getClazz("net.minecraft.world.item.ItemTrident");

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

            Class<?> toolClass = getClazz("net.minecraft.world.item.ItemTool");

            return toolClass.isInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isArmor(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);

            Method getItem = Reflex.getMethod(nmsItem.getClass(), "getItem");

            Object item = Reflex.invokeMethod(getItem, nmsItem);

            Class<?> armorClass = getClazz("net.minecraft.world.item.ItemArmor");

            return armorClass.isInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String fixColors(@NotNull String str) {
        try {
            str = str.replace("\n", "%n%"); // CraftChatMessage wipes all lines out.

            Class<?> baseComponentClass = getClazz("net.minecraft.network.chat.IChatBaseComponent");
            Class<?> chatMessageClass   = getCraftClass("util.CraftChatMessage");

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

    public float getAttackCooldown(Player p) {
        try {
            Class<?> entityPlayerClass = getClazz("net.minecraft.server.level.EntityPlayer");
            Class    entityHumanClass  = getClazz("net.minecraft.world.entity.player.EntityHuman");
            Object   craftPlayer       = getCraftPlayer(p);
            Method   getHandle         = Reflex.getMethod(craftPlayer.getClass(), "getHandle");

            Object ep = entityPlayerClass.cast(Reflex.invokeMethod(getHandle, craftPlayer));

            Method getAttackCooldown = Reflex.getMethod(entityHumanClass, getAttackCooldownMethodName(), float.class);
            if (getAttackCooldown == null) {
                throw new NullPointerException("Could not find a \"getAttackCooldown\" method using Reflection. " +
                        "Attempting " + getAttackCooldownMethodName() + "(float)");
            }

            return (float) Reflex.invokeMethod(getAttackCooldown, entityHumanClass.cast(ep), 0f);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void changeSkull(Block b, String hash) {
        try {
            if (!(b.getState() instanceof Skull)) return;

            Skull       skull   = (Skull) b.getState();
            GameProfile profile = getNonPlayerProfile(hash);
            Reflex.setFieldValue(skull, "profile", profile);
            skull.update();
        } catch (Exception e) {
            NexEngine.get().getLogger().warning("Could not update skull");
            e.printStackTrace();
        }
    }
}
