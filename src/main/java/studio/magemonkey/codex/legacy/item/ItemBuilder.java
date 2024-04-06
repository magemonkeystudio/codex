package studio.magemonkey.codex.legacy.item;

import studio.magemonkey.codex.util.ItemUtils;
import studio.magemonkey.codex.util.SerializationBuilder;
import studio.magemonkey.risecore.legacy.util.DeserializationWorker;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
@SerializableAs("Codex_Item")
public class ItemBuilder implements ConfigurationSerializable {
    @Getter
    protected Material material   = Material.AIR;
    @Getter
    protected int     amount      = 1;
    @Getter
    protected short   durability  = 0;
    @Getter
    protected boolean      unbreakable = false;
    @Getter
    protected String       name;
    @Getter
    protected List<String>              lore        = new ArrayList<>(5);
    @Getter
    protected Map<Enchantment, Integer> enchants    = new LinkedHashMap<>(3);
    @Getter
    protected DataBuilder               dataBuilder = null;
    @Getter
    protected List<ItemFlag>            flags       = new ArrayList<>(5);
    protected int            modelData   = -1;

    /**
     * @deprecated Items should be store using ItemMeta and loaded the same way. It is less likely to break that way.
     */
    public ItemBuilder(final Map<String, Object> map) {
        final DeserializationWorker w = DeserializationWorker.start(map);
        Bukkit.getServer().getConsoleSender().sendMessage("Building item: " + w.getString("name", "none"));
        this.material = ItemUtils.getMaterial(w.getString("material", "AIR"));
        this.amount = w.getInt("amount", 1);
        this.durability = w.getShort("durability");
        this.name = w.getString("name", null);
        this.unbreakable = w.getBoolean("unbreakable", false);
        this.lore = w.getStringList("lore", new ArrayList<>(3));
        this.flags = w.getStringList("flags", new ArrayList<>(1))
                .stream()
                .map(s -> ItemFlag.valueOf(s.toUpperCase()))
                .collect(Collectors.toList());
        final Map<String, Object> enchantsMap = w.getSection("enchants");
        if (enchantsMap != null) {
            for (final Map.Entry<String, Object> entry : enchantsMap.entrySet()) {
                this.enchants.put(Enchantment.getByName(entry.getKey()), ((Number) entry.getValue()).intValue());
            }
        }
        this.dataBuilder = DataBuilder.build(w.getTypedObject("data", new HashMap<>(1)));

        modelData = w.getInt("modelData", -1);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("material", this.material)
                .append("amount", this.amount)
                .append("durability", this.durability)
                .append("name", this.name)
                .append("lore", this.lore)
                .append("enchants", this.enchants)
                .append("dataBuilder", this.dataBuilder)
                .toString();
    }

    public ItemBuilder unbreakable(final boolean flag) {
        this.unbreakable = flag;
        return this;
    }

    public ItemBuilder unbreakable(final ItemMeta meta) {
        this.unbreakable = meta.isUnbreakable();
        return this;
    }

    public ItemBuilder flag(final ItemFlag flag) {
        this.flags.add(flag);
        return this;
    }

    public ItemBuilder clearFlags() {
        this.flags.clear();
        return this;
    }

    public ItemBuilder flag(final ItemFlag... flags) {
        Collections.addAll(this.flags, flags);
        return this;
    }

    public ItemBuilder flag(final ItemMeta meta) {
        this.flags.addAll(meta.getItemFlags());
        return this;
    }

