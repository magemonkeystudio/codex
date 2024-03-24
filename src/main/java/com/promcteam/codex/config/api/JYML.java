package com.promcteam.codex.config.api;

import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.manager.api.gui.ContentType;
import com.promcteam.codex.manager.api.gui.GuiItem;
import com.promcteam.codex.manager.types.ClickType;
import com.promcteam.codex.utils.*;
import com.promcteam.codex.utils.actions.ActionManipulator;
import com.promcteam.codex.utils.constants.JStrings;
import com.promcteam.codex.utils.craft.api.ICraftRecipe;
import com.promcteam.codex.utils.craft.api.IFurnaceRecipe;
import com.promcteam.codex.utils.random.Rnd;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class JYML extends YamlConfiguration {

    private final File    file;
    private       boolean isChanged = false;

    public JYML(@NotNull String path, @NotNull String file) throws InvalidConfigurationException {
        this(new File(path, file));
    }

    public JYML(@NotNull File file) throws InvalidConfigurationException {
        FileUT.create(file);
        this.file = file;
        this.reload();
    }

    @NotNull
    public File getFile() {
        return this.file;
    }

    public void save() {
        try {
            this.save(this.file);
        } catch (IOException e) {
            CodexEngine.get().error("Could not save config: " + file.getName());
            e.printStackTrace();
        }
    }

    public boolean saveChanges() {
        if (this.isChanged) {
            this.save();
            this.isChanged = false;
            return true;
        }
        return false;
    }

    public boolean reload() throws InvalidConfigurationException {
        try {
            this.load(this.file);
            this.isChanged = false;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @NotNull
    public static JYML loadOrExtract(@NotNull CodexPlugin<?> plugin, @NotNull String filePath) throws
            InvalidConfigurationException {
        if (!plugin.getDataFolder().exists()) {
            FileUT.mkdir(plugin.getDataFolder());
        }
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }

        File file = new File(plugin.getDataFolder() + filePath);
        if (!file.exists()) {
            FileUT.create(file);
            try {
                InputStream input = plugin.getClass().getResourceAsStream(filePath);
                FileUT.copy(input, file);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return new JYML(file);
    }

    @NotNull
    public static List<JYML> loadAll(@NotNull String path, boolean deep) {
        List<JYML> configs = new ArrayList<>();
        for (File file : FileUT.getFiles(path, deep)) {
            try {
                configs.add(new JYML(file));
            } catch (InvalidConfigurationException e) {
                CodexEngine.get().error("Could not load " + file.getAbsolutePath() + ": Configuration error");
                e.printStackTrace();
            }
        }
        return configs;
    }

    // Add missing value
    public boolean addMissing(@NotNull String path, @Nullable Object val) {
        if (this.contains(path)) return false;
        this.set(path, val);
        return true;
    }

    @Override
    public void set(String path, @Nullable Object o) {
		/*if (o != null && o instanceof String) {
			String s = (String) o;
			s = s.replace('§', '&');
			o = s;
		}*/
        if (o instanceof Set) {
            o = new ArrayList<>((Set<?>) o);
        } else if (o instanceof Location) {
            o = LocUT.serialize((Location) o);
        }
        super.set(path, o);
        this.isChanged = true;
    }

    public boolean remove(@NotNull String path) {
        if (!this.contains(path)) return false;
        this.set(path, null);
        return true;
    }

    @NotNull
    public Set<String> getSection(@NotNull String path) {
        ConfigurationSection section = this.getConfigurationSection(path);
        return section == null ? Collections.emptySet() : section.getKeys(false);
    }

    @Override
    @Nullable
    public String getString(String path) {
        String str = super.getString(path);
        return str == null || str.isEmpty() ? null : str;
    }

    @Override
    @NotNull
    public String getString(String path, @Nullable String def) {
        String str = super.getString(path, def);
        if (str == null) {
            return def != null ? def : "";
        }
        return str;
    }

    @NotNull
    public Set<String> getStringSet(@NotNull String path) {
        return new HashSet<>(this.getStringList(path));
    }

    @Nullable
    public Location getLocation(String path) {
        String raw = this.getString(path);
        return raw == null ? null : LocUT.deserialize(raw);
    }

    @Deprecated
    public void setLocation(@NotNull String path, @Nullable Location loc) {
        this.set(path, loc == null ? null : LocUT.serialize(loc));
    }

    public int[] getIntArray(@NotNull String path) {
        int[] slots = new int[0];

        String str = this.getString(path);
        if (str == null) return slots;

        String[] raw = str.replaceAll("\\s", "").split(",");
        slots = new int[raw.length];
        for (int i = 0; i < raw.length; i++) {
            try {
                slots[i] = Integer.parseInt(raw[i].trim());
            } catch (NumberFormatException ex) {
            }
        }
        return slots;
    }

    public void setIntArray(@NotNull String path, int[] arr) {
        if (arr == null) {
            this.set(path, null);
            return;
        }

        StringBuilder str = new StringBuilder();
        for (int num : arr) {
            if (str.length() > 0) {
                str.append(",");
            }
            str.append(num);
        }
        this.set(path, str.toString());
    }

    @NotNull
    public ItemStack getItem(@NotNull String path) {
        return this.getItem(path, false);
    }

    @NotNull
    public ItemStack getItem(@NotNull String path, boolean id) {
        if (!path.isEmpty() && !path.endsWith(".")) path = path + ".";

        Material material = Material.getMaterial(this.getString(path + "material", "").toUpperCase());
        if (material == null || material == Material.AIR) return new ItemStack(Material.AIR);

        ItemStack item = new ItemStack(material);
        ItemMeta  meta = item.getItemMeta();
        if (meta == null) return item;

        item.setAmount(this.getInt(path + "amount", 1));

        String hash = this.getString(path + "skull-hash", this.getString(path + "head-texture"));
        if (!hash.isEmpty()) {
            if (id) {
                String idz = this.getFile().getName().replace(".yml", "");
                ItemUT.addSkullTexture(item, hash, idz);
            } else {
                ItemUT.addSkullTexture(item, hash);
            }
        }

        int durability = this.getInt(path + "durability");
        if (durability > 0 && meta instanceof Damageable) {
            Damageable d = (Damageable) meta;
            d.setDamage(durability);
        }

        String name = this.getString(path + "name");
        meta.setDisplayName(name != null ? StringUT.color(name) : null);
        meta.setLore(StringUT.color(this.getStringList(path + "lore")));

        if (this.getBoolean(path + "enchanted")) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
        }

        int model = this.getInt(path + "model-data", this.getInt(path + "custom-model-data"));
        meta.setCustomModelData(model != 0 ? model : null);

        List<String> flags = this.getStringList(path + "item-flags");
        if (flags.contains(JStrings.MASK_ANY)) {
            meta.addItemFlags(ItemFlag.values());
        } else {
            for (String flag : flags) {
                ItemFlag itemFlag = CollectionsUT.getEnum(flag, ItemFlag.class);
                if (itemFlag != null) meta.addItemFlags(itemFlag);
            }
        }

        String color = this.getString(path + "color");
        if (color != null && !color.isEmpty()) {
            String[] rgb = color.split(",");
            int      r   = StringUT.getInteger(rgb[0], Rnd.get(255));
            int      g   = rgb.length >= 2 ? StringUT.getInteger(rgb[1], Rnd.get(255)) : Rnd.get(255);
            int      b   = rgb.length >= 3 ? StringUT.getInteger(rgb[2], Rnd.get(255)) : Rnd.get(255);

            if (meta instanceof LeatherArmorMeta) {
                LeatherArmorMeta lm = (LeatherArmorMeta) meta;
                lm.setColor(Color.fromRGB(r, g, b));
            } else if (meta instanceof PotionMeta) {
                PotionMeta pm = (PotionMeta) meta;
                pm.setColor(Color.fromRGB(r, g, b));
            }
        }

        meta.setUnbreakable(this.getBoolean(path + "unbreakable"));
        item.setItemMeta(meta);

        return item;
    }

    @Nullable
    public GuiItem getGuiItem(@NotNull String path) {
        return this.getGuiItem(path, null);
    }

    @Nullable
    public <T extends Enum<T>> GuiItem getGuiItem(@NotNull String path, @Nullable Class<T> clazzEnum) {
        if (!path.endsWith(".")) path = path + ".";

        ItemStack item = this.getItem(path);
        if (item.getType() == Material.AIR) return null;

        int[] slots = this.getIntArray(path + "slots");

        // Get custom enum type of Gui Item.
        Enum<?> type = ContentType.NONE;
        if (clazzEnum != null) {
            String      typeRaw = this.getString(path + "type", "");
            @Nullable T eConfig = CollectionsUT.getEnum(typeRaw, clazzEnum);
            type = eConfig == null ? CollectionsUT.getEnum(typeRaw, ContentType.class) : eConfig;
        }

        boolean animAutoPlay   = this.getBoolean(path + "animation.auto-play");
        int     animStartFrame = this.getInt(path + "animation.start-frame");

        TreeMap<Integer, ItemStack> animFrames = new TreeMap<>();
        for (String sFrame : this.getSection(path + "animation-frames")) {
            int frame = StringUT.getInteger(sFrame, -1);
            if (frame < 0) continue;

            ItemStack frameItem = this.getItem(path + "animation-frames." + sFrame);
            animFrames.put(frame, frameItem);
        }
        if (animFrames.isEmpty()) {
            for (String sFrame : this.getSection(path + "animation.frames")) {
                int frame = StringUT.getInteger(sFrame, -1);
                if (frame < 0) continue;

                ItemStack frameItem = this.getItem(path + "animation.frames." + sFrame);
                animFrames.put(frame, frameItem);
            }
        }

        String[] ss = path.split("\\.");
        String   id = ss[ss.length - 1];
        if (id == null || id.isEmpty()) {
            id = file.getName().replace(".yml", "") + "-icon-" + Rnd.get(3000);
        }

        Map<ClickType, ActionManipulator> customClicks = new HashMap<>();
        for (String sType : this.getSection(path + "custom-actions")) {
            ClickType clickType = CollectionsUT.getEnum(sType, ClickType.class);
            if (clickType == null) continue;

            ActionManipulator actions =
                    new ActionManipulator(CodexEngine.get(), this, path + "custom-actions." + sType);
            customClicks.put(clickType, actions);
        }

        String permission = this.getString(path + "permission");

        GuiItem guiItem = new GuiItem(
                id, type, item,
                animAutoPlay, animStartFrame, animFrames,
                customClicks, permission, slots);

        return guiItem;
    }

    public void setItem(@NotNull String path, @Nullable ItemStack item) {
        if (item == null) {
            this.set(path, null);
            return;
        }

        if (!path.endsWith(".")) path = path + ".";
        this.set(path.substring(0, path.length() - 1), null);

        Material material = item.getType();
        this.set(path + "material", material.name());
        this.set(path + "amount", item.getAmount());
        this.set(path + "head-texture", ItemUT.getSkullTexture(item));

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (meta instanceof Damageable) {
            int durability = ((Damageable) meta).getDamage();
            this.set(path + "durability", durability);
        }

        if (meta.hasDisplayName()) {
            this.set(path + "name", StringUT.colorRaw(meta.getDisplayName()));
        }

        List<String> lore = meta.getLore();
        if (lore != null) {
            List<String> loreRaw = new ArrayList<>();
            lore.forEach(line -> loreRaw.add(StringUT.colorRaw(line)));
            this.set(path + "lore", loreRaw);
        }

        this.set(path + "enchanted", meta.hasEnchants());
        this.set(path + "custom-model-data", meta.hasCustomModelData() ? meta.getCustomModelData() : null);

        Color  color    = null;
        String colorRaw = null;
        if (meta instanceof PotionMeta) {
            PotionMeta pm = (PotionMeta) meta;
            color = pm.getColor();
        } else if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta lm = (LeatherArmorMeta) meta;
            color = lm.getColor();
        }
        if (color != null) {
            colorRaw = new StringBuilder()
                    .append(color.getRed()).append(",")
                    .append(color.getGreen()).append(",")
                    .append(color.getBlue()).append(",").toString();
        }
        this.set(path + "color", colorRaw);

        List<String> itemFlags = meta.getItemFlags().stream().map(ItemFlag::name).collect(Collectors.toList());
        this.set(path + "item-flags", itemFlags);
        this.set(path + "unbreakable", meta.isUnbreakable());
    }

    // Base 64

    @Nullable
    public ItemStack getItem64(@NotNull String path) {
        String code = this.getString(path);
        if (code == null) return null;

        return ItemUT.fromBase64(code);
    }

    public void setItem64(@NotNull String path, @Nullable ItemStack item) {
        if (item == null) {
            this.set(path, null);
        } else {
            try {
                String code = ItemUT.toBase64(item);
                this.set(path, code);
            } catch (Exception e) {
                CodexEngine.get().getLogger().warning("Could not set Item64");
                e.printStackTrace();
            }
        }
    }

    @NotNull
    public ItemStack[] getItemList64(@NotNull String path) {
        List<String> list = this.getStringList(path);
        return ItemUT.fromBase64(list);
    }

    public void setItemList64(@NotNull String path, @NotNull List<ItemStack> item) {
        List<String> code = ItemUT.toBase64(item);
        this.set(path, code);
    }

    // ----------------------------------------- //

    @Nullable
    public ICraftRecipe getCraftRecipe(@NotNull CodexPlugin<?> plugin, @NotNull String path) {
        if (!path.endsWith(".")) path += ".";

        String    id     = this.getString(path + "id", file.getName().replace(".yml", ""));
        boolean   shape  = this.getBoolean(path + "shaped");
        ItemStack result = this.getItem64(path + "result");
        if (result == null) {
            return null;
        }

        ICraftRecipe recipe   = new ICraftRecipe(plugin, id, result, shape);
        int          ingCount = 0;
        for (String s : this.getSection(path + "ingredients")) {
            String    path2 = path + "ingredients." + s;
            ItemStack ing   = this.getItem64(path2);
            recipe.addIngredient(ingCount, ing);

            ingCount++;
        }

        return recipe;
    }

    public void setRecipe(@NotNull String path, @Nullable ICraftRecipe recipe) {
        if (!path.endsWith(".")) path += ".";
        if (recipe == null) {
            if (path.endsWith(".")) path = path.substring(0, path.length() - 1);
            this.set(path, null);
            return;
        }

        this.set(path + "shaped", recipe.isShaped());
        this.setItem64(path + "result", recipe.getResult());

        char[] ziga = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};

        ItemStack[] ings = recipe.getIngredients();
        for (int i = 0; i < ings.length; i++) {
            char c = ziga[i];
            this.setItem64(path + "ingredients." + c, ings[i]);
        }
    }

    @Nullable
    public IFurnaceRecipe getFurnaceRecipe(@NotNull CodexPlugin<?> plugin, @NotNull String path) {
        if (!path.endsWith(".")) path += ".";

        String    id     = this.getString(path + "id", file.getName().replace(".yml", ""));
        ItemStack input  = this.getItem64(path + "input");
        ItemStack result = this.getItem64(path + "result");
        if (result == null || input == null) {
            return null;
        }
        float  exp  = (float) this.getDouble(path + "exp");
        double time = this.getDouble(path + "time");

        IFurnaceRecipe recipe = new IFurnaceRecipe(plugin, id, result, exp, time);
        recipe.addIngredient(input);

        return recipe;
    }

    public void setRecipe(@NotNull String path, @Nullable IFurnaceRecipe recipe) {
        if (!path.endsWith(".")) path += ".";
        if (recipe == null) {
            if (path.endsWith(".")) path = path.substring(0, path.length() - 1);

            this.set(path, null);
            return;
        }

        this.setItem64(path + "input", recipe.getInput());
        this.setItem64(path + "result", recipe.getResult());
        this.set(path + "exp", recipe.getExp());
        this.set(path + "time", recipe.getTime() / 20D); // Turn to decimal seconds
    }
}
