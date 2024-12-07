package studio.magemonkey.codex.util.messages;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.bungee.BungeeUtil;
import studio.magemonkey.codex.legacy.placeholder.PlaceholderItem;
import studio.magemonkey.codex.legacy.placeholder.PlaceholderType;
import studio.magemonkey.codex.legacy.riseitem.DarkRiseItem;
import studio.magemonkey.codex.legacy.utils.ComponentUtils;
import studio.magemonkey.codex.util.ItemUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageUtil {
    public static Map<String, Object> messages = new HashMap<>();

    public static void load(FileConfiguration config, Plugin plugin) {
        reload(config, plugin);
    }

    public static void reload(FileConfiguration config, Plugin plugin) {
        Map<String, Object> temp = getMessages(config);
        for (String s : temp.keySet()) {
            messages.remove(s);
        }
        messages.putAll(temp);
        CodexEngine.get().getLogger().info("Mapped all language data for " + plugin.getName() + ".");
    }

    public static Map<String, Object> getMessages(@NotNull ConfigurationSection section) {
        Map<String, Object> temp = new HashMap<>();

        for (String entry : section.getValues(false).keySet()) {
            String id = section.getCurrentPath().trim().isEmpty() ? entry : section.getCurrentPath() + "." + entry;
            if (section.get(entry) instanceof ConfigurationSection) {
                temp.putAll(getMessages((ConfigurationSection) section.get(entry)));
            } else if (section.get(entry) instanceof ArrayList) {
                temp.put(id, section.get(entry));
            } else {
                if (section.get(entry).toString().contains("\n")) {
                    List<String> array = new ArrayList<>(Arrays.asList(section.get(entry).toString().split("\n")));
                    temp.put(id, array);
                } else
                    temp.put(id, section.get(entry).toString());
            }
        }

        return temp;
    }

    public static void sendMessage(CommandSender player, BaseComponent... comps) {
        List<BaseComponent> send = new ArrayList<>();
        for (BaseComponent component : comps) {
            if (component instanceof TextComponent && ((TextComponent) component).getText().equalsIgnoreCase("\n")) {
                player.spigot().sendMessage(send.toArray(new BaseComponent[0]));
                send.clear();
            } else
                send.add(component);
        }
        if (!send.isEmpty())
            player.spigot().sendMessage(send.toArray(new BaseComponent[0]));
    }

    public static void sendMessage(String key, CommandSender player, MessageData... replace) {
        BaseComponent[] comps = getMessageAsComponent(key, replace);
        sendMessage(player, comps);

/*        for (String send : msgs) {
            if (send.equalsIgnoreCase("false"))
                continue;
            boolean asComponent = false;
            String toSend = send;
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            for (MessageData messageData : replace) {
                String rep = getReplacement(messageData.getName());
                toSend = toSend.replace(rep, "=!=" + messageData.getName() + "=!=");
                if (messageData.getObject() instanceof BaseComponent && send.contains(rep))
                    asComponent = true;

                if (asComponent)
                    map.put(messageData.getName(), messageData.getObject());
                else
                    send = send.replace(rep, messageData.getObject().toString());
//                if (messageData.getObject() instanceof BaseComponent) {
//                    asComponent = true;
//                    base.addExtra((BaseComponent) messageData.getObject());
//                } else {
//                    if (asComponent)
//                        base.addExtra(send.replace(messageData.getName(), messageData.getObject().toString()));
//                    else
//                        send = send.replace(messageData.getName(), messageData.getObject().toString());
//                }
            }

            ArrayList<BaseComponent> comps = new ArrayList<>();
            if (asComponent) {
                for (String st : toSend.split("=!=")) {
                    if (map.get(st) == null)
                        continue;
                    if (map.get(st) instanceof BaseComponent)
                        comps.add((BaseComponent) map.get(st));
                    else
                        comps.add(new TextComponent(map.get(st).toString()));
                }
                player.spigot().sendMessage(comps.toArray(new BaseComponent[0]));
            } else
                player.sendMessage(send);
        }*/
    }

    public static List<String> getString(String key) {
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
                ((List<String>) msg).forEach(e -> ret.add(e));
            } catch (ClassCastException e) {
                ret.add(msg.toString());
            }
        }

        return ItemUtils.fixColors(ret);
    }

    public static BaseComponent[] getMessageAsComponent(String path, MessageData... replace) {
//        if (!messages.containsKey(path))
//            return new BaseComponent[]{new TextComponent(path)};

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
//            for (String st : RiseStringUtils.splitAround(send, placeholders.keySet())) {
//
//                TextComponent component = new TextComponent(st);
//
//                for (Map.Entry<String, PlaceholderItem<Object>> entry : placeholders.entrySet()) {
//                    PlaceholderItem<Object> holder = entry.getValue();
//                    String id = entry.getKey();
//                    if (!component.toPlainText().contains(id))
//                        continue;
//
//                    Object translated = holder.apply(data.get(holder.getType().getId()).getObject(), null);
//
//                    if (translated instanceof BaseComponent)
//                        ComponentUtils.replace(component, id, (BaseComponent) translated);
//                    else
//                        ComponentUtils.replace(component, id, translated.toString());
//                }
//
//                for (MessageData datum : replace) {
//                    String check = "$<" + datum.getName() + ">";
//                    if (!component.toPlainText().contains(check))
//                        continue;
//
//                    if (datum.getObject() instanceof BaseComponent)
//                        ComponentUtils.replace(component, check, (BaseComponent) datum.getObject());
//                    else
//                        ComponentUtils.replace(component, check, datum.getObject().toString());
//                }
//                components.add(component);
//            }
            if (msgs.size() > 1)
                components.add(new TextComponent("\n"));
        }

        return components.toArray(new BaseComponent[0]);


