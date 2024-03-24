package com.promcteam.codex.nms;

import io.netty.channel.Channel;
import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.utils.reflection.ReflectionManager;
import com.promcteam.codex.utils.reflection.ReflectionUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NMS {

    ReflectionUtil reflectionUtil = ReflectionManager.getReflectionUtil();

    @NotNull
    default String toJSON(@NotNull ItemStack item) {
        return reflectionUtil.toJson(item);
    }

    @Nullable
    default String toBase64(@NotNull ItemStack item) {
        return reflectionUtil.toBase64(item);
    }

    @Nullable
    default ItemStack fromBase64(@NotNull String data) {
        try {
            return reflectionUtil.fromBase64(data);
        } catch (Exception e) {
            CodexEngine.get().getLogger().warning("Error parsing item from data!");
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    default String getNbtString(@NotNull ItemStack item) {
        return reflectionUtil.getNbtString(item);
    }

    default void openChestAnimation(@NotNull Block chest, boolean open) {
        reflectionUtil.openChestAnimation(chest, open);
    }

    default void sendAttackPacket(@NotNull Player p, int i) {
        reflectionUtil.sendAttackPacket(p, i);
    }

    @NotNull
    default Channel getChannel(@NotNull Player p) {
        return reflectionUtil.getChannel(p);
    }

    default void sendPacket(@NotNull Player p, @NotNull Object packet) {
        reflectionUtil.sendPacket(p, packet);
    }

    @NotNull
    default ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player) {
        return reflectionUtil.damageItem(item, amount, player);
    }

    @NotNull
    default String fixColors(@NotNull String str) {
        return reflectionUtil.fixColors(str);
    }

    default double getDefaultDamage(@NotNull ItemStack itemStack) {
        return reflectionUtil.getDefaultDamage(itemStack);
    }

    default double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return reflectionUtil.getDefaultSpeed(itemStack);
    }

    default double getDefaultArmor(@NotNull ItemStack itemStack) {
        return reflectionUtil.getDefaultArmor(itemStack);
    }

    default double getDefaultToughness(@NotNull ItemStack itemStack) {
        return reflectionUtil.getDefaultToughness(itemStack);
    }

    default boolean isWeapon(@NotNull ItemStack itemStack) {
        return reflectionUtil.isWeapon(itemStack);
    }

    default boolean isTool(@NotNull ItemStack itemStack) {
        return reflectionUtil.isTool(itemStack);
    }

    default boolean isArmor(@NotNull ItemStack itemStack) {
        return reflectionUtil.isArmor(itemStack);
    }
}
