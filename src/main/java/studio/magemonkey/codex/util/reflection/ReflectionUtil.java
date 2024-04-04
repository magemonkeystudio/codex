package studio.magemonkey.codex.util.reflection;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.core.Version;
import studio.magemonkey.codex.util.Reflex;
import studio.magemonkey.codex.util.constants.JNumbers;
import io.netty.channel.Channel;
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
        String version =
                Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
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
        UUID        uid     = UUID.randomUUID();
        GameProfile profile = new GameProfile(uid, uid.toString().substring(0, 8));
        profile.getProperties().put("textures", new Property("textures", hash));

        return profile;
    }

    default void setKiller(LivingEntity entity, Player player) {
        try {
            Class<?> living =
                    ReflectionManager.MINOR_VERSION >= 17 ? Reflex.getClass("net.minecraft.world.entity.EntityLiving")
                            : Reflex.getNMSClass("EntityLiving");
            Method handle     = Reflex.getCraftClass("entity.CraftEntity").getDeclaredMethod("getHandle");
            Field  killer     = living.getDeclaredField(getKillerField());
            Field  damageTime = living.getDeclaredField(getDamageTimeField());

            killer.setAccessible(true);
            damageTime.setAccessible(true);

            Object hit    = handle.invoke(entity);
            Object source = handle.invoke(player);
            killer.set(hit, source);
            damageTime.set(hit, 100);
        } catch (Exception e) {
            CodexEngine.get().error("Could not set killer.");
            e.printStackTrace();
        }
    }

    default String getNetworkManagerFieldName() {
        return switch (Version.CURRENT) {
            case V1_16_R3 -> "networkManager";
            case V1_17_R1, V1_18_R1, V1_18_R2 -> "a";
            case V1_19_R1, V1_19_R2 -> "b";
            case V1_20_R2, V1_20_R3 -> "c";
            default -> "h";
        };
    }

    default String getChannelFieldName() {
        return switch (Version.CURRENT) {
            case V1_16_R3 -> "channel";
            case V1_17_R1, V1_18_R1 -> "k";
            case V1_20_R2, V1_20_R3 -> "n";
            default -> "m";
        };
    }

    default String getAttackCooldownMethodName() {
        return switch (Version.CURRENT) {
            case V1_16_R3, V1_17_R1 -> "getAttackCooldown";
            case V1_18_R1, V1_18_R2, V1_19_R1 -> "v";
            case V1_19_R2 -> "w";
            case V1_19_R3 -> "z";
            case V1_20_R2, V1_20_R3 -> "B";
            default -> "A";
        };
    }

    default String getKillerField() {
        return switch (Version.CURRENT) {
            case V1_16_R3 -> "killer";
            case V1_17_R1, V1_18_R1, V1_18_R2, V1_19_R1, V1_19_R2 -> "bc";
            case V1_19_R3 -> "aX";
            case V1_20_R2, V1_20_R3 -> "aY";
            default -> "aZ";
        };
    }

    default String getDamageTimeField() {
        return switch (Version.CURRENT) {
            case V1_16_R3 -> "lastDamageByPlayerTime";
            case V1_17_R1, V1_18_R1, V1_18_R2, V1_19_R1, V1_19_R2 -> "bd";
            case V1_19_R3 -> "aY";
            case V1_20_R2, V1_20_R3 -> "aZ";
            default -> "ba";
        };
    }
}
