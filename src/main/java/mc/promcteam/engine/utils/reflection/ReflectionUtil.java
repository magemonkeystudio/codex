package mc.promcteam.engine.utils.reflection;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import mc.promcteam.engine.NexEngine;
import mc.promcteam.engine.utils.Reflex;
import mc.promcteam.engine.utils.constants.JNumbers;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;

public interface ReflectionUtil {

    Object newNBTTagCompound();

    Object newNBTTagList();

    default Object getNMSCopy(ItemStack item) {
        try {
            Class<?> craftItemClass = getCraftClass("inventory.CraftItemStack");
            Method   asNMSCopy      = Reflex.getMethod(craftItemClass, "asNMSCopy", ItemStack.class);

            return Reflex.invokeMethod(asNMSCopy, null, item);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    ItemStack toBukkitCopy(Object nmsItem);

    Object save(Object nmsItem, Object nbtCompound);

    default Class<?> getNMSClass(String nmsClassName) throws ClassNotFoundException {
        String   version  = ReflectionManager.VERSION + ".";
        String   name     = "net.minecraft.server." + version + nmsClassName;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    default Class<?> getCraftClass(String craftClassName) throws ClassNotFoundException {
        String   version    = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String   name       = "org.bukkit.craftbukkit." + version + craftClassName;
        Class<?> craftClass = Class.forName(name);
        return craftClass;
    }

    Object getConnection(Player player);

    default Object getCraftPlayer(Player player) {
        try {
            Class<?> craftClass = getCraftClass("entity.CraftPlayer");
            return craftClass.cast(player);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    Object getEntity(Object craftEntity);

    Channel getChannel(Player player);

    default void sendPackets(Player player, Collection<Object> packets) {
        for (Object packet : packets) {
            sendPacket(player, packet);
        }
    }

    void sendPacket(Player player, Object packet);

    void sendAttackPacket(Player player, int id);

    void openChestAnimation(Block chest, boolean open);

    default String toJson(@NotNull ItemStack item) {
        try {
            Object nbtCompound = newNBTTagCompound();
            Object nmsItem     = getNMSCopy(item);

            nbtCompound = save(nmsItem, nbtCompound);

            Method toString = Reflex.getMethod(nbtCompound.getClass(), "toString");

            String js = (String) Reflex.invokeMethod(toString, nbtCompound);
            if (js.length() > JNumbers.JSON_MAX) {
                ItemStack item2 = new ItemStack(item.getType());
                return toJson(item2);
            }

            return js;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    String toBase64(@NotNull ItemStack item);

    ItemStack fromBase64(@NotNull String data);

    default String getNbtString(@NotNull ItemStack item) {
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

    ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player);

    Multimap<Object, Object> getAttributes(@NotNull ItemStack item);

    double getAttributeValue(@NotNull ItemStack item, @NotNull Object attribute);

    double getDefaultDamage(@NotNull ItemStack item);

    double getDefaultSpeed(@NotNull ItemStack item);

    double getDefaultArmor(@NotNull ItemStack item);

    double getDefaultToughness(@NotNull ItemStack item);

    Object getGenericAttribute(String field);

    boolean isWeapon(@NotNull ItemStack item);

    boolean isTool(@NotNull ItemStack item);

    boolean isArmor(@NotNull ItemStack item);

    String fixColors(@NotNull String str);

    float getAttackCooldown(Player player);

    void changeSkull(Block b, String hash);

    default GameProfile getNonPlayerProfile(String hash) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", hash));

        return profile;
    }

    default void setKiller(LivingEntity entity, Player player) {
        try {
            Class<?> living = ReflectionManager.MINOR_VERSION >= 17 ? Reflex.getClass("net.minecraft.world.entity.EntityLiving")
                    : Reflex.getNMSClass("EntityLiving");
            Method handle = Reflex.getCraftClass("entity.CraftEntity").getDeclaredMethod("getHandle");
            Field  killer;
            Field  damageTime;
            if (ReflectionManager.MINOR_VERSION >= 17)
                killer = living.getDeclaredField("bc");
            else
                killer = living.getDeclaredField("killer");

            if (ReflectionManager.MINOR_VERSION >= 17)
                damageTime = living.getDeclaredField("bd");
            else
                damageTime = living.getDeclaredField("lastDamageByPlayerTime");

            killer.setAccessible(true);
            damageTime.setAccessible(true);

            Object hit    = handle.invoke(entity);
            Object source = handle.invoke(player);
            killer.set(hit, source);
            damageTime.set(hit, 100);
        } catch (Exception e) {
            NexEngine.get().error("Could not set killer.");
            e.printStackTrace();
        }
    }

}
