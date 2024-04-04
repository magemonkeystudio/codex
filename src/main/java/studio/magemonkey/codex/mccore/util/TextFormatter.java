package studio.magemonkey.codex.mccore.util;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class TextFormatter {

    private static final String COLOR_REGEX = "([0-9a-fk-orA-FL-OR])";

    /**
     * Formats text into individual words
     * (e.g. This Would Be A Result)
     *
     * @param string string to format
     * @return formatted string
     */
    public static String format(String string) {
        if (string == null || string.length() == 0)
            return string;

        String[] pieces = string.split("[ _]");
        String   result = pieces[0].substring(0, 1).toUpperCase() + pieces[0].substring(1).toLowerCase();
        for (int i = 1; i < pieces.length; i++) {
            result += " " + pieces[i].substring(0, 1).toUpperCase() + pieces[i].substring(1).toLowerCase();
        }
        return result;
    }

    /**
     * Colors a string using &amp; as the color indicator
     *
     * @param string string to color
     * @return colored string
     */
    public static String colorString(String string) {
        return colorString(string, '&');
    }

    /**
     * Colors a string using the given color indicator
     *
     * @param string string to color
     * @param token  color indicator
     * @return colored string
     */
    public static String colorString(String string, char token) {
        if (string == null) return null;
        return string.replaceAll(token + COLOR_REGEX, ChatColor.COLOR_CHAR + "$1");
    }

    /**
     * Colors a string builder using &amp; as the color indicator
     *
     * @param sb string builder to color
     */
    public static void colorString(StringBuilder sb) {
        colorString(sb, '&');
    }

    /**
     * Colors a string builder using the given color indicator
     *
     * @param sb    string builder to color
     * @param token color indicator
     */
    public static void colorString(StringBuilder sb, char token) {
        if (sb == null) return;
        String t     = token + "";
        int    index = sb.indexOf(t);
        while (index >= 0 && index < sb.length() - 1) {
            ChatColor color = ChatColor.getByChar(sb.charAt(index + 1));
            if (color != null) {
                sb.setCharAt(index, ChatColor.COLOR_CHAR);
            }
            index = sb.indexOf(t, index + 1);
        }
    }

    /**
     * Colors a list of strings using &amp; as the color indicator
     *
     * @param list string list
     * @return colored string list
     */
    public static List<String> colorStringList(List<String> list) {
        return colorStringList(list, '&');
    }

    /**
     * Colors a list of strings with the given color indicator
     *
     * @param list  string list
     * @param token color indicator
     * @return colored string list
     */
    public static List<String> colorStringList(List<String> list, char token) {
        return list.stream().map(string -> colorString(string, token))
                .collect(Collectors.toList());
    }

}
