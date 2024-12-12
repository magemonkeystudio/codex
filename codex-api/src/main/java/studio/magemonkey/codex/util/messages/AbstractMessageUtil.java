package studio.magemonkey.codex.util.messages;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.Codex;
import studio.magemonkey.codex.bungee.BungeeUtil;

import java.util.*;

public abstract class AbstractMessageUtil {
    protected Map<String, Object> messages = new HashMap<>();

    public abstract BaseComponent[] getMessageAsComponent(String path, MessageData... replace);

    public abstract String getMessageAsString(String path, String def, boolean stripColor, MessageData... data);

    public void reload(FileConfiguration config, JavaPlugin plugin) {
        Map<String, Object> temp = getMessages(config);
        for (String s : temp.keySet()) {
            messages.remove(s);
        }
        messages.putAll(temp);
        Codex.info("Mapped all language data for " + plugin.getName() + ".");
    }

    public void load(FileConfiguration config, JavaPlugin plugin) {
        reload(config, plugin);
    }

    private Map<String, Object> getMessages(@NotNull ConfigurationSection section) {
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

    public String getMessageAsString(String path, final String def, MessageData... data) {
        return getMessageAsString(path, def, false, data);
    }

    public void sendMessage(CommandSender player, BaseComponent... comps) {
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

    public void sendMessage(String key, CommandSender player, MessageData... replace) {
        BaseComponent[] comps = getMessageAsComponent(key, replace);
        sendMessage(player, comps);
    }

    public void broadcastMessage(String key, MessageData... data) {
        Bukkit.getServer().getOnlinePlayers().forEach(online -> sendMessage(key, online, data));
        sendMessage(key, Bukkit.getConsoleSender(), data);
    }

    public void broadcastNetworkMessage(String key, MessageData... data) {
        if (!BungeeUtil.isBungee()) broadcastMessage(key, data);
        else BungeeUtil.broadcastMessage(getMessageAsString(key, key, false, data));
    }
}
