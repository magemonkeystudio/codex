package mc.promcteam.engine.nms;

import io.netty.channel.Channel;
import mc.promcteam.engine.NexEngine;
import mc.promcteam.engine.utils.reflection.ReflectionUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NMS {

    @NotNull
    default String toJSON(@NotNull ItemStack item) {
        return ReflectionUtil.toJSON(item);
    }

    @Nullable
    default String toBase64(@NotNull ItemStack item) {
        return ReflectionUtil.toBase64(item);
    }

    @Nullable
    default ItemStack fromBase64(@NotNull String data) {
        try {
            return ReflectionUtil.fromBase64(data);
        } catch (Exception e) {
            NexEngine.get().getLogger().warning("Error parsing item from data!");
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    default String getNbtString(@NotNull ItemStack item) {
        return ReflectionUtil.getNbtString(item);
    }

    default void openChestAnimation(@NotNull Block chest, boolean open) {
        ReflectionUtil.openChestAnimation(chest, open);
    }

    default void sendAttackPacket(@NotNull Player p, int i) {
        ReflectionUtil.sendAttackPacket(p, i);
    }

    @NotNull
    default Channel getChannel(@NotNull Player p) {
        return ReflectionUtil.getChannel(p);
    }

    default void sendPacket(@NotNull Player p, @NotNull Object packet) {
        ReflectionUtil.sendPacket(p, packet);
    }

    @NotNull
    default ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player) {
        return ReflectionUtil.damageItem(item, amount, player);
    }

    @NotNull
    default String fixColors(@NotNull String str) {
        return ReflectionUtil.fixColors(str);
    }

    default double getDefaultDamage(@NotNull ItemStack itemStack) {
        return ReflectionUtil.getAttributeValue(itemStack, ReflectionUtil.getGenericAttribute("ATTACK_DAMAGE"));
    }

    default double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return ReflectionUtil.getAttributeValue(itemStack, ReflectionUtil.getGenericAttribute("ATTACK_SPEED"));
    }

    default double getDefaultArmor(@NotNull ItemStack itemStack) {
        return ReflectionUtil.getAttributeValue(itemStack, ReflectionUtil.getGenericAttribute("ARMOR"));
    }

    default double getDefaultToughness(@NotNull ItemStack itemStack) {
        return ReflectionUtil.getAttributeValue(itemStack, ReflectionUtil.getGenericAttribute("ARMOR_TOUGHNESS"));
    }

    default boolean isWeapon(@NotNull ItemStack itemStack) {
        return ReflectionUtil.isWeapon(itemStack);
    }

    default boolean isTool(@NotNull ItemStack itemStack) {
        return ReflectionUtil.isTool(itemStack);
    }

    default boolean isArmor(@NotNull ItemStack itemStack) {
        return ReflectionUtil.isArmor(itemStack);
    }
}
