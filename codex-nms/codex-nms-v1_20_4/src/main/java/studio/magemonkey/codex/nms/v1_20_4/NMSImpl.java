package studio.magemonkey.codex.nms.v1_20_4;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftChatMessage;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.api.NMS;
import studio.magemonkey.codex.util.constants.JNumbers;

import java.lang.reflect.Field;
import java.util.Collection;

public class NMSImpl implements NMS {
    @Override
    public String getVersion() {
        return "1.20.4";
    }

    @NotNull
    @Override
    public Object getConnection(Player player) {
        return ((CraftPlayer) player).getHandle().c;
    }

    @NotNull
    @Override
    public Channel getChannel(@NotNull Player player) {
        PlayerConnection connection = ((PlayerConnection) getConnection(player));
        // We have to reflexively get the `channel` field
        try {
            Field networkManagerField = connection.getClass().getDeclaredField("connection");
            networkManagerField.setAccessible(true);
            NetworkManager networkManager = (NetworkManager) networkManagerField.get(connection);
            return networkManager.n;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Could not get Channel field from PlayerConnection", e);
        }
    }

    @Override
    public void sendPacket(@NotNull Player player, @NotNull Object packet) {
        Preconditions.checkArgument(packet instanceof Packet, "Packet must be an instance of net.minecraft.server.Packet");
        ((PlayerConnection) getConnection(player)).a((Packet<?>) packet);
    }

    @Override
    public void openChestAnimation(@NotNull Block chest, boolean open) {
        WorldServer   world     = ((CraftWorld) chest.getWorld()).getHandle();
        BlockPosition position  = new BlockPosition(chest.getX(), chest.getY(), chest.getZ());
        IBlockData    blockData = world.a_(position);
        world.a(position, blockData.b(), 1, open ? 1 : 0);
    }

    @Override
    public void sendAttackPacket(@NotNull Player player, int i) {
        PacketPlayOutAnimation packet = new PacketPlayOutAnimation(((CraftPlayer) player).getHandle(), i);
        sendPacket(player, packet);
    }

    @NotNull
    @Override
    public String fixColors(@NotNull String str) {
        str = str.replace("\n", "%n%");

        IChatBaseComponent baseComponent = CraftChatMessage.fromStringOrNull(str);
        String             singleColor   = CraftChatMessage.fromComponent(baseComponent);

        return singleColor.replace("%n%", "\n");
    }

    @Override
    public double getDefaultDamage(@NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return 0;

        Collection<AttributeModifier> modifiers = itemMeta.getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE);
        if (modifiers == null || modifiers.isEmpty()) return 0;

        return modifiers.stream().mapToDouble(AttributeModifier::getAmount).sum();
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return 0;

        Collection<AttributeModifier> modifiers = itemMeta.getAttributeModifiers(Attribute.GENERIC_ATTACK_SPEED);
        if (modifiers == null || modifiers.isEmpty()) return 0;

        return modifiers.stream().mapToDouble(AttributeModifier::getAmount).sum();
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return 0;

        Collection<AttributeModifier> modifiers = itemMeta.getAttributeModifiers(Attribute.GENERIC_ARMOR);
        if (modifiers == null || modifiers.isEmpty()) return 0;

        return modifiers.stream().mapToDouble(AttributeModifier::getAmount).sum();
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return 0;

        Collection<AttributeModifier> modifiers = itemMeta.getAttributeModifiers(Attribute.GENERIC_ARMOR_TOUGHNESS);
        if (modifiers == null || modifiers.isEmpty()) return 0;

        return modifiers.stream().mapToDouble(AttributeModifier::getAmount).sum();
    }

    @Override
    public boolean isWeapon(@NotNull ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        Item                               item    = nmsItem.d();
        return item instanceof ItemSword || item instanceof ItemAxe || item instanceof ItemTrident;
    }

    @Override
    public boolean isArmor(@NotNull ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        Item                               item    = nmsItem.d();
        return item instanceof ItemArmor;
    }

    @Override
    public boolean isTool(@NotNull ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        Item                               item    = nmsItem.d();
        return item instanceof ItemTool || item instanceof ItemShears;
    }

    @Override
    public String toJson(@NotNull ItemStack item) {
        try {
            NBTTagCompound                     nbtCompound = new NBTTagCompound();
            net.minecraft.world.item.ItemStack nmsItem     = CraftItemStack.asNMSCopy(item);

            nmsItem.b(nbtCompound);

            String js = nbtCompound.toString();
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

    @Override
    public void setKiller(@NotNull LivingEntity entity, @NotNull Player killer) {
        try {
            EntityLiving hit      = ((CraftLivingEntity) entity).getHandle();
            hit.aY = ((CraftPlayer) killer).getHandle();
            Field  damageTime  = hit.getClass().getField("aZ");

            damageTime.setAccessible(true);

            damageTime.set(hit, 100);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Unable to set killer. Something went wrong", e);
        }
    }
}
