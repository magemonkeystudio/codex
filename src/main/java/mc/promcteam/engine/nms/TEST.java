package mc.promcteam.engine.nms;

import com.google.common.collect.Multimap;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Simply a blank implementation of the NMS interface. For testing.
 */
public class TEST implements NMS {
    @Override
    public ItemStack fromBase64(@NotNull String data) {
        return null;
    }

    @Override
    @NotNull
    public String getNbtString(@NotNull ItemStack item) {
        return null;
    }

    @Override
    @NotNull
    public String fixColors(@NotNull String str) {
        return str;
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return 0;
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return 0;
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return 0;
    }

    @Override
    public boolean isWeapon(@NotNull ItemStack itemStack) {
        return false;
    }
}
