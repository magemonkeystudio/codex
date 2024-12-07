package studio.magemonkey.codex.api;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.UUID;

public interface NMS {
    /**
     * Gets the version of the NMS implementation
     *
     * @return the version of the NMS implementation
     */
    String getVersion();

    /**
     * Gets the connection for the player. This is a PlayerConnection object.
     *
     * @param player the player to get the connection for
     * @return the connection for the player
     */
    @NotNull
    Object getConnection(Player player);

    /**
     * Gets the channel for the player
     *
     * @param player the player to get the channel for
     * @return the channel for the player
     */
    @NotNull
    Channel getChannel(@NotNull Player player);

    /**
     * Sends a packet to the player
     *
     * @param player the player to send the packet to
     * @param packet the packet to send
     */
    void sendPacket(@NotNull Player player, @NotNull Object packet);

    /**
     * Plays the chest animation for the given block
     *
     * @param chest the chest block to play the animation for
     * @param open  true if the chest should be opened, false if it should be closed
     */
    void openChestAnimation(@NotNull Block chest, boolean open);

    /**
     * Simulates the player attacking
     *
     * @param player the player to simulate the attack for
     * @param i      the type of attack
     */
    void sendAttackPacket(@NotNull Player player, int i);

    /**
     * Attempts to parse/reparse the colors of a string to ensure consistent formatting
     *
     * @param str the string to fix
     * @return the fixed string
     */
    @NotNull
    String fixColors(@NotNull String str);

    /**
     * Gets the default attack damage for the given item.
     *
     * @param itemStack the item to get the default damage for
     * @return the default attack damage for the item
     */
    double getDefaultDamage(@NotNull ItemStack itemStack);

    /**
     * Gets the default attack speed of the given item.
     *
     * @param itemStack the item to get the default speed of
     * @return the default attack speed of the item
     */
    double getDefaultSpeed(@NotNull ItemStack itemStack);

    /**
     * Gets the default armor of the given item.
     *
     * @param itemStack the item to get the default armor of
     * @return the default armor of the item
     */
    double getDefaultArmor(@NotNull ItemStack itemStack);

    /**
     * Gets the default armor toughness of the given item.
     *
     * @param itemStack the item to get the default toughness of
     * @return the default armor toughness of the item
     */
    double getDefaultToughness(@NotNull ItemStack itemStack);

    /**
     * Determines if the given item is a weapon. Weapons include swords, bows, crossbows, tridents, and axes.
     *
     * @param itemStack the item to check
     * @return true if the item is a weapon, false otherwise
     */
    boolean isWeapon(@NotNull ItemStack itemStack);

    /**
     * Determines if the given item is armor. Armor includes helmets, chestplates, leggings, and boots.
     *
     * @param itemStack the item to check
     * @return true if the item is armor, false otherwise
     */
    boolean isArmor(@NotNull ItemStack itemStack);

    /**
     * Determines if the given item is a tool. Tools include pickaxes, axes, shovels, hoes, and shears.
     *
     * @param itemStack the item to check
     * @return true if the item is a tool, false otherwise
     */
    boolean isTool(@NotNull ItemStack itemStack);

    /**
     * Gets the NBT string of the given item in JSON.
     *
     * @param itemStack the item to get the NBT string of
     * @return the NBT string of the item
     */
    String toJson(@NotNull ItemStack itemStack);

    void setKiller(@NotNull LivingEntity entity, @NotNull Player killer);

    void changeSkull(@NotNull Block block, @NotNull String hash);

    default GameProfile getNonPlayerProfile(String hash) {
        UUID        uid     = UUID.randomUUID();
        GameProfile profile = new GameProfile(uid, uid.toString().substring(0, 8));
        profile.getProperties().put("textures", new Property("textures", hash));

        return profile;
    }

    default double getAttributeValue(@NotNull ItemStack item, @NotNull Attribute attribute) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(attribute);
        if (modifiers == null || modifiers.isEmpty()) return 0;

        return modifiers.stream().mapToDouble(AttributeModifier::getAmount).sum();
    }

    @NotNull
    default Attribute getAttribute(String name) {
        Attribute attr = null;
        try {
            attr = Attribute.valueOf(name);
        } catch (IllegalArgumentException ignored) {
        }
        if (attr == null) {
            // Try with GENERIC_ prefix
            try {
                attr = Attribute.valueOf("GENERIC_" + name);
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (attr == null) {
            // Try with PLAYER_ prefix
            try {
                attr = Attribute.valueOf("PLAYER_" + name);
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (attr == null) {
            // Throw an exception if the attribute is still null
            throw new IllegalArgumentException("Unknown attribute: " + name);
        }

        return attr;
    }

    Object getNMSCopy(@NotNull ItemStack itemStack);

    @NotNull
    default Field getField(@NotNull Class<?> clazz, @NotNull String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            }
            return getField(superClass, fieldName);
        }
    }
}
