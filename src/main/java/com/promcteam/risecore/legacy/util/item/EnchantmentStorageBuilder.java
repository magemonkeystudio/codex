package com.promcteam.risecore.legacy.util.item;

import com.promcteam.risecore.legacy.util.SerializationBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class EnchantmentStorageBuilder extends DataBuilder {
    private Map<Enchantment, Integer> enchants = new LinkedHashMap<>(3);

    public EnchantmentStorageBuilder() {
    }

    public EnchantmentStorageBuilder(final Map<String, Object> map) {
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            if ("==".equals(entry.getKey()) || "TYPE".equals(entry.getKey())) {
                continue;
            }
            this.enchants.put(Enchantment.getByName(entry.getKey()), ((Number) entry.getValue()).intValue());
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("enchants", this.enchants)
                .toString();
    }

    public Map<Enchantment, Integer> getEnchants() {
        return this.enchants;
    }

    public EnchantmentStorageBuilder enchant(final Map<Enchantment, Integer> enchants) {
        this.enchants = enchants;
        return this;
    }

    public EnchantmentStorageBuilder enchant(final Enchantment enchantment, final int power) {
        this.enchants.put(enchantment, power);
        return this;
    }

    public EnchantmentStorageBuilder enchant(final Enchantment enchantment) {
        this.enchant(enchantment, 1);
        return this;
    }

    public EnchantmentStorageBuilder unEnchant(final Enchantment enchantment) {
        this.enchants.remove(enchantment);
        return this;
    }

    public EnchantmentStorageBuilder clear() {
        this.enchants.clear();
        return this;
    }

    @Override
    public void apply(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof EnchantmentStorageMeta)) {
            return;
        }

        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemMeta;
        if (meta.hasStoredEnchants()) {
            final Iterable<Enchantment> enchs = new HashSet<>(meta.getStoredEnchants().keySet());
            for (final Enchantment ench : enchs) {
                meta.removeStoredEnchant(ench);
            }
        }
        for (final Map.Entry<Enchantment, Integer> entry : this.enchants.entrySet()) {
            meta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
        }
    }

    @Override
    public EnchantmentStorageBuilder use(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof EnchantmentStorageMeta)) {
            return null;
        }

        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemMeta;
        this.enchants = new LinkedHashMap<>(meta.getStoredEnchants());
        return this;
    }

    @Override
    public String getType() {
        return "enchantment_book";
    }

    @Override
    public Map<String, Object> serialize() {
        final SerializationBuilder b = SerializationBuilder.start(this.enchants.size() + 1).append(super.serialize());
        for (final Map.Entry<Enchantment, Integer> entry : this.enchants.entrySet()) {
            b.append(entry.getKey().getName(), entry.getValue());
        }
        return b.build();
    }

    public static EnchantmentStorageBuilder start() {
        return new EnchantmentStorageBuilder();
    }
}
