package com.promcteam.codex.testutil.reflection;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.promcteam.codex.utils.reflection.ReflectionUtil;
import io.netty.channel.Channel;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public class TestReflectionUtil implements ReflectionUtil {
    @Override
    public Object newNBTTagCompound() {
        return null;
    }

    @Override
    public Object newNBTTagList() {
        return null;
    }

    @Override
    public Object getNMSCopy(ItemStack item) {
        return null;
    }

    @Override
    public ItemStack toBukkitCopy(Object nmsItem) {
        return null;
    }

    @Override
    public Object save(Object nmsItem, Object nbtCompound) {
        return null;
    }

    @Override
    public Class<?> getNMSClass(String nmsClassName) throws ClassNotFoundException {
        return null;
    }

    @Override
    public Class<?> getCraftClass(String craftClassName) throws ClassNotFoundException {
        return null;
    }

    @Override
    public Object getConnection(Player player) {
        return null;
    }

    @Override
    public Object getCraftPlayer(Player player) {
        return null;
    }

    @Override
    public Object getEntity(Object craftEntity) {
        return null;
    }

    @Override
    public Channel getChannel(Player player) {
        return new TestChannel();
    }

    @Override
    public void sendPackets(Player player, Collection<Object> packets) {

    }

    @Override
    public void sendPacket(Player player, Object packet) {

    }

    @Override
    public void sendAttackPacket(Player player, int id) {

    }

    @Override
    public void openChestAnimation(Block chest, boolean open) {

    }

    @Override
    public String toJson(@NotNull ItemStack item) {
        return "json-string";
    }

    @Override
    public String toBase64(@NotNull ItemStack item) {
        return "b64-string";
    }

    @Override
    public ItemStack fromBase64(@NotNull String data) {
        return new ItemStack(Material.PUMPKIN);
    }

    @Override
    public String getNbtString(@NotNull ItemStack item) {
        return "nbt-string";
    }

    @Override
    public ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player) {
        // TODO Actually damage the item
        return item;
    }

    @Override
    public Multimap<Object, Object> getAttributes(@NotNull ItemStack item) {
        return null;
    }

    @Override
    public double getAttributeValue(@NotNull ItemStack item, @NotNull Object attribute) {
        return 0;
    }

    @Override
    public double getDefaultDamage(@NotNull ItemStack item) {
        return 0;
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack item) {
        return 0;
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack item) {
        return 0;
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack item) {
        return 0;
    }

    @Override
    public Object getGenericAttribute(String field) {
        return null;
    }

    @Override
    public boolean isWeapon(@NotNull ItemStack item) {
        return false;
    }

    @Override
    public boolean isTool(@NotNull ItemStack item) {
        return false;
    }

    @Override
    public boolean isArmor(@NotNull ItemStack item) {
        return false;
    }

    @Override
    public String fixColors(@NotNull String str) {
        return str;
    }

    @Override
    public float getAttackCooldown(Player player) {
        return 0;
    }

    @Override
    public void changeSkull(Block b, String hash) {

    }

    @Override
    public GameProfile getNonPlayerProfile(String hash) {
        UUID        uid     = UUID.randomUUID();
        GameProfile profile = new GameProfile(uid, uid.toString());
        return profile;
    }

    @Override
    public void setKiller(LivingEntity entity, Player player) {
    }
}