//        String send = ItemUtils.fixColors(messages.get(path).toString());
//        ArrayList<BaseComponent> ret = new ArrayList<>();
//        HashMap<String, BaseComponent> rs = new HashMap<>();
//
//        for (MessageData datum : data) {
//            if (datum.getName().equalsIgnoreCase("item")) {
//                TextComponent comp = new TextComponent(((DarkRiseItem) datum.getObject()).getName());//.getItemMeta().getDisplayName());
//                comp.setHoverEvent(NMSPlayerUtils.convert(((DarkRiseItem) datum.getObject()).getItem()));
//                rs.put("$<riseItem.name>", comp);
//                rs.put("$<riseItem.id>", new TextComponent(((DarkRiseItem) datum.getObject()).getId()));
//            } else {
//                rs.put(getReplacement(datum.getName()), new TextComponent(String.valueOf(datum.getObject())));
//            }
//        }
//
//        String[] builder = new String[]{send};
//        for (String replace : rs.keySet()) {
//            builder = RiseStringUtils.splitAround(builder, replace);
//        }

        //        ArrayList<String> builder = new ArrayList<>();
        //        String[] split = {send};
        //        for (String replace : rs.keySet()) {
        //            System.out.println("Checking for \"" + replace + "\"");
        //            for (int j = 0; j < split.length; j++) {
        //                String pre = split[j];
        //                String[] post = pre.split(replace);
        //                if (post.length > 1) {
        //                    for (int i = 0; i < post.length; i++) {
        //                        String sp = post[i];
        //                        builder.add(sp);
        //                        if (i + 1 < post.length)
        //                            builder.add(replace);
        //                    }
        //                    split = builder.toArray(split);
        //                    builder.clear();
        //                }
        //            }
        //            System.out.println(StringUtils.join(split, ", "));
        //        }

//        for (String sp : builder) {
//            if (rs.keySet().contains(sp)) {
//                ret.add(rs.get(sp));
//            } else
//                ret.add(new TextComponent(sp));
//        }
//
//        return ret.toArray(new BaseComponent[0]);
    }

    public static String getMessageAsString(String path, final String def, MessageData... data) {
        return getMessageAsString(path, def, false, data);
    }

    public static String getMessageAsString(String path, final String def, boolean stripColor, MessageData... data) {
        if (!messages.containsKey(path))
            path = def;
        BaseComponent[] strs = getMessageAsComponent(path, data);

        StringBuilder bob = new StringBuilder();

        for (BaseComponent str : strs) {
            bob.append(str.toLegacyText());
        }

        return stripColor ? ChatColor.stripColor(bob.toString()) : bob.toString();
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

    public static void broadcastMessage(String key, MessageData... data) {
        Bukkit.getServer().getOnlinePlayers().forEach(online -> sendMessage(key, online, data));
        sendMessage(key, Bukkit.getConsoleSender(), data);
    }

    public static void broadcastNetworkMessage(String key, MessageData... data) {
        if (!BungeeUtil.isBungee()) broadcastMessage(key, data);
        else BungeeUtil.broadcastMessage(getMessageAsString(key, key, false, data));
    }
}
