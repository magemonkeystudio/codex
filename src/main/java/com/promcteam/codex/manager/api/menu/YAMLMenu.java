package com.promcteam.codex.manager.api.menu;

import com.promcteam.codex.utils.ItemUT;
import com.promcteam.codex.utils.StringUT;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public abstract class YAMLMenu<T> {
    private static final Set<YAMLMenu<?>> YAML_MENUS = new HashSet<>();

    public static void reloadMenus(Plugin plugin) {
        YAML_MENUS.stream().filter(yamlMenu -> yamlMenu.plugin.equals(plugin)).forEach(YAMLMenu::reload);
    }

    protected final Plugin                          plugin;
    protected final String                          path;
    protected       String                          title;
    protected       int                             rows;
    protected       NavigableMap<Integer, String>   slots;
    protected       NavigableMap<String, ItemStack> items;

    public YAMLMenu(Plugin plugin, String path) {
        this.plugin = plugin;
        this.path = path;
        reload();
        YAML_MENUS.add(this);
    }

    public void reload() {
        File file = new File(this.plugin.getDataFolder(), this.path);
        if (!file.exists()) {
            plugin.saveResource(this.path, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        this.title = StringUT.color(config.getString("title", ""));
        this.rows = config.getInt("rows", 6);
        NavigableMap<Integer, String>   slots   = new TreeMap<>();
        NavigableMap<String, ItemStack> items   = new TreeMap<>();
        ConfigurationSection            section = config.getConfigurationSection("slots");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            try {
                int i = Integer.parseInt(key);
                if (i < 0) {
                    throw new IllegalArgumentException();
                }
                String function = section.getString(key);
                if (function == null) {
                    continue;
                }
                slots.put(i, function);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid index \"" + key + "\" in " + this);
            }
        }

        section = config.getConfigurationSection("items");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection yamlItem = section.getConfigurationSection(key);
            if (yamlItem == null) {
                plugin.getLogger().warning("Invalid item \"" + key + "\" in " + this);
                continue;
            }
            String   materialString = yamlItem.getString("material", "dirt");
            Material material;
            try {
                material = Material.valueOf(materialString.toUpperCase().replace('-', '_'));
            } catch (IllegalArgumentException e1) {
                plugin.getLogger().warning("Invalid material \"" + materialString + "\" in menu " + this);
                material = Material.DIRT;
            }
            ItemStack itemStack    = new ItemStack(material, yamlItem.getInt("amount", 1));
            String    skullTexture = yamlItem.getString("skull-texture", null);
            if (skullTexture != null) {
                ItemUT.addSkullTexture(itemStack, skullTexture);
            }
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                String displayName = yamlItem.getString("display-name");
                if (displayName != null) {
                    meta.setDisplayName(StringUT.color(displayName));
                }
                List<String> lore = StringUT.color(yamlItem.getStringList("lore"));
                if (lore.size() > 0) {
                    meta.setLore(lore);
                }
                int cmd = yamlItem.getInt("custom-model-data", 0);
                if (cmd != 0) {
                    meta.setCustomModelData(cmd);
                }
                itemStack.setItemMeta(meta);
            }
            items.put(key, itemStack);
        }

        this.slots = Collections.unmodifiableNavigableMap(slots);
        this.items = Collections.unmodifiableNavigableMap(items);
    }

    protected abstract String getTitle(String yamlTitle, T parameter);

    public String getTitle(T parameter) {return this.getTitle(this.title, parameter);}

    public int getRows() {return rows;}

    public boolean isEmpty() {return this.slots.isEmpty();}

    public NavigableMap<Integer, String> getSlots() {return slots;}

    public NavigableMap<String, ItemStack> getItems() {return items;}

    @NotNull
    public ItemStack getItem(String name) {return this.items.getOrDefault(name, new ItemStack(Material.AIR)).clone();}

    @Nullable
    public abstract Slot getSlot(String function, T parameter, Player player);

    public void setSlots(Menu menu, T parameter) {
        menu.slots.clear();
        Player player = menu.getPlayer();
        for (Map.Entry<Integer, String> entry : this.slots.entrySet()) {
            Slot slot = this.getSlot(entry.getValue(), parameter, player);
            if (slot != null) {
                menu.setSlot(entry.getKey(), slot);
            }
        }
        ItemStack emptySlot = this.getItem("empty");
        for (int i = 0, size = menu.slots.lastKey() / (this.getRows() * 9) + 1; i < size; i++) {
            menu.slots.putIfAbsent(i, new Slot(emptySlot));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YAMLMenu<?> yamlMenu = (YAMLMenu<?>) o;
        return this.plugin.equals(yamlMenu.plugin) && this.path.equals(yamlMenu.path);
    }

    @Override
    public int hashCode() {return Objects.hash(this.plugin, this.path);}

    @Override
    public String toString() {return this.plugin.getName() + ':' + this.path;}
}
