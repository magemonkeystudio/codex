package studio.magemonkey.codex.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import studio.magemonkey.codex.api.items.ItemType;
import studio.magemonkey.codex.legacy.placeholder.PlaceholderRegistry;
import studio.magemonkey.codex.legacy.placeholder.PlaceholderType;
import studio.magemonkey.codex.util.messages.MessageData;
import studio.magemonkey.codex.util.messages.MessageUtil;
import studio.magemonkey.codex.util.messages.NMSPlayerUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemUtils {
    public static final PlaceholderType<ItemType> ITEM_TYPE =
            PlaceholderType.create("itemTYpe", ItemType.class);

    static {
        ITEM_TYPE.registerItem("name", item -> {
            ItemStack itemStack = item.create();
            ItemMeta  meta      = itemStack.getItemMeta();
            String    string    = null;
            if (meta != null) {
                string = meta.getDisplayName();
                if (string.isEmpty()) {
                    try {
                        string = meta.getItemName();
                    } catch (NoSuchMethodError ignored) {
                    }
                }
            }
            BaseComponent baseComponent;
            if (string == null || string.isEmpty()) {
                baseComponent = new TranslatableComponent(itemStack.getType().getItemTranslationKey());
            } else {
                baseComponent = new TextComponent(string);
            }
            baseComponent.setHoverEvent(NMSPlayerUtils.convert(itemStack));
            return baseComponent;
        });
        ITEM_TYPE.registerItem("displayName", item -> {
            ItemStack itemStack = item.create();
            ItemMeta  meta      = itemStack.getItemMeta();
            String    string    = null;
            if (meta != null) {
                string = meta.getDisplayName();
                if (string.isEmpty()) {
                    try {
                        string = meta.getItemName();
                    } catch (NoSuchMethodError ignored) {
                    }
                }
            }
            BaseComponent baseComponent;
            if (string == null || string.isEmpty()) {
                baseComponent = new TranslatableComponent(itemStack.getType().getItemTranslationKey());
            } else {
                baseComponent = new TextComponent(string);
            }
            baseComponent.setHoverEvent(NMSPlayerUtils.convert(itemStack));
            return baseComponent;
        });
        ITEM_TYPE.registerItem("material", d -> d.create().getType());
        ITEM_TYPE.registerItem("id", item -> {
            TextComponent textComponent = new TextComponent(item.getNamespacedID());
            textComponent.setHoverEvent(NMSPlayerUtils.convert(item.create()));
            return textComponent;
        });
        ITEM_TYPE.registerItem("lore", c -> StringUtils.join(c.create().getItemMeta().getLore(), '\n'));
        ITEM_TYPE.registerItem("enchantments", c -> StringUtils.join(c.create().getEnchantments().keySet(), ", "));
        ITEM_TYPE.registerChild("item", PlaceholderRegistry.ITEM, ItemType::create);
    }

    public static ItemStack replaceText(ItemStack item, MessageData... replace) {
        if (item == null) return null;
        item = item.clone();

        ItemMeta im = getItemMeta(item);
        if (im.hasDisplayName()) {
            im.setDisplayName(MessageUtil.getMessageAsString(im.getDisplayName(), im.getDisplayName(), false, replace));
        }

        if (im.getLore() != null && !im.getLore().isEmpty()) {
            List<String> newLore = im.getLore()
                    .stream()
                    .map(lore -> MessageUtil.getMessageAsString(lore, lore, false, replace))
                    .collect(Collectors.toList());
            im.setLore(newLore);
        }

        item.setItemMeta(im);
        return item;
    }

    /**
     * Attempts to match the string to a bukkit material.
     *
     * @param mat the string to match
     * @return the material that matches the input string
     */
    @SuppressWarnings("deprecation")
    public static Material getMaterial(final String mat) {
        Material material = Material.getMaterial(mat);
        if (material != null) return material;

        material = Material.matchMaterial(mat);
        if (material != null) return material;

        try {
            final int id = Integer.parseInt(mat);
            Bukkit.getLogger()
                    .severe("Codex attempting to get a material by it's id. Please change it to a name. ID: " + mat);
            Thread.dumpStack();
            for (Material m : Material.class.getEnumConstants()) {
                if (m.getId() == id) {
                    return m;
                }
            }
        } catch (final Exception ignored) {
            return Material.AIR;
        }

        return null;
    }

    public static String removeColors(String str) {
        if (str == null) return null;
        return ChatColor.stripColor(str);
    }

    /**
     * Strips colors from the list.
     *
     * @param list List of Strings to remove color from
     * @return New ArrayList of cleaned Strings
     */
    public static List<String> removeColors(List<String> list) {
        if (list == null) return null;

        return list.stream().map(ItemUtils::removeColors).collect(Collectors.toList());
    }

    /**
     * Fixes color characters in a string
     *
     * @param msg message
     * @return new string
     */
    public static String fixColors(String msg) {
        if (msg == null) return null;

        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    /**
     * Fixes color characters in a List
     *
     * @param msg List
     * @return new List
     */
    public static ArrayList<String> fixColors(List<String> msg) {
        if (msg == null) return null;
        ArrayList<String> ret = new ArrayList<>();
        for (String s : msg) {
            String fixColors = fixColors(s);
            ret.add(fixColors);
        }
        return ret;
    }

    public static ItemMeta getItemMeta(final ItemStack itemStack) {
        final ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        return meta;
    }

    public static FireworkEffect simpleDeserializeEffect(final Map<Object, Object> map) {
        if (map == null) return null;
        final DeserializationWorker w = DeserializationWorker.startUnsafe(map);

        final FireworkEffect.Type type       = w.getEnum("type", FireworkEffect.Type.BALL);
        final boolean             trail      = w.getBoolean("trail");
        final boolean             flicker    = w.getBoolean("flicker");
        final List<Color>         colors     = simpleDeserializeColors(w.getTypedObject("colors"));
        final List<Color>         fadeColors = simpleDeserializeColors(w.getTypedObject("fadeColors"));
        return FireworkEffect.builder()
                .with(type)
                .trail(trail)
                .flicker(flicker)
                .withColor(colors)
                .withFade(fadeColors)
                .build();
    }

    public static List<FireworkEffect> simpleDeserializeEffects(final Collection<Map<Object, Object>> list) {
        if (list == null) return new ArrayList<>(1);
        final List<FireworkEffect> result = new ArrayList<>(list.size());
        for (final Map<Object, Object> map : list) {
            result.add(simpleDeserializeEffect(map));
        }
        return result;
    }

    public static Color simpleDeserializeColor(final String string) {
        if (string == null) return null;
        return Color.fromRGB(Integer.parseInt(string, 16));
    }

    public static List<Color> simpleDeserializeColors(final Collection<String> strings) {
        if (strings == null) return new ArrayList<>(1);
        final List<Color> result = new ArrayList<>(strings.size());
        for (final String str : strings) {
            result.add(simpleDeserializeColor(str));
        }
        return result;
    }

    public static String simpleSerializeColor(final Color color) {
        if (color == null) return null;
        return Integer.toString(color.asRGB(), 16);
    }

    public static List<String> simpleSerializeColors(final Collection<Color> colors) {
        if (colors == null) return new ArrayList<>(1);
        final List<String> result = new ArrayList<>(colors.size());
        for (final Color color : colors) {
            result.add(simpleSerializeColor(color));
        }
        return result;
    }

    public static Map<String, Object> simpleSerializeEffect(final FireworkEffect effect) {
        if (effect == null) return null;
        final SerializationBuilder b = SerializationBuilder.start(5);
        b.append("type", effect.getType());
        b.append("trail", effect.hasTrail());
        b.append("flicker", effect.hasFlicker());
        b.append("colors", simpleSerializeColors(effect.getColors()));
        b.append("fadeColors", simpleSerializeColors(effect.getFadeColors()));
        return b.build();
    }

    public static List<Map<String, Object>> simpleSerializeEffects(final Collection<FireworkEffect> effects) {
        if (effects == null) return new ArrayList<>(1);
        final List<Map<String, Object>> result = new ArrayList<>(effects.size());
        for (final FireworkEffect effect : effects) {
            result.add(simpleSerializeEffect(effect));
        }
        return result;
    }

    /**
     * Compact given array, it will create the smallest possible array with given items,
     * so it will join duplicated items etc.
     *
     * @param respectStackSize if method should respect max stack size.
     * @param itemsToCompact   item to compact.
     * @return compacted array of items.
     */
    public static ItemStack[] compact(final boolean respectStackSize, final ItemStack... itemsToCompact) {
        final ItemStack[] items = new ItemStack[itemsToCompact.length];
        int               j     = 0;
        for (final ItemStack itemStack : itemsToCompact) {
            items[j++] = (itemStack == null) ? null : itemStack.clone();
        }

        for (int i = 0, itemsLength = items.length; i < itemsLength; i++) {
            final ItemStack item = items[i];
            if ((item == null) || (item.getType() == Material.AIR)) {
                continue;
            }
            for (int k = i + 1; k < itemsLength; k++) {
                final ItemStack item2 = items[k];
                if (!item.isSimilar(item2)) continue;

                if (!respectStackSize) {
                    item.setAmount(item.getAmount() + item2.getAmount());
                    items[k] = null;
                    continue;
                }

                final int space = item.getMaxStackSize() - item.getAmount();
                if (space > 0) {
                    final int toAdd = item2.getAmount();
                    if (space > toAdd) {
                        item.setAmount(item.getAmount() + toAdd);
                        items[k] = null;
                    } else {
                        item.setAmount(item.getAmount() + space);
                        item2.setAmount(toAdd - space);
                    }
                }
            }
        }
        final List<ItemStack> result = new ArrayList<>(items.length);
        for (final ItemStack item : items) {
            if ((item == null) || (item.getType() == Material.AIR)) {
                continue;
            }
            result.add(item);
        }
        return result.toArray(new ItemStack[0]);
    }

}