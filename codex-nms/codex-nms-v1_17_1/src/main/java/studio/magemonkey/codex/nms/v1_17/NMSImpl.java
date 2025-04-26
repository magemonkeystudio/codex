package studio.magemonkey.codex.nms.v1_17;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.TileEntitySkull;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.entity.Boat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.compat.NMS;
import studio.magemonkey.codex.util.constants.JNumbers;

import java.lang.reflect.Field;

public class NMSImpl implements NMS {
    @Override
    public String getVersion() {
        return "1.17.1";
    }

    @NotNull
    @Override
    public Object getConnection(Player player) {
        return ((CraftPlayer) player).getHandle().b;
    }

    @NotNull
    @Override
    public Channel getChannel(@NotNull Player player) {
        return ((PlayerConnection) getConnection(player)).a().k;
    }

    @Override
    public void sendPacket(@NotNull Player player, @NotNull Object packet) {
        Preconditions.checkArgument(packet instanceof Packet,
                "Packet must be an instance of net.minecraft.server.Packet");
        ((PlayerConnection) getConnection(player)).sendPacket((Packet<?>) packet);
    }

    @Override
    public void openChestAnimation(@NotNull Block chest, boolean open) {
        WorldServer   world     = ((CraftWorld) chest.getWorld()).getHandle();
        BlockPosition position  = new BlockPosition(chest.getX(), chest.getY(), chest.getZ());
        IBlockData    blockData = world.getType(position);
        world.playBlockAction(position, blockData.getBlock(), 1, open ? 1 : 0);
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
        return getAttributeValue(itemStack, Attribute.GENERIC_ATTACK_DAMAGE);
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, Attribute.GENERIC_ATTACK_SPEED);
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, Attribute.GENERIC_ARMOR);
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, Attribute.GENERIC_ARMOR_TOUGHNESS);
    }

    @Override
    public boolean isWeapon(@NotNull ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        Item                               item    = nmsItem.getItem();
        return item instanceof ItemSword || item instanceof ItemAxe || item instanceof ItemTrident;
    }

    @Override
    public boolean isArmor(@NotNull ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        Item                               item    = nmsItem.getItem();
        return item instanceof ItemArmor;
    }

    @Override
    public boolean isTool(@NotNull ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        Item                               item    = nmsItem.getItem();
        return item instanceof ItemTool || item instanceof ItemShears;
    }

    @Override
    public String toJson(@NotNull ItemStack item) {
        try {
            NBTTagCompound                     nbtCompound = new NBTTagCompound();
            net.minecraft.world.item.ItemStack nmsItem     = CraftItemStack.asNMSCopy(item);

            nmsItem.save(nbtCompound);

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
            EntityLiving hit = ((CraftLivingEntity) entity).getHandle();
            hit.bc = ((CraftPlayer) killer).getHandle();
            Field damageTime = getField(hit.getClass(), "bd");

            damageTime.setAccessible(true);

            damageTime.set(hit, 100);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Unable to set killer. Something went wrong", e);
        }
    }

    @Override
    public void changeSkull(@NotNull Block block, @NotNull String hash) {
        if (!(block.getState() instanceof Skull)) return;

        TileEntitySkull skull = (TileEntitySkull) ((CraftWorld) block.getWorld()).getHandle()
                .getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        if (skull == null) return;

        GameProfile profile = getNonPlayerProfile(hash);
        skull.setGameProfile(profile);
        skull.update();
    }

    @Override
    public Object getNMSCopy(@NotNull ItemStack item) {
        return CraftItemStack.asNMSCopy(item);
    }

    @Override
    public Material getMaterial(Boat boat) {
        TreeSpecies woodType = boat.getWoodType();

        return switch (woodType) {
            case REDWOOD -> Material.SPRUCE_BOAT;
            case BIRCH -> Material.BIRCH_BOAT;
            case JUNGLE -> Material.JUNGLE_BOAT;
            case ACACIA -> Material.ACACIA_BOAT;
            case DARK_OAK -> Material.DARK_OAK_BOAT;
            default -> Material.OAK_BOAT;
        };
    }
}
