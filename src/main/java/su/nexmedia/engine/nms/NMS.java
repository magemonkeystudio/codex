package su.nexmedia.engine.nms;

import io.netty.channel.Channel;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.reflection.ReflectionUtil;

public interface NMS {

    @NotNull
    String toJSON(@NotNull ItemStack item);

    @Nullable
    String toBase64(@NotNull ItemStack item);

    @Nullable
    ItemStack fromBase64(@NotNull String data);

    @NotNull
    String getNbtString(@NotNull ItemStack item);

    void openChestAnimation(@NotNull Block chest, boolean open);

    void sendAttackPacket(@NotNull Player p, int i);

    @NotNull
    default Channel getChannel(@NotNull Player p) {
        return ReflectionUtil.getChannel(p);
    }

    default void sendPacket(@NotNull Player p, @NotNull Object packet) {
        ReflectionUtil.sendPacket(p, packet);
    }

    @NotNull
    ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player);

    @NotNull
    String fixColors(@NotNull String str);

    double getDefaultDamage(@NotNull ItemStack itemStack);

    double getDefaultSpeed(@NotNull ItemStack itemStack);

    double getDefaultArmor(@NotNull ItemStack itemStack);

    double getDefaultToughness(@NotNull ItemStack itemStack);

    boolean isWeapon(@NotNull ItemStack itemStack);

    boolean isTool(@NotNull ItemStack itemStack);

    boolean isArmor(@NotNull ItemStack itemStack);
}
