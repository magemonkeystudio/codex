package studio.magemonkey.codex.nms;

import io.netty.channel.Channel;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.util.reflection.ReflectionManager;
import studio.magemonkey.codex.util.reflection.ReflectionUtil;

public class NMS {
    private final ReflectionUtil reflectionUtil = ReflectionManager.getReflectionUtil();

    @NotNull
    public String toJSON(@NotNull ItemStack item) {
        return reflectionUtil.toJson(item);
    }

    @Nullable
    public String toBase64(@NotNull ItemStack item) {
        return reflectionUtil.toBase64(item);
    }

    @Nullable
    public ItemStack fromBase64(@NotNull String data) {
        try {
            return reflectionUtil.fromBase64(data);
        } catch (Exception e) {
            CodexEngine.get().getLogger().warning("Error parsing item from data!");
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    public String getNbtString(@NotNull ItemStack item) {
        return reflectionUtil.getNbtString(item);
    }

    public void openChestAnimation(@NotNull Block chest, boolean open) {
        reflectionUtil.openChestAnimation(chest, open);
    }

    public void sendAttackPacket(@NotNull Player p, int i) {
        reflectionUtil.sendAttackPacket(p, i);
    }

    @NotNull
    public Channel getChannel(@NotNull Player p) {
        return reflectionUtil.getChannel(p);
    }

    public void sendPacket(@NotNull Player p, @NotNull Object packet) {
        reflectionUtil.sendPacket(p, packet);
    }

    @NotNull
    public ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player) {
        return reflectionUtil.damageItem(item, amount, player);
    }

    @NotNull
    public String fixColors(@NotNull String str) {
        return reflectionUtil.fixColors(str);
    }

    public double getDefaultDamage(@NotNull ItemStack itemStack) {
        return reflectionUtil.getDefaultDamage(itemStack);
    }

    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return reflectionUtil.getDefaultSpeed(itemStack);
    }

    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return reflectionUtil.getDefaultArmor(itemStack);
    }

    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return reflectionUtil.getDefaultToughness(itemStack);
    }

    public boolean isWeapon(@NotNull ItemStack itemStack) {
        return reflectionUtil.isWeapon(itemStack);
    }

    public boolean isTool(@NotNull ItemStack itemStack) {
        return reflectionUtil.isTool(itemStack);
    }

    public boolean isArmor(@NotNull ItemStack itemStack) {
        return reflectionUtil.isArmor(itemStack);
    }
}