    public ItemBuilder material(final Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder material(final ItemStack source) {
        this.material = source.getType();
        return this;
    }

    public ItemBuilder durability(final int damage) {
        return this.durability((short) damage);
    }

    public ItemBuilder durability(final short damage) {
        this.durability = damage;
        return this;
    }

    public ItemBuilder durability(final ItemStack source) {
        this.durability = source.getDurability();
        return this;
    }

    public ItemBuilder amount(final int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder amount(final ItemStack source) {
        this.amount = source.getAmount();
        return this;
    }

    public ItemBuilder name(final String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder name(final ItemMeta source) {
        this.name = source.getDisplayName();//ItemUtils.removeColors(source.getDisplayName());
        return this;
    }

    public ItemBuilder clearName() {
        this.name = null;
        return this;
    }

    public ItemBuilder lore(final List<String> lore) {
        this.lore = (lore == null) ? new ArrayList<String>(5) : new ArrayList<>(lore);
        return this;
    }

    public ItemBuilder lore(final ItemMeta source) {
        this.lore = source.hasLore() ? /*ItemUtils.removeColors(*/new ArrayList<>(source.getLore())/*)*/
                : new ArrayList<String>(5);
        return this;
    }

    public ItemBuilder newLoreLine(final String lore) {
        this.lore.add(lore);
        return this;
    }

    public ItemBuilder newLoreLine(final Object lore) {
        this.lore.add(lore.toString());
        return this;
    }

    public ItemBuilder newLoreLine(final Collection<String> lore) {
        this.lore.addAll(lore);
        return this;
    }

    public ItemBuilder newLoreLine(final String... lore) {
        this.newLoreLine(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder insertLoreLine(final int index, final String lore) {
        this.lore.add(index, lore);
        return this;
    }

    public ItemBuilder insertLoreLine(final int index, final Collection<String> lore) {
        this.lore.addAll(index, lore);
        return this;
    }

    public ItemBuilder insertLoreLine(final int index, final String... lore) {
        this.insertLoreLine(index, Arrays.asList(lore));
        return this;
    }

    public ItemBuilder removeLoreLine(final String lore) {
        this.lore.remove(lore);
        return this;
    }

    public ItemBuilder removeLoreLine(final Collection<String> lore) {
        this.lore.removeAll(lore);
        return this;
    }

    public ItemBuilder removeLoreLine(final String... lore) {
        this.removeLoreLine(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder removeLoreLine(final int lore) {
        this.lore.remove(lore);
        return this;
    }

    public ItemBuilder setLoreLine(final int index, final String lore) {
        this.lore.set(index, lore);
        return this;
    }

    public ItemBuilder clearLore() {
        this.lore.clear();
        return this;
    }

    public ItemBuilder enchant(final Map<Enchantment, Integer> enchants) {
        this.enchants = new LinkedHashMap<>(enchants);
        return this;
    }

    public ItemBuilder enchant(final ItemMeta source) {
        this.enchants = source.hasEnchants() ? new LinkedHashMap<>(source.getEnchants())
                : new LinkedHashMap<Enchantment, Integer>(3);
        return this;
    }

    public ItemBuilder enchant(final Enchantment enchantment, final int power) {
        this.enchants.put(enchantment, power);
        return this;
    }

    public ItemBuilder enchant(final Enchantment enchantment) {
        this.enchant(enchantment, 1);
        return this;
    }

    public ItemBuilder unEnchant(final Enchantment enchantment) {
        this.enchants.remove(enchantment);
        return this;
    }

    public ItemBuilder clearEnchants() {
        this.enchants.clear();
        return this;
    }

    public ItemBuilder data(final DataBuilder dataBuilder) {
        this.dataBuilder = dataBuilder;
        return this;
    }

    public ItemBuilder data(final ItemMeta meta) {
        //TODO Is there a more OOP way to do this?
        if (meta instanceof BookMeta) {
            this.dataBuilder = new BookDataBuilder();
        } else if (meta instanceof EnchantmentStorageMeta) {
            this.dataBuilder = new EnchantmentStorageBuilder();
        } else if (meta instanceof FireworkEffectMeta) {
            this.dataBuilder = new FireworkEffectBuilder();
        } else if (meta instanceof FireworkMeta) {
            this.dataBuilder = new FireworkBuilder();
        } else if (meta instanceof LeatherArmorMeta) {
            this.dataBuilder = new LeatherArmorBuilder();
        } else if (meta instanceof MapMeta) {
            this.dataBuilder = new MapBuilder();
        } else if (meta instanceof PotionMeta) {
            this.dataBuilder = new PotionDataBuilder();
        } else if (meta instanceof SkullMeta) {
            this.dataBuilder = new SkullBuilder();
        }
        if (this.dataBuilder != null) {
            this.dataBuilder.use(meta);
        }
        return this;
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(this.material, this.amount);
//        this.applyFunc();
        final ItemMeta meta =
                item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(this.material);
//        if (meta instanceof Damageable) { // 1.13+
        item.setDurability(this.durability); // 1.12
        //((Damageable) meta).setDamage(this.durability); // 1.13+
//        }
        if (this.name != null) {
            meta.setDisplayName(ItemUtils.fixColors(this.name));
        }
        if ((this.flags != null) && !this.flags.isEmpty()) {
            meta.addItemFlags(this.flags.toArray(new ItemFlag[this.flags.size()]));
        }
        if ((this.lore != null) && !this.lore.isEmpty()) {
            final List<String> lore = new ArrayList<>(this.lore.size() + 5);
            for (final String loreLine : this.lore) {
                Collections.addAll(lore, loreLine.split("\n"));
            }
//            this.lore.stream().forEach(str -> Collections.addAll(lore, str.split("\n")));
            meta.setLore(ItemUtils.fixColors(lore));
        }
        if (this.enchants != null) {
            for (final Map.Entry<Enchantment, Integer> entry : this.enchants.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }
        if (this.dataBuilder != null) {
            this.dataBuilder.apply(meta);
        }

        if (this.modelData != -1)
            meta.setCustomModelData(modelData);

        item.setItemMeta(meta);
        return item;
    }

    public ItemBuilder reset() {
        this.material = Material.AIR;
        this.amount = 1;
        this.durability = 0;
        this.name = null;
        if (this.flags != null) {
            this.flags.clear();
        } else {
            this.flags = new ArrayList<>(5);
        }
        if (this.lore != null) {
            this.lore.clear();
        } else {
            this.lore = new ArrayList<>(5);
        }
        if (this.enchants != null) {
            this.enchants.clear();
        } else {
            this.enchants = new LinkedHashMap<>(3);
        }
        this.dataBuilder = null;
        this.unbreakable = false;
        this.modelData = -1;
        return this;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        final SerializationBuilder b = SerializationBuilder.start(7);
        b.append("material", this.material);
        b.append("amount", this.amount);
        b.append("durability", this.durability);
        b.append("unbreakable", this.unbreakable);
        b.append("name", this.name);
        b.append("lore", this.lore);
        b.append("flags",
                (this.flags == null) ? new ArrayList<>(1)
                        : this.flags.stream().map(Enum::name).collect(Collectors.toList()));
        final SerializationBuilder enchant = SerializationBuilder.start(this.enchants.size());
        for (final Map.Entry<Enchantment, Integer> entry : this.enchants.entrySet()) {
            enchant.append(entry.getKey().getName(), entry.getValue());
        }
        b.append("enchants", enchant);
        b.append("data", (this.dataBuilder == null) ? null : this.dataBuilder.serialize());
        if (modelData != -1)
            b.append("modelData", modelData);
        return b.build();
    }

    public static ItemBuilder newItem(final Material material) {
        return new ItemBuilder().material(material);
    }

    public static ItemBuilder newItem(final ItemStack itemStack) {
        if (itemStack == null) {
            return new ItemBuilder();
        }
        final ItemBuilder itemBuilder = new ItemBuilder().material(itemStack).amount(itemStack).durability(itemStack);
        final ItemMeta    meta        = ItemUtils.getItemMeta(itemStack);
        if (meta == null) {
            return itemBuilder;
        }
        return itemBuilder.name(meta).lore(meta).enchant(meta).flag(meta).unbreakable(meta).data(meta);
    }
}
