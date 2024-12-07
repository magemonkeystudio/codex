package studio.magemonkey.codex.nms.v1_21_2;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.channel.Channel;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.TileEntitySkull;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_21_R2.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R2.util.CraftChatMessage;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.Codex;
import studio.magemonkey.codex.api.NMS;
import studio.magemonkey.codex.util.constants.JNumbers;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

public class NMSImpl implements NMS {
    @Override
    public String getVersion() {
        return "1.21.2";
    }

    @NotNull
    @Override
    public Object getConnection(Player player) {
        return ((CraftPlayer) player).getHandle().f;
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
        return getAttributeValue(itemStack, Attribute.ATTACK_DAMAGE);
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, Attribute.ATTACK_SPEED);
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, Attribute.ARMOR);
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, Attribute.ARMOR_TOUGHNESS);
    }

    @Override
    public boolean isWeapon(@NotNull ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        Item                               item    = nmsItem.h();
        return item instanceof ItemSword || item instanceof ItemAxe || item instanceof ItemTrident;
    }

    @Override
    public boolean isArmor(@NotNull ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        Item                               item    = nmsItem.h();
        return item instanceof ItemArmor;
    }

    @Override
    public boolean isTool(@NotNull ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        Item                               item    = nmsItem.h();
        return item instanceof ItemTool || item instanceof ItemShears;
    }

    @Override
    public String toJson(@NotNull ItemStack item) {
        try {
            NBTTagCompound                     nbtCompound = new NBTTagCompound();
            net.minecraft.world.item.ItemStack nmsItem     = CraftItemStack.asNMSCopy(item);

            nmsItem.b(CraftRegistry.getMinecraftRegistry(), nbtCompound);

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
            Field damageTime = hit.getClass().getField("bd");

            damageTime.setAccessible(true);

            damageTime.set(hit, 100);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Unable to set killer. Something went wrong", e);
        }
    }

    @Override
    public void changeSkull(@NotNull Block block, @NotNull String hash) {
        try {
            if (!(block.getState() instanceof Skull)) return;

            try {
                Skull          skull    = (Skull) block.getState();
                PlayerProfile  profile  = Bukkit.createPlayerProfile(UUID.randomUUID(), hash.substring(0, 16));
                PlayerTextures textures = profile.getTextures();

                // If the hash is a URL, we can just set the skin directly, otherwise, we need to decode
                // the hash from Base64 and extract the url from the JSON data.
                if (hash.startsWith("http")) {
                    textures.setSkin(new URL(hash));
                } else {
                    String decoded = new String(java.util.Base64.getDecoder().decode(hash));
                    // Construct the json object
                    JsonObject json = new Gson().fromJson(decoded, JsonObject.class);
                    // Get the textures object
                    JsonObject texturesJson = json.getAsJsonObject("textures");
                    // Get the skin object
                    JsonObject skin = texturesJson.getAsJsonObject("SKIN");
                    // Get the url
                    String url = skin.get("url").getAsString();
                    textures.setSkin(new URL(url));
                }

                profile = profile.update().get();
                skull.setOwnerProfile(profile);
                skull.update();
            } catch (NoSuchMethodError | Exception e) {
                Codex.info("Could not change skull with modern method, trying legacy method.");
                if (!(block.getState() instanceof Skull)) return;

                TileEntitySkull skull = (TileEntitySkull) ((CraftWorld) block.getWorld()).getHandle().c_(new BlockPosition(block.getX(), block.getY(), block.getZ()));
                if (skull == null) return;

                ResolvableProfile profile = new ResolvableProfile(getNonPlayerProfile(hash));
                skull.a(profile);
                skull.e();
            }
        } catch (Exception e) {
            Codex.warn("Could not update skull");
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public Attribute getAttribute(String name) {
        return Registry.ATTRIBUTE.getOrThrow(NamespacedKey.minecraft(name.toLowerCase(Locale.US)));
    }

    @Override
    public Object getNMSCopy(@NotNull ItemStack item) {
        return CraftItemStack.asNMSCopy(item);
    }
}
