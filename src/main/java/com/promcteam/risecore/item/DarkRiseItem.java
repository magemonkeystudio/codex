package com.promcteam.risecore.item;

import com.promcteam.risecore.legacy.cmds.DelayedCommand;
import org.apache.commons.lang3.DoubleRange;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public interface DarkRiseItem extends ConfigurationSerializable {
    String getId();

    String getName();

    boolean isDropOnDeath();

    int isRemoveOnDeath();

    boolean isConfirmOnUse();

    int isRemoveOnUse();

    boolean canDrop();

    boolean isTradeable();

    boolean isEnabledEnchantedDurability();

//    boolean isTwoHand();

    DoubleRange chanceToLostDurability();

    List<DelayedCommand> getCommands();

    List<String> getPermission();

    String getPermissionMessage();

    ItemStack getItem(int paramInt);

    default ItemStack getItem() {
        return getItem(1);
    }

    default boolean isSimilar(DarkRiseItem item) {
        if (item == null)
            return false;
        return (item == this || getId().equals(item.getId()));
    }

    default boolean isSimilar(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName())
            return false;
        return (item == this || getName().equals(meta.getDisplayName()));
    }

    boolean isVanilla();

    void invoke(CommandSender paramCommandSender);
}
