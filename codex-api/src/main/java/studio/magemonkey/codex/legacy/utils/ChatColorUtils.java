package studio.magemonkey.codex.legacy.utils;

import com.google.common.collect.Sets;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Set;
import java.util.regex.Pattern;

public class ChatColorUtils {
    public static final  char           COLOR_CHAR                   = '\u00A7';
    public static final  char           DEFAULT_ALTERNATE_COLOR_CHAR = '&';
    private static final Pattern        STRIP_COLOR_PATTERN;
    private static final Set<ChatColor> styles                       = Sets.newHashSet(ChatColor.UNDERLINE,
            ChatColor.BOLD,
            ChatColor.STRIKETHROUGH,
            ChatColor.MAGIC,
            ChatColor.ITALIC);

    public static ChatColor getByChar(final char code) {
        return ChatColor.getByChar(code);
    }

    public static String stripColor(final CharSequence input) {
        if (input == null) {
            return null;
        }
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static BaseComponent[] translateAlternateColorCodes(final char altColorChar, final String textToTranslate) {
        return ComponentUtils.fromLegacyText(translateAlternateColorCodesInString(altColorChar, textToTranslate));
    }

    public static BaseComponent[] translateAlternateColorCodes(final String textToTranslate) {
        return ComponentUtils.fromLegacyText(translateAlternateColorCodesInString(DEFAULT_ALTERNATE_COLOR_CHAR,
                textToTranslate));
    }

    public static String translateAlternateColorCodesInString(final char altColorChar, final String textToTranslate) {
        final char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < (b.length - 1); i++) {
            if ((b[i] == altColorChar) && ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[(i + 1)]) > -1)) {
                b[i] = COLOR_CHAR;
                b[(i + 1)] = Character.toLowerCase(b[(i + 1)]);
            }
        }
        return new String(b);
    }

    public static String translateAlternateColorCodesInString(final String textToTranslate) {
        return translateAlternateColorCodesInString(DEFAULT_ALTERNATE_COLOR_CHAR, textToTranslate);
    }

    public static String removeColorCodesInString(final char altColorChar, final String textToTranslate) {
        final char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < (b.length - 1); i++) {
            if ((b[i] == COLOR_CHAR) && ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[(i + 1)]) > -1)) {
                b[i] = altColorChar;
                b[(i + 1)] = Character.toLowerCase(b[(i + 1)]);
            }
        }
        return new String(b);
    }

    public static String getLastColors(final CharSequence input) {
        String    result = "";
        final int length = input.length();
        for (int index = length - 1; index > -1; index--) {
            final char section = input.charAt(index);
            if ((section == COLOR_CHAR) && (index < (length - 1))) {
                final char      c     = input.charAt(index + 1);
                final ChatColor color = getByChar(c);
                if (color != null) {
                    result = color.toString() + result;
                    if ((!styles.contains(color)) || (color.equals(ChatColor.RESET))) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    static {
        STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-OR]");

    }
}
