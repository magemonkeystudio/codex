package studio.magemonkey.codex.mccore.util;

public class TextFormatter {
    /**
     * Formats text into individual words
     * (e.g. This Would Be A Result)
     *
     * @param string string to format
     * @return formatted string
     */
    public static String format(String string) {
        if (string == null || string.isEmpty())
            return string;

        String[] pieces = string.split("[ _]");
        StringBuilder result =
                new StringBuilder(pieces[0].substring(0, 1).toUpperCase() + pieces[0].substring(1).toLowerCase());
        for (int i = 1; i < pieces.length; i++) {
            result.append(" ")
                    .append(pieces[i].substring(0, 1).toUpperCase())
                    .append(pieces[i].substring(1).toLowerCase());
        }
        return result.toString();
    }
}
