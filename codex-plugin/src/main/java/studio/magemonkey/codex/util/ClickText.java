package studio.magemonkey.codex.util;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.core.Version;
import studio.magemonkey.codex.util.constants.JStrings;

import java.util.*;

/**
 * @author Fogus® Multimedia
 */
public class ClickText {

    private final String                 text;
    private final Map<String, ClickWord> replacers;
    private       ComponentBuilder       builder;

    public ClickText(@NotNull String text) {
        this.text = StringUT.color(text);
        this.builder = new ComponentBuilder("");
        this.replacers = new HashMap<>();
    }

    @NotNull
    public ClickWord createFullPlaceholder() {
        ClickWord clickWord = new ClickWord(this.text);
        this.replacers.put(JStrings.MASK_ANY, clickWord);
        return clickWord;
    }

    @NotNull
    public ClickWord createPlaceholder(@NotNull String placeholder, @NotNull String text) {
        ClickWord clickWord = new ClickWord(text);
        this.replacers.put(placeholder, clickWord);
        return clickWord;
    }

    @NotNull
    private BaseComponent[] build(@NotNull String line) {
        this.builder = new ComponentBuilder("");

        // Support for full line the single ClickText
        ClickWord full = this.replacers.get(JStrings.MASK_ANY);
        if (full != null) {
            this.builder.append(full.build());//.event(full.getClickEvent()).event(full.getHoverEvent());
            return this.builder.create();
        }

        List<String> pieces = new LinkedList<>(Arrays.asList(line));
        for (String s : this.replacers.keySet()) {
            List<String> tmp = new LinkedList<>();
            for (String piece : pieces) {
                if (!piece.contains(s)) {
                    tmp.add(piece);
                    continue;
                }

                String[] split = piece.split(s);
                for (int i = 0; i < split.length; i++) {
                    tmp.add(split[i]);
                    if (piece.endsWith(s) || i < split.length - 1)
                        tmp.add(s);
                }
            }

            pieces = new ArrayList<>(tmp);
        }

        for (String word : pieces) {
            // Get text Json data if it present.
            Optional<String> optWord = this.replacers.keySet().stream()
                    .filter(holder -> word.contains(holder)).findFirst();
            ClickWord clickWord = optWord.isPresent() ? this.replacers.get(optWord.get()) : null;

            if (clickWord != null) {
                this.builder.append(clickWord.build(), ComponentBuilder.FormatRetention.NONE);
            } else {
                this.builder.append(TextComponent.fromLegacyText(word), ComponentBuilder.FormatRetention.NONE);
            }
        }
        return this.builder.create();
    }

    public void send(@NotNull CommandSender sender) {
        if (sender instanceof Player) {
            for (String line : this.text.split("\n")) {
                ((Player) sender).spigot().sendMessage(this.build(line));
            }
        }
    }

    public void send(@NotNull Set<Player> list) {
        for (String line : this.text.split("\n")) {
            BaseComponent[] jsonText = this.build(line);
            for (Player p : list) {
                p.spigot().sendMessage(jsonText);
            }
        }
    }

    public static class ClickWord {

        private final String     text;
        public        HoverEvent hover;
        public        ClickEvent click;

        public ClickWord(@NotNull String text) {
            this.text = StringUT.color(text);
        }

        @NotNull
        public String getText() {
            return this.text;
        }

        @NotNull
        public HoverEvent getHoverEvent() {
            return this.hover;
        }

        @NotNull
        public ClickEvent getClickEvent() {
            return this.click;
        }

        @NotNull
        public ClickWord hint(@NotNull String text) {
            return this.hint(text.split("\n"));
        }

        @NotNull
        public ClickWord hint(@NotNull List<String> text) {
            return this.hint(text.toArray(new String[text.size()]));
        }

        @SuppressWarnings("deprecation")
        @NotNull
        public ClickWord hint(@NotNull String... text) {
            StringBuilder textBuilder = new StringBuilder();
            for (String line : text) {
                if (textBuilder.length() > 0) {
                    textBuilder.append("\n");
                }
                textBuilder.append(line);
            }

            BaseComponent[] base = TextComponent.fromLegacyText(StringUT.color(textBuilder.toString()));
            this.hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(base)});
            return this;
        }

        @SuppressWarnings("deprecation")
        @NotNull
        public ClickWord showItem(@NotNull ItemStack item) {
            if (Version.CURRENT.isAtLeast(Version.V1_20_R4)) {
                String nbt = String.format("{\"id\":\"%s\",\"count\":%d,\"components\": %s}",
                        item.getType().getKey().getKey(),
                        item.getAmount(),
                        item.getItemMeta() != null ? item.getItemMeta().getAsString() : "{}");
                this.hover = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(nbt)});
            } else if (Version.CURRENT.isAtLeast(Version.V1_18_R2)) {
                this.hover = new HoverEvent(HoverEvent.Action.SHOW_ITEM,
                        new Item(item.getType().getKey().getKey().toString(),
                                item.getAmount(),
                                ItemTag.ofNbt(item.getItemMeta().getAsString())));
            } else {
                // 1.16.5 - 1.18.1
                // We have to serialize the nbt from the item stack by hand
                this.hover = new HoverEvent(HoverEvent.Action.SHOW_ITEM, toBase(item));
            }
            return this;
        }

        private BaseComponent[] toBase(@NotNull ItemStack item) {
            String json = CodexEngine.get().getVersionManager().getNms().toJson(item);
            if (json != null) {
                return TextComponent.fromLegacyText(json);
            }

            return new BaseComponent[0];
        }

        @Deprecated
        public void showEntity(@NotNull String json) {
            this.hover = new HoverEvent(HoverEvent.Action.SHOW_ENTITY, toBase(json));
        }

        @Deprecated
        public void achievement(@NotNull String id) {
            this.hover = new HoverEvent(HoverEvent.Action.SHOW_ACHIEVEMENT, toBase(StringUT.colorOff(id)));
        }

        //

        @NotNull
        public ClickWord execCmd(@NotNull String cmd) {
            this.click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd);
            return this;
        }

        @NotNull
        public ClickWord suggCmd(@NotNull String cmd) {
            this.click = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd);
            return this;
        }

        @NotNull
        public ClickWord url(@NotNull String url) {
            this.click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
            return this;
        }

        @NotNull
        public ClickWord file(@NotNull String path) {
            this.click = new ClickEvent(ClickEvent.Action.OPEN_FILE, path);
            return this;
        }

        @Deprecated
        public void page(int page) {
            this.click = new ClickEvent(ClickEvent.Action.CHANGE_PAGE, String.valueOf(page));
        }

        //

        @NotNull
        public TextComponent build() {
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(this.text));
            if (this.hover != null) {
                component.setHoverEvent(this.hover);
            }
            if (this.click != null) {
                component.setClickEvent(this.click);
            }
            return component;
        }

        @NotNull
        @Deprecated
        private BaseComponent[] toBase(@NotNull String text) {
            return new BaseComponent[]{new TextComponent(text)};
        }
    }
}
