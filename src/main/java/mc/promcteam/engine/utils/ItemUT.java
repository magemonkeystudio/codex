package mc.promcteam.engine.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.Setter;
import mc.promcteam.engine.NexEngine;
import mc.promcteam.engine.config.ConfigManager;
import mc.promcteam.engine.core.Version;
import mc.promcteam.engine.hooks.Hooks;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ItemUT {

    @Setter
    @Getter
    public static NexEngine engine;

    public static final  String                     LORE_FIX_PREFIX = "fogus_loren-";
    public static final  String                     NAME_FIX_PREFIX = "fogus_namel-";
    public static final  String                     TAG_SPLITTER    = "__x__";

    public static int addToLore(@NotNull List<String> lore, int pos, @NotNull String value) {
        if (pos >= lore.size() || pos < 0) {
            lore.add(value);
        } else {
            lore.add(pos, value);
        }
        return pos + 1;
    }

    public static void addLore(@NotNull ItemStack item, @NotNull String id, @NotNull String text, int pos) {
        String[] lines = text.split(TAG_SPLITTER);
        addLore(item, id, Arrays.asList(lines), pos);
    }

    public static void addLore(@NotNull ItemStack item, @NotNull String id, @NotNull List<String> text, int pos) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();

        text = StringUT.color(text);
        StringBuilder loreTag = new StringBuilder();

        delLore(item, id);
        for (String line : text) {
            pos = addToLore(lore, pos, line);

            if (loreTag.length() > 0) loreTag.append(TAG_SPLITTER);
            loreTag.append(line);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        addLoreTag(item, id, loreTag.toString());
    }

    public static void delLore(@NotNull ItemStack item, @NotNull String id) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) return;

        int index = getLoreIndex(item, id, 0);
        if (index < 0) return;

        int lastIndex = getLoreIndex(item, id, 1);
        int diff      = lastIndex - index;

        for (int i = 0; i < (diff + 1); i++) {
            lore.remove(index);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        delLoreTag(item, id);
    }

    public static int getLoreIndex(@NotNull ItemStack item, @NotNull String id) {
        return getLoreIndex(item, id, 0);
    }

    public static int getLoreIndex(@NotNull ItemStack item, @NotNull String id, int type) {
        String storedText = DataUT.getStringData(item, ItemUT.getLoreKey(id));
        if (storedText == null) storedText = DataUT.getStringData(item, ItemUT.getLoreKey2(id));
        if (storedText == null) return -1;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return -1;

        List<String> lore = meta.getLore();
        if (lore == null) return -1;

        String[] lines    = storedText.split(TAG_SPLITTER);
        String   lastText = null;
        int      count    = 0;

        if (type == 0) {
            for (int i = 0; i < lines.length; i++) {
                lastText = lines[i];
                if (!StringUT.colorOff(lastText).isEmpty()) {
                    break;
                }
                count--;
            }
        } else {
            for (int i = lines.length; i > 0; i--) {
                lastText = lines[i - 1];
                if (!StringUT.colorOff(lastText).isEmpty()) {
                    break;
                }
                count++;
            }
        }

        if (lastText == null) return -1;

        int index = lore.indexOf(lastText) + count;

        // Clean up invalid lore tags.
        if (index < 0) {
            delLoreTag(item, id);
        }
        return index;
    }

    @NotNull
    private static NamespacedKey getLoreKey(@NotNull String id) {
        return new NamespacedKey(engine, LORE_FIX_PREFIX + id.toLowerCase());
    }

    @NotNull
    private static NamespacedKey getLoreKey2(@NotNull String id) {
        return Objects.requireNonNull(NamespacedKey.fromString("nexengine:" + LORE_FIX_PREFIX + id.toLowerCase()));
    }

    @NotNull
    private static NamespacedKey getNameKey(@NotNull String id) {
        return new NamespacedKey(engine, NAME_FIX_PREFIX + id.toLowerCase());
    }

    public static void addLoreTag(@NotNull ItemStack item, @NotNull String id, @NotNull String text) {
        DataUT.setData(item, ItemUT.getLoreKey(id), text);
    }

    public static void delLoreTag(@NotNull ItemStack item, @NotNull String id) {
        DataUT.removeData(item, ItemUT.getLoreKey(id));
        DataUT.removeData(item, NamespacedKey.fromString("nexengine:" + LORE_FIX_PREFIX + id));
    }

    @Nullable
    public static String getLoreTag(@NotNull ItemStack item, @NotNull String id) {
        String data = DataUT.getStringData(item, ItemUT.getLoreKey(id));
        return data != null
                ? data
                : DataUT.getStringData(item, NamespacedKey.fromString("nexengine:" + LORE_FIX_PREFIX + id));
    }

    public static void addNameTag(@NotNull ItemStack item, @NotNull String id, @NotNull String text) {
        DataUT.setData(item, ItemUT.getNameKey(id), text);
    }

    public static void delNameTag(@NotNull ItemStack item, @NotNull String id) {
        DataUT.removeData(item, ItemUT.getNameKey(id));
        DataUT.removeData(item, NamespacedKey.fromString("nexengine:" + NAME_FIX_PREFIX + id));
    }

    @Nullable
    public static String getNameTag(@NotNull ItemStack item, @NotNull String id) {
        String data = DataUT.getStringData(item, ItemUT.getNameKey(id));
        String name = data != null
                ? data
                : DataUT.getStringData(item, NamespacedKey.fromString("nexengine:" + NAME_FIX_PREFIX + id));
        return name;
    }

    @NotNull
    public static String getItemName(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        String name = meta != null && meta.hasDisplayName()
                ? meta.getDisplayName()
                : getEngine().lang().getEnum(item.getType());
        return name;
    }

    @NotNull
    public static void addSkullTexture(@NotNull ItemStack item, @NotNull String value) {
        ItemUT.addSkullTexture(item, value, "");
    }

    @NotNull
    public static void addSkullTexture(@NotNull ItemStack item, @NotNull String value, @NotNull String id) {
        if (item.getType() != Material.PLAYER_HEAD) return;

        UUID uuid = ConfigManager.getTempUUID(id);
        if (uuid == null) uuid = UUID.randomUUID();

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return;

        GameProfile profile = new GameProfile(uuid, uuid.toString().substring(0, 16));
        profile.getProperties().put("textures", new Property("textures", value));
        Reflex.invokeMethod(Reflex.getMethod(meta, "setProfile", GameProfile.class), meta, profile);

        item.setItemMeta(meta);
    }

    @Nullable
    public static String getSkullTexture(@NotNull ItemStack item) {
        if (item.getType() != Material.PLAYER_HEAD) return null;

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return null;

        GameProfile profile = (GameProfile) Reflex.getFieldValue(meta, "profile");
        if (profile == null) return null;

        Collection<Property> properties = profile.getProperties().get("textures");
        Optional<Property> opt = properties.stream()
                .filter(prop -> prop.getName().equalsIgnoreCase("textures") || prop.getSignature().equalsIgnoreCase("textures"))
                .findFirst();

        return opt.isPresent() ? opt.get().getValue() : null;
    }

    public static void applyPlaceholderAPI(@NotNull Player player, @NotNull ItemStack item) {
        if (!Hooks.hasPlaceholderAPI()) return;
        replace(item, str -> PlaceholderAPI.setPlaceholders(player, str));
    }

    public static void replace(@NotNull ItemStack item, @NotNull UnaryOperator<String> cs) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String name = cs.apply(meta.hasDisplayName() ? meta.getDisplayName() : "");
        meta.setDisplayName(name);

        List<String> lore = meta.getLore();
        if (lore != null) {
            lore.replaceAll(cs);
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
    }

    public static void replaceLore(@NotNull ItemStack item, @NotNull String placeholder, @NotNull List<String> replacer) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        List<String> lore = meta.getLore();
        if (lore == null) {
            return;
        }

        meta.setLore(StringUT.replace(lore, placeholder, replacer));
        item.setItemMeta(meta);
    }

    public static void replaceLore(@NotNull ItemStack item, @NotNull String placeholder, @NotNull String replacer) {
        replaceLore(item, placeholder, List.of(replacer));
    }

    public static void addItem(@NotNull Player player, @NotNull ItemStack... items) {
        Inventory inv   = player.getInventory();
        World     world = player.getWorld();

        for (ItemStack item : items) {
            if (isAir(item)) continue;
            if (inv.firstEmpty() == -1) {
                world.dropItem(player.getLocation(), item);
            } else {
                inv.addItem(item);
            }
        }
    }

    public static boolean isAir(@Nullable ItemStack item) {
        if (item == null) return true;

        if (Version.CURRENT.isHigher(Version.V1_14_R1)) {
            return item.getType() == Material.AIR || item.getType().isAir();
        }
        return item.getType() == Material.AIR;
    }

    public static boolean isWeapon(@NotNull ItemStack item) {
        return getEngine().getNMS().isWeapon(item);
    }

    public static boolean isTool(@NotNull ItemStack item) {
        return getEngine().getNMS().isTool(item);
    }

    public static boolean isArmor(@NotNull ItemStack item) {
        return getEngine().getNMS().isArmor(item);
    }

    public static boolean isBow(@NotNull ItemStack item) {
        return item.getType() == Material.BOW || item.getType() == Material.CROSSBOW;
    }

    @NotNull
    public static EquipmentSlot[] getItemSlots(@NotNull ItemStack item) {
        if (isArmor(item)) {
            return new EquipmentSlot[]{getEquipmentSlotByItemType(item)};
        }
        return new EquipmentSlot[]{EquipmentSlot.HAND, EquipmentSlot.OFF_HAND};
    }

    @NotNull
    public static EquipmentSlot getEquipmentSlotByItemType(@NotNull ItemStack item) {
        String raw = item.getType().name();
        if (raw.contains("HELMET") || raw.contains("SKULL") || raw.contains("HEAD")) {
            return EquipmentSlot.HEAD;
        }
        if (raw.endsWith("CHESTPLATE") || raw.endsWith("ELYTRA")) {
            return EquipmentSlot.CHEST;
        }
        if (raw.endsWith("LEGGINGS")) {
            return EquipmentSlot.LEGS;
        }
        if (raw.endsWith("BOOTS")) {
            return EquipmentSlot.FEET;
        }
        if (item.getType() == Material.SHIELD) {
            return EquipmentSlot.OFF_HAND;
        }
        return EquipmentSlot.HAND;
    }

    @NotNull
    public static String toJson(@NotNull ItemStack item) {
        return getEngine().getNMS().toJSON(item);
    }

    @Nullable
    public static String toBase64(@NotNull ItemStack item) {
        try {
            return getEngine().getNMS().toBase64(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @NotNull
    public static List<String> toBase64(@NotNull ItemStack[] item) {
        return toBase64(Arrays.asList(item));
    }

    @NotNull
    public static List<String> toBase64(@NotNull List<ItemStack> items) {
        List<String> list = items.stream().map(item -> {
                    try {
                        return toBase64(item);
                    } catch (Exception e) {
                        getEngine().getLogger().warning("Could not convert to b64.");
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toList());
        list.removeIf(data -> data == null);
        return list;
    }

    @Nullable
    public static ItemStack fromBase64(@NotNull String data) {
        return getEngine().getNMS().fromBase64(data);
    }

    @NotNull
    public static ItemStack[] fromBase64(@NotNull List<String> list) {
        List<ItemStack> items = list.stream().map(base -> fromBase64(base))
                .collect(Collectors.toList());
        items.removeIf(item -> item == null);
        return items.toArray(new ItemStack[list.size()]);
    }
}
