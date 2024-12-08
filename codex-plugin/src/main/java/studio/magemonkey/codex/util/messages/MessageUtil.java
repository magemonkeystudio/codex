package studio.magemonkey.codex.util.messages;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import studio.magemonkey.codex.legacy.placeholder.PlaceholderItem;
import studio.magemonkey.codex.legacy.placeholder.PlaceholderType;
import studio.magemonkey.codex.legacy.riseitem.DarkRiseItem;
import studio.magemonkey.codex.legacy.utils.ComponentUtils;
import studio.magemonkey.codex.util.ItemUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageUtil extends AbstractMessageUtil {
    @Override
    public BaseComponent[] getMessageAsComponent(String path, MessageData... replace) {
        List<BaseComponent> components = new ArrayList<>();

        List<String>                         msgs         = getString(path);
        Map<String, PlaceholderItem<Object>> placeholders = new HashMap<>();
        Map<String, MessageData> data = Arrays.stream(replace)
                .collect(Collectors.toMap(MessageData::getName, messageData -> messageData, (a, b) -> b, HashMap::new));

        Pattern pat = Pattern.compile("\\$<(.*?)>");
        msgs.stream().map(pat::matcher).forEach(mat -> {
            while (mat.find()) {
                PlaceholderItem<Object> it = PlaceholderType.getQualifiedItem(mat.group(1));
                if (it != null) {
                    String id = "$<" + mat.group(1) + ">";
                    placeholders.put(id, it);
                }
            }
        });

        for (String send : msgs) {
            if (send.equalsIgnoreCase("false"))
                continue;

            BaseComponent[] comps = ComponentUtils.fromLegacyText(send);
            for (BaseComponent component : comps) {
                for (Map.Entry<String, PlaceholderItem<Object>> entry : placeholders.entrySet()) {
                    PlaceholderItem<Object> holder = entry.getValue();
                    String                  id     = entry.getKey();
                    if (!component.toPlainText().contains(id))
                        continue;
                    Object translated = holder.apply(data.get(holder.getType().getId()).getObject(), null);
                    if (translated instanceof BaseComponent)
                        ComponentUtils.replace(component, id, (BaseComponent) translated);
                    else
                        ComponentUtils.replace(component, id, translated.toString());
                }
                for (MessageData datum : replace) {
                    String check = "$<" + datum.getName() + ">";
                    if (!component.toPlainText().contains(check))
                        continue;
                    if (datum.getObject() instanceof BaseComponent)
                        ComponentUtils.replace(component, check, (BaseComponent) datum.getObject());
                    else
                        ComponentUtils.replace(component, check, datum.getObject().toString());
                }
                components.add(component);
            }
            if (msgs.size() > 1)
                components.add(new TextComponent("\n"));
        }

        return components.toArray(new BaseComponent[0]);
    }

    @Override
    public String getMessageAsString(String path, final String def, boolean stripColor, MessageData... data) {
        if (!messages.containsKey(path))
            path = def;
        BaseComponent[] strs = getMessageAsComponent(path, data);

        StringBuilder bob = new StringBuilder();

        for (BaseComponent str : strs) {
            bob.append(str.toLegacyText());
        }

        return stripColor ? ChatColor.stripColor(bob.toString()) : bob.toString();
    }

    private List<String> getString(String key) {
        List<String> ret = new ArrayList<>();
        if (!messages.containsKey(key)) {
            ret.add(key);
            return ret;
        }


        Object msg = messages.get(key);
        if (msg == null) {
            ret.add(key);
        } else {
            try {
                ret.addAll(((List<String>) msg));
            } catch (ClassCastException e) {
                ret.add(msg.toString());
            }
        }

        return ItemUtils.fixColors(ret);
    }

    public static String getReplacement(String string) {
        string = "$<" + string + ">";

        return string;
    }

    private static MessageData[] translateSpecial(MessageData... data) {
        List<MessageData> rep = new ArrayList<>();
        for (MessageData md : data) {
            if (md.getName().equalsIgnoreCase("item")) {
                DarkRiseItem it = (DarkRiseItem) md.getObject();
                rep.add(new MessageData("riseItem.name", it.getName()));
                rep.add(new MessageData("riseItem.id", it.getId()));
            } else
                rep.add(md);
        }

        return rep.toArray(new MessageData[0]);
    }
}
