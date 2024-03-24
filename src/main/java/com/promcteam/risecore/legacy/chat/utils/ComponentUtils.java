package com.promcteam.risecore.legacy.chat.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.chat.TextComponentSerializer;
import net.md_5.bungee.chat.TranslatableComponentSerializer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.md_5.bungee.api.ChatColor.*;

public final class ComponentUtils {
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(BaseComponent.class, new ComponentSerializer()).registerTypeAdapter(TextComponent.class, new TextComponentSerializer()).registerTypeAdapter(TranslatableComponent.class, new TranslatableComponentSerializer()).create();
    private static final Pattern url = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$");
    private static final Pattern format = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    private ComponentUtils() {
    }

    public static BaseComponent[] parse(String json) {
        return json.startsWith("[") ? gson.fromJson(json, BaseComponent[].class) : new BaseComponent[]{gson.fromJson(json, BaseComponent.class)};
    }

    public static boolean isEmpty(BaseComponent component) {
        List<BaseComponent> extra = component.getExtra();
        if (extra != null) {
            for (Iterator<BaseComponent> iterator = extra.iterator(); iterator.hasNext(); ) {
                if (isEmpty(iterator.next())) {
                    iterator.remove();
                }
            }
        }
        boolean emptyExtra = (extra == null) || extra.isEmpty();
        if (component instanceof TextComponent) {
            String text = ((TextComponent) component).getText();
            return emptyExtra && ((text == null) || text.isEmpty());
        }
        if (component instanceof TranslatableComponent) {
            String translate = ((TranslatableComponent) component).getTranslate();
            return emptyExtra && ((translate == null) || translate.isEmpty());
        }
        return emptyExtra;
    }

    public static BaseComponent[] fromLegacyText(String message) {
        List<BaseComponent> components = new ArrayList<>(20);
        StringBuilder builder = new StringBuilder();
        TextComponent component = new TextComponent("");
        TextComponent builderComponent = new TextComponent("");
        Matcher matcher = url.matcher(message);

        for (int i = 0; i < message.length(); ++i) {
            char c = message.charAt(i);
            if (c == '\n') {
                if (builder.length() > 0) {
                    builderComponent.setText(builder.toString());
                    component.addExtra(builderComponent);
                    builderComponent = new TextComponent("");
                    builder = new StringBuilder();
                }
                components.add(component);
                component = new TextComponent("");
                continue;
            }
            if (c == ChatColor.COLOR_CHAR) {
                ++i;
                c = message.charAt(i);
                if ((c >= 'A') && (c <= 'Z')) {
                    c += 32;
                }

                ChatColor pos = ChatColor.getByChar(c);
                if (pos != null) {
                    if (builder.length() > 0) {
                        builderComponent.setText(builder.toString());
                        component.addExtra(builderComponent);
                        builderComponent = new TextComponent("");
                        builder = new StringBuilder();
                    }

                    if (pos.equals(BOLD))
                        builderComponent.setBold(Boolean.TRUE);
                    else if (pos.equals(ITALIC))
                        builderComponent.setItalic(Boolean.TRUE);
                    else if (pos.equals(UNDERLINE))
                        builderComponent.setUnderlined(Boolean.TRUE);
                    else if (pos.equals(STRIKETHROUGH))
                        builderComponent.setStrikethrough(Boolean.TRUE);
                    else if (pos.equals(MAGIC))
                        builderComponent.setObfuscated(Boolean.TRUE);
                    else if (pos.equals(RESET))
                        pos = ChatColor.WHITE;
                    else {
                        builderComponent = new TextComponent("");
                        builderComponent.setColor(pos);
                    }
                }
            } else {
                int var10 = message.indexOf(' ', i);
                if (var10 == -1) {
                    var10 = message.length();
                }

                if (matcher.region(i, var10).find()) {
                    if (builder.length() > 0) {
                        builderComponent.setText(builder.toString());
                        component.addExtra(builderComponent);
                        builderComponent = new TextComponent("");
                        builder = new StringBuilder();
                    }

                    String urlString = message.substring(i, var10);
                    builderComponent.setText(urlString);
                    builderComponent.setClickEvent(new ClickEvent(Action.OPEN_URL, urlString.startsWith("http") ? urlString : "http://" + urlString));
                    component.addExtra(builderComponent);
                    builderComponent = new TextComponent("");
                    i += var10 - i - 1;
                } else {
                    builder.append(c);
                }
            }
        }

        if (builder.length() > 0) {
            builderComponent.setText(builder.toString());
            component.addExtra(builderComponent);
        }
        components.add(component);

        return components.toArray(new BaseComponent[components.size()]);
    }

