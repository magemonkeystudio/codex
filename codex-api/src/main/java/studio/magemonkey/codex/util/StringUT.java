package studio.magemonkey.codex.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.api.VersionManager;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUT {

    public static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");

    @NotNull
    public static String oneSpace(@NotNull String str) {
        return str.trim().replaceAll("\\s+", " ");
    }

    @NotNull
    public static String colorSensitiveStrip(@NotNull String str) {
        int           length = str.length();
        StringBuilder result = new StringBuilder();

        int i = 0;
        for (; i < length; i++) {
            char a = str.charAt(i);
            if (a == '§') {
                result.append(a);
                i++;
                if (i < length) {
                    char b = str.charAt(i);
                    if (ChatColor.getByChar(b) == null) {
                        break;
                    } else {
                        result.append(b);
                    }
                }
            } else if (a != ' ') {
                break;
            }
        }

        int leadingLength = result.length();

        int j = length - 1;
        for (; j > i; j--) {
            char a = str.charAt(j);
            if (a == ' ') continue;
            result.insert(leadingLength, a);
            j--;
            if (j > i) {
                char b = str.charAt(j);
                if (b == '§' && ChatColor.getByChar(a) != null) {
                    result.insert(leadingLength, b);
                } else {
                    break;
                }
            }
        }

        result.insert(leadingLength, str.substring(i, j + 1));
        return result.toString();
    }

    @NotNull
    public static String noSpace(@NotNull String str) {
        return str.trim().replaceAll("\\s+", "");
    }

    @NotNull
    public static String color(@Nullable String str) {
        if (str == null || str.isBlank()) return "";

        str = colorHex(str);
        return ChatColor.translateAlternateColorCodes('&', colorFix(str));
    }

    /**
     * Removes multiple color codes that are 'color of color'. Example: '&amp;a&amp;b&amp;cText' -> '&amp;cText'.
     *
     * @param str String to fix.
     * @return A string with a proper color codes formatting.
     */
    @NotNull
    public static String colorFix(@NotNull String str) {
        return VersionManager.getNms().fixColors(str);
    }

    @NotNull
    public static String colorHex(@NotNull String str) {
        Matcher       matcher = HEX_PATTERN.matcher(str);
        StringBuilder buffer  = new StringBuilder(str.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
                    + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
                    + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
                    + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }

    @NotNull
    public static String colorHexRaw(@NotNull String str) {
        StringBuffer buffer = new StringBuffer(str);

        int index;
        while ((index = buffer.toString().indexOf(ChatColor.COLOR_CHAR + "x")) >= 0) {
            int count = 0;
            buffer = buffer.replace(index, index + 2, "#");

            for (int point = index + 1; count < 6; point += 1) {
                buffer = buffer.deleteCharAt(point);
                count++;
            }
        }

        return buffer.toString();
    }

    @NotNull
    public static String colorRaw(@NotNull String str) {
        return str.replace(ChatColor.COLOR_CHAR, '&');
    }

    @NotNull
    public static String colorOff(@NotNull String str) {
        String off = ChatColor.stripColor(str);
        return off == null ? "" : off;
    }

    @NotNull
    public static List<String> color(@NotNull List<String> list) {
        list.replaceAll(StringUT::color);
        return list;
    }

    @NotNull
    public static Set<String> color(@NotNull Set<String> list) {
        return new HashSet<>(StringUT.color(new ArrayList<>(list)));
    }

    @NotNull
    public static List<String> replace(@NotNull String placeholder, List<String> r, String... orig) {
        return replace(List.of(orig), placeholder, r);
    }

    @NotNull
    public static List<String> replace(@NotNull List<String> orig, @NotNull String placeholder, List<String> r) {
        if (r.isEmpty()) {
            r = List.of("[]");
        }
        orig = new ArrayList<>(orig);
        for (int i = 0, loreSize = orig.size(); i < loreSize; i++) {
            String line = orig.get(i);
            int    pos  = line.indexOf(placeholder);
            if (pos < 0) {
                continue;
            }
            String format = StringUT.getColor(line.substring(0, pos));
            orig.set(i, line.substring(0, pos) + r.get(0));
            for (int j = 1, size = r.size(); j < size; j++) {
                i++;
                loreSize++;
                orig.add(i, format + r.get(j));
            }
            orig.set(i, orig.get(i) + line.substring(pos + placeholder.length()));
        }
        return orig;
    }

    @NotNull
    public static List<String> replace(@NotNull List<String> orig, @NotNull String placeholder, String r) {
        return replace(orig, placeholder, Collections.singletonList(r));
    }

    public static double getDouble(@NotNull String input, double def) {
        return getDouble(input, def, false);
    }

    public static double getDouble(@NotNull String input, double def, boolean allowNega) {
        try {
            double amount = Double.parseDouble(input);
            if (amount < 0.0 && !allowNega) {
                throw new NumberFormatException();
            }
            return amount;
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    public static int getInteger(@NotNull String input, int def) {
        return getInteger(input, def, false);
    }

    public static int getInteger(@NotNull String input, int def, boolean nega) {
        return (int) getDouble(input, def, nega);
    }

    public static int[] getIntArray(@NotNull String str) {
        String[] raw   = str.replaceAll("\\s", ",").replace(",,", ",").split(",");
        int[]    slots = new int[raw.length];
        for (int i = 0; i < raw.length; i++) {
            try {
                slots[i] = Integer.parseInt(raw[i].trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return slots;
    }

    @NotNull
    public static String capitalizeFully(@NotNull String str) {
        return WordUtils.capitalizeFully(str);
    }

    @NotNull
    public static String capitalizeFirstLetter(@NotNull String original) {
        if (original.isEmpty()) return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    @NotNull
    public static List<String> getByFirstLetters(@NotNull String arg, @NotNull List<String> source) {
        List<String> ret  = new ArrayList<>();
        List<String> sugg = new ArrayList<>(source);
        StringUtil.copyPartialMatches(arg, sugg, ret);
        Collections.sort(ret);
        return ret;
    }

    @NotNull
    public static String extractCommandName(@NotNull String cmd) {
        String   cmdFull      = colorOff(cmd).split(" ")[0];
        String   cmdName      = cmdFull.replace("/", "").replace("\\/", "");
        String[] pluginPrefix = cmdName.split(":");
        if (pluginPrefix.length == 2) {
            cmdName = pluginPrefix[1];
        }

        return cmdName;
    }

    public static boolean isCustomBoolean(@NotNull String str) {
        if (str.equalsIgnoreCase("0") || str.equalsIgnoreCase("off")) {
            return true;
        }
        if (str.equalsIgnoreCase("1") || str.equalsIgnoreCase("on")) {
            return true;
        }
        if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")) {
            return true;
        }
        return str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("no");
    }

    public static boolean parseCustomBoolean(@NotNull String str) {
        if (str.equalsIgnoreCase("0") || str.equalsIgnoreCase("off")
                || str.equals("no")) {
            return false;
        }
        if (str.equalsIgnoreCase("1") || str.equalsIgnoreCase("on")
                || str.equals("yes")) {
            return true;
        }
        return Boolean.parseBoolean(str);
    }

    @NotNull
    public static String c(@NotNull String s) {
        char[] ch  = s.toCharArray();
        char[] out = new char[ch.length * 2];
        int    i   = 0;
        for (char c : ch) {
            int orig = Character.getNumericValue(c);
            int min;
            int max;

            char cas;
            if (Character.isUpperCase(c)) {
                min = Character.getNumericValue('A');
                max = Character.getNumericValue('Z');
                cas = 'q';
            } else {
                min = Character.getNumericValue('a');
                max = Character.getNumericValue('z');
                cas = 'p';
            }

            int  pick = min + (max - orig);
            char get  = Character.forDigit(pick, Character.MAX_RADIX);
            out[i] = get;
            out[++i] = cas;
            i++;
        }
        return String.valueOf(out);
    }

    @NotNull
    public static String d(@NotNull String s) {
        char[] ch  = s.toCharArray();
        char[] dec = new char[ch.length / 2];
        for (int i = 0; i < ch.length; i = i + 2) {
            int     j      = i;
            char    letter = ch[j];
            char    cas    = ch[++j];
            boolean upper  = cas == 'q';

            int max;
            int min;
            if (upper) {
                min = Character.getNumericValue('A');
                max = Character.getNumericValue('Z');
            } else {
                min = Character.getNumericValue('a');
                max = Character.getNumericValue('z');
            }

            int  orig = max - Character.getNumericValue(letter) + min;
            char get  = Character.forDigit(orig, Character.MAX_RADIX);
            if (upper) get = Character.toUpperCase(get);

            dec[i / 2] = get;
        }
        return String.valueOf(dec);
    }

    @NotNull
    public static String getColor(String str) {
        StringBuilder builder = new StringBuilder();
        int           j       = 0;
        while (true) {
            int t = str.indexOf('§', j);
            j = str.indexOf('&', j);
            if (t >= 0 && (j < 0 || t < j)) {
                j = t;
            }
            if (j >= 0) {
                j++;
                if (j >= str.length()) {
                    break;
                }
                ChatColor color = ChatColor.getByChar(str.charAt(j));
                if (color != null) {
                    builder.append(color);
                }
            } else {
                break;
            }
        }
        return builder.toString();
    }

    public static List<String> wrap(String value, int maxLength) {
        List<String>  splitValue = new ArrayList<>();
        StringBuilder color      = new StringBuilder();
        while (ChatColor.stripColor(value).length() > maxLength) {
            int i = value.lastIndexOf(' ', maxLength);
            if (i < 0) {
                i = maxLength;
            }
            String first = value.substring(0, i);
            color.append(getColor(first));
            splitValue.add(first);
            value = color + value.substring(i);
        }
        splitValue.add(value);
        return splitValue;
    }

    public static List<String> wrap(List<String> value, int maxLenght) {
        List<String> splitValue = new ArrayList<>();
        for (String aValue : value) {
            splitValue.addAll(wrap(aValue, maxLenght));
        }
        return splitValue;
    }

    public static BaseComponent parseJson(String rawJson) {
        BaseComponent[] array = ComponentSerializer.parse(rawJson);
        if (array.length == 1) {
            return array[0];
        } else {
            BaseComponent component = new TextComponent();
            for (BaseComponent baseComponent : array) {
                component.addExtra(baseComponent);
            }
            return component;
        }
    }
}
