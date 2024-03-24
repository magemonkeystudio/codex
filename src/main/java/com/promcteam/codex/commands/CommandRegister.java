package com.promcteam.codex.commands;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.commands.api.IGeneralCommand;
import com.promcteam.codex.utils.Reflex;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;

public class CommandRegister extends Command implements PluginIdentifiableCommand {

    protected       Plugin          plugin;
    protected final CommandExecutor owner;
    protected       TabCompleter    tab;

    public CommandRegister(String[] aliases, String desc, String usage, CommandExecutor owner, Plugin plugin2) {
        super(aliases[0], desc, usage, Arrays.asList(aliases));
        this.owner = owner;
        this.plugin = plugin2;
    }

    public void setTabCompleter(@NotNull TabCompleter tab) {
        this.tab = tab;
    }

    @Override
    @NotNull
    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        return this.owner.onCommand(sender, this, label, args);
    }

    @Override
    @NotNull
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (this.tab != null) {
            List<String> list = this.tab.onTabComplete(sender, this, alias, args);
            if (list != null) {
                return list;
            }
        }
        return Collections.emptyList();
    }

    public static void register(@NotNull Plugin plugin, @NotNull IGeneralCommand<?> command) {
        CommandRegister cmd = new CommandRegister(command.labels(), command.description(), command.usage(), command, plugin);
        cmd.setTabCompleter(command);
        cmd.setPermission(command.getPermission());

        Server     server = plugin.getServer();
        CommandMap map    = (CommandMap) Reflex.getFieldValue(server, "commandMap");
        if (map == null) return;

        map.register(plugin.getDescription().getName(), cmd);
    }

    public static void register(Plugin plugin, CommandExecutor cxecutor, TabCompleter tab, String[] aliases, String desc, String usage) {
        CommandRegister reg = new CommandRegister(aliases, desc, usage, cxecutor, plugin);
        reg.setTabCompleter(tab);

        Server     server = plugin.getServer();
        CommandMap map    = (CommandMap) Reflex.getFieldValue(server, "commandMap");
        if (map == null) return;

        map.register(plugin.getDescription().getName(), reg);
    }

    public static void syncCommands() {
        // Fix tab completer when registerd on runtime
        Server server = Bukkit.getServer();
        Method method = Reflex.getMethod(server.getClass(), "syncCommands");
        if (method == null) return;

        Reflex.invokeMethod(method, server);
    }

    @SuppressWarnings("unchecked")
    public static void unregister(@NotNull CodexPlugin<?> plugin, String[] aliases) {
        SimpleCommandMap map = (SimpleCommandMap) Reflex.getFieldValue(plugin.getPluginManager(), "commandMap");
        if (map == null) return;

        HashMap<String, Command> knownCommands = (HashMap<String, Command>) Reflex.getFieldValue(map, "knownCommands");
        if (knownCommands == null) return;

        for (String command : aliases) {
            for (String commandAlias : getAliases(command)) {
                Command cmd = map.getCommand(commandAlias);
                if (cmd == null) {
                    continue;
                }
                if (!cmd.unregister(map)) {
                    plugin.error("Unable to unregister command: " + commandAlias);
                }
                knownCommands.remove(commandAlias);
                //plugin.info("Command unregistered: '" + commandAlias + "'");
            }
        }
    }

    @NotNull
    public static Set<String> getAliases(@NotNull String cmd) {
        SimpleCommandMap map = (SimpleCommandMap) Reflex.getFieldValue(Bukkit.getServer().getPluginManager(), "commandMap");
        if (map == null) return Collections.emptySet();

        for (Command c2 : map.getCommands()) {
            if (c2.getLabel().equalsIgnoreCase(cmd) || c2.getAliases().contains(cmd)) {
                Set<String> aa = new HashSet<>(c2.getAliases());
                aa.add(c2.getLabel());

                return aa;
            }
        }

        return Collections.emptySet();
    }
}