    public static String toString(BaseComponent[] components) {
        return gson.toJson(components);
    }

    public static String toString(BaseComponent components) {
        return gson.toJson(components);
    }

    public static String toPlainText(BaseComponent[] component) {
        if (component.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(100 * component.length);
        sb.append(component[0].toPlainText());
        if (component.length > 1) {
            for (int i = 1; i < component.length; i++) {
                final BaseComponent baseComponent = component[i];
                sb.append('\n');
                sb.append(baseComponent.toPlainText());
            }
        }
        return sb.toString();
    }

    public static String toLegacyText(BaseComponent[] component) {
        if (component.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(100 * component.length);
        sb.append(toLegacyText(component[0]));
        if (component.length > 1) {
            for (int i = 1; i < component.length; i++) {
                final BaseComponent baseComponent = component[i];
                sb.append('\n');
                sb.append(toLegacyText(baseComponent));
            }
        }
        return sb.toString();
    }

    public static String toLegacyText(BaseComponent this_) {
        StringBuilder builder = new StringBuilder();
        toLegacyText(this_, builder);
        return builder.toString();
    }

    static void toLegacyText(BaseComponent this_, StringBuilder builder) {
        toLegacyText(this_, builder, true);
    }

    static void toLegacyText(BaseComponent this_, StringBuilder builder, boolean checkType) {
        if (checkType) {
            if (this_ instanceof TextComponent) {
                toLegacyText((TextComponent) this_, builder);
                return;
            }
            if (this_ instanceof TranslatableComponent) {
                toLegacyText((TranslatableComponent) this_, builder);
                return;
            }
        }
        List<BaseComponent> extra = this_.getExtra();
        if (extra != null) {
            for (final BaseComponent e : extra) {
                toLegacyText(e, builder);
            }
        }

    }

    static void toLegacyText(TranslatableComponent this_, StringBuilder builder) {
        String trans = this_.getTranslate();

        Matcher matcher = format.matcher(trans);
        int position = 0;
        int i = 0;

        while (matcher.find(position)) {
            int pos = matcher.start();
            if (pos != position) {
                addFormat(this_, builder);
                builder.append(trans.substring(position, pos));
            }

            position = matcher.end();
            String formatCode = matcher.group(2);
            switch (formatCode.charAt(0)) {
                case '%':
                    addFormat(this_, builder);
                    builder.append('%');
                    break;
                case 'd':
                case 's':
                    String withIndex = matcher.group(1);
                    toLegacyText(this_.getWith().get((withIndex != null) ? (Integer.parseInt(withIndex) - 1) : i++), builder);
                default:
                    break;
            }
        }

        if (trans.length() != position) {
            addFormat(this_, builder);
            builder.append(trans.substring(position, trans.length()));
        }

        toLegacyText(this_, builder, false);
    }

    static void addFormat(BaseComponent this_, StringBuilder builder) {
        ChatColor colorRaw = this_.getColorRaw();
        if (colorRaw != null) {
            builder.append(this_.getColor());
        }
        if (this_.isBold()) {
            builder.append(BOLD);
        }
        if (this_.isItalic()) {
            builder.append(ITALIC);
        }
        if (this_.isUnderlined()) {
            builder.append(UNDERLINE);
        }
        if (this_.isStrikethrough()) {
            builder.append(ChatColor.STRIKETHROUGH);
        }
        if (this_.isObfuscated()) {
            builder.append(ChatColor.MAGIC);
        }
    }

    static void toLegacyText(TextComponent this_, StringBuilder builder) {
        addFormat(this_, builder);
        String text = this_.getText();
        if (text != null) {
            builder.append(text);
        }
        toLegacyText(this_, builder, false);
    }

    public static boolean canBeLegacy(BaseComponent[] component) {
        for (final BaseComponent baseComponent : component) {
            if (!canBeLegacy(baseComponent)) {
                return false;
            }
        }
        return true;
    }

    public static boolean canBeLegacy(BaseComponent component) {
        if (component.getClickEvent() != null) {
            return false;
        }
        if (component.getHoverEvent() != null) {
            return false;
        }
        final List<BaseComponent> extra = component.getExtra();
        if ((extra == null) || extra.isEmpty()) {
            return true;
        }
        for (final BaseComponent baseComponent : extra) {
            if (!ComponentUtils.canBeLegacy(baseComponent)) {
                return false;
            }
        }
        return true;
    }

    public static BaseComponent[] safeParse(String json) {
        return safeParse(json, false);
    }

    public static BaseComponent[] safeParse(String json, boolean color) {
        try {
            BaseComponent[] parse = ComponentSerializer.parse(json);
        } catch (Exception e) {
        }
        return color ? ChatColorUtils.translateAlternateColorCodes(json) : fromLegacyText(json);
    }

    public static BaseComponent[][] safeParseMulti(String[] json, boolean color) {
        BaseComponent[][] result = new BaseComponent[json.length][];
        for (int i = 0; i < json.length; i++) {
            result[i] = safeParse(json[i], color);
        }
        return result;
    }

    public static int replace(final BaseComponent this_, final String text, final BaseComponent component, final int limit) {
        return replace_(null, this_, text, component, limit);
    }


    public static int replace(final BaseComponent this_, final String text, final BaseComponent component) {
        return replace(this_, text, component, -1);
    }

    public static boolean replaceOnce(final BaseComponent this_, final String text, final BaseComponent component) {
        return replace(this_, text, component, 1) != 1;
    }

    public static int replace(final BaseComponent this_, final String text, final String repl, final int limit) {
        return replace_(null, this_, text, repl, limit);
    }

    public static int replace(final BaseComponent this_, final String text, final String repl) {
        return replace(this_, text, repl, -1);
    }

    public static boolean replaceOnce(final BaseComponent this_, final String text, final String repl) {
        return replace(this_, text, repl, 1) != 1;
    }

    static int replace_(BaseComponent component_, TranslatableComponent this_, final String text, final BaseComponent component, int limit) {
        String translate = this_.getTranslate();
        final int startIndex = translate.indexOf(text);
        if (startIndex != -1) {
            final int endIndex = startIndex + text.length();
            this_.setTranslate(translate.substring(0, startIndex) + toLegacyText(component) + translate.substring(endIndex));
            if (--limit == 0) {
                return 0;
            }
        }
        List<BaseComponent> with = this_.getWith();
        if (with != null) {
            for (final BaseComponent w : with) {
                limit = replace_(this_, w, text, component, limit);
                if (limit == 0) {
                    return 0;
                }
            }
        }
        return replace_(component_, this_, text, component, limit, false);
    }

    static int replace_(BaseComponent component_, TranslatableComponent this_, final String text, final String repl, int limit) {
        String translate = this_.getTranslate();
        final int startIndex = translate.indexOf(text);
        if (startIndex != -1) {
            final int endIndex = startIndex + text.length();
            this_.setTranslate(translate.substring(0, startIndex) + repl + translate.substring(endIndex));
            if (--limit == 0) {
                return 0;
            }
        }
        List<BaseComponent> with = this_.getWith();
        if (with != null) {
            for (final BaseComponent w : with) {
                limit = replace_(this_, w, text, repl, limit);
                if (limit == 0) {
                    return 0;
                }
            }
        }
        return replace_(component_, this_, text, repl, limit, false);
    }

    static int replace_(final BaseComponent component_, final TextComponent this_, final String text, final BaseComponent component, int limit) {
        String thisText = this_.getText();
        if (thisText == null) {
            return replace_(component_, this_, text, component, limit, false);
        }
        final int startIndex = thisText.indexOf(text);
        if (startIndex != -1) {
            final int endIndex = startIndex + text.length();
            final String pre = thisText.substring(0, startIndex);
            final String post = thisText.substring(endIndex);
            this_.setText(pre);
            List<BaseComponent> extra = this_.getExtra();
            if (extra != null) {
                extra.addAll(0, Arrays.asList(component.duplicate(), new TextComponent(post)));
            } else {
                this_.setExtra(extra = new ArrayList<>(2));
                extra.add(component.duplicate());
                extra.add(new TextComponent(post));
            }
            if (--limit == 0) {
                return 0;
            }
        }
        return replace_(component_, this_, text, component, limit, false);
    }

    static int replace_(final BaseComponent component_, final TextComponent this_, final String text, final String repl, int limit) {
        String thisText = this_.getText();
        if (thisText == null) {
            return replace_(component_, this_, text, repl, limit, false);
        }
        final int startIndex = thisText.indexOf(text);
        if (startIndex != -1) {
            final int endIndex = startIndex + text.length();
            this_.setText(thisText.substring(0, startIndex) + repl + thisText.substring(endIndex));
            if (--limit == 0) {
                return 0;
            }
        }
        return replace_(component_, this_, text, repl, limit, false);
    }

    static int replace_(final BaseComponent component_, final BaseComponent this_, final String text, final BaseComponent component, int limit) {
        return replace_(component_, this_, text, component, limit, true);
    }

    static int replace_(final BaseComponent component_, final BaseComponent this_, final String text, final BaseComponent component, int limit, boolean checkType) {
        if (checkType) {
            if (this_ instanceof TextComponent) {
                return replace_(component_, (TextComponent) this_, text, component, limit);
            }
            if (this_ instanceof TranslatableComponent) {
                return replace_(component_, (TranslatableComponent) this_, text, component, limit);
            }
        }
        List<BaseComponent> extra = this_.getExtra();
        if (extra != null) {
            for (final BaseComponent bs : extra) {
                limit = replace_(this_, bs, text, component, limit);
                if (limit == 0) {
                    return 0;
                }
            }
        }
        if (this_.getHoverEvent() != null) {
            limit = replace_(this_, this_.getHoverEvent(), text, component, limit);
            if (limit == 0) {
                return 0;
            }
        }
        if (this_.getClickEvent() != null) {
            limit = replace_(this_, this_.getClickEvent(), text, component, limit);
            if (limit == 0) {
                return 0;
            }
        }
        return limit;
    }

    static int replace_(final BaseComponent component_, final BaseComponent this_, final String text, final String repl, int limit) {
        return replace_(component_, this_, text, repl, limit, true);
    }

    static int replace_(final BaseComponent component_, final BaseComponent this_, final String text, final String repl, int limit, boolean checkType) {
        if (checkType) {
            if (this_ instanceof TextComponent) {
                return replace_(component_, (TextComponent) this_, text, repl, limit);
            }
            if (this_ instanceof TranslatableComponent) {
                return replace_(component_, (TranslatableComponent) this_, text, repl, limit);
            }
        }
        List<BaseComponent> extra = this_.getExtra();
        if (extra != null) {
            for (final BaseComponent bs : extra) {
                limit = replace_(this_, bs, text, repl, limit);
                if (limit == 0) {
                    return 0;
                }
            }
        }
        if (this_.getHoverEvent() != null) {
            limit = replace_(this_, this_.getHoverEvent(), text, repl, limit);
            if (limit == 0) {
                return 0;
            }
        }
        if (this_.getClickEvent() != null) {
            limit = replace_(this_, this_.getClickEvent(), text, repl, limit);
            if (limit == 0) {
                return 0;
            }
        }
        return limit;
    }


    static int replace_(final BaseComponent component_, final ClickEvent this_, final String text, final String repl, int limit) {
        final int startIndex = this_.getValue().indexOf(text);
        if (startIndex != -1) {
            final int endIndex = startIndex + text.length();
            component_.setClickEvent(new ClickEvent(this_.getAction(), this_.getValue().substring(0, startIndex) + repl + this_.getValue().substring(endIndex)));
            if (--limit == 0) {
                return 0;
            }
        }
        return limit;
    }

    static int replace_(final BaseComponent component_, final ClickEvent this_, final String text, final BaseComponent component, int limit) {
        final int startIndex = this_.getValue().indexOf(text);
        if (startIndex != -1) {
            final int endIndex = startIndex + text.length();
            component_.setClickEvent(new ClickEvent(this_.getAction(), this_.getValue().substring(0, startIndex) + toLegacyText(component) + this_.getValue().substring(endIndex)));
            if (--limit == 0) {
                return 0;
            }
        }
        return limit;
    }

    static int replace_(final BaseComponent component_, final HoverEvent this_, final String text, final BaseComponent component, int limit) {
        BaseComponent[] value = this_.getValue();
        if (value != null) {
            for (final BaseComponent bs : value) {
                limit = replace_(null, bs, text, component, limit);
                if (limit == 0) {
                    return 0;
                }
            }
        }
        return limit;
    }


    static int replace_(final BaseComponent component_, final HoverEvent this_, final String text, final String repl, int limit) {
        BaseComponent[] value = this_.getValue();
        if (value != null) {
            for (final BaseComponent bs : value) {
                limit = replace_(null, bs, text, repl, limit);
                if (limit == 0) {
                    return 0;
                }
            }
        }
        return limit;
    }

    public static ClickEvent duplicate(ClickEvent this_) {
        return new ClickEvent(this_.getAction(), this_.getValue());
    }

    public static HoverEvent duplicate(HoverEvent this_) {
        BaseComponent[] value = this_.getValue();
        if (value == null) {
            return new HoverEvent(this_.getAction(), Collections.EMPTY_LIST);
        }
        final BaseComponent[] valueCpy = new BaseComponent[value.length];
        for (int i = 0; i < value.length; i++) {
            valueCpy[i] = value[i].duplicate();
        }
        return new HoverEvent(this_.getAction(), valueCpy);
    }
}
