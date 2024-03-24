package com.promcteam.risecore.command;

import com.promcteam.risecore.legacy.util.message.MessageData;
import com.promcteam.risecore.legacy.util.message.MessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public abstract class RiseCommand implements CommandExecutor, TabCompleter {

    protected     Plugin plugin;
    private final String name;
    private       String description, usage;
    private final ArrayList<String> aliases       = new ArrayList<>();
    private       RiseCommand       parentCommand = null;
    private final HashSet<RiseCommand> children      = new HashSet<>();
    private final Command              bukkitCommand;

    public RiseCommand(Plugin plugin,
                       String name,
                       String description,
                       String usage,
                       List<String> aliases,
                       RiseCommand parentCommand) {
        this.plugin = plugin;
        this.name = name.toLowerCase();
        setDescription(description);
        setUsage(usage);
        setParentCommand(parentCommand);
        for (String alias : aliases) {
            this.aliases.add(alias.toLowerCase());
        }

        if (parentCommand == null) {
            plugin.getServer().getPluginCommand(name).setExecutor(this);
            plugin.getServer().getPluginCommand(name).setTabCompleter(this);
        }

        RiseCommand parent = hasParentCommand() ? getParentCommand() : this;
        while (parent != null && parent.hasParentCommand()) {
            parent = parent.getParentCommand();
        }
        bukkitCommand = Bukkit.getServer().getPluginCommand(parent.getName());
    }

    public RiseCommand(Plugin plugin, String name, String description, String usage) {
        this(plugin, name, description, usage, Collections.singletonList(name), null);
    }

    public RiseCommand(String name, List<String> aliases, Plugin plugin) {
        this(plugin, name, "", "", aliases, null);
    }

    public RiseCommand(String name, List<String> aliases, RiseCommand parentCommand) {
        this(parentCommand != null ? parentCommand.plugin : null, name, "", "", aliases, parentCommand);
    }

    public RiseCommand(String name, List<String> aliases) {
        this(name, aliases, (RiseCommand) null);
    }

    protected boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    private void setParentCommand(RiseCommand parent) {
        this.parentCommand = parent;
        if (parent != null)
            parent.addChild(this);
    }

    public RiseCommand getParentCommand() {
        return parentCommand;
    }

    public boolean hasParentCommand() {
        return parentCommand != null;
    }

    public HashSet<RiseCommand> getChildren() {
        return children;
    }

    public void addChild(RiseCommand command) {
        children.add(command);
    }

    public RiseCommand getChild(String[] args) {
//        RiseCommand newChild = null;
        if (args.length == 0)
            return null;

        for (RiseCommand child : children) {
            if (child.isAlias(args[0])) {
                return child;
                //Nix recursion, as it's already recursive in the onCommand call.
//                String[] newArgs = new String[args.length - 1];
//                for (int i = 1; i < args.length; i++)
//                    newArgs[i - 1] = args[i];
//
//                newChild = child.getChild(newArgs);
//                if (newChild == null)
//                    return child; //If the recursion gives us a null result, we should return the child we're on.
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public boolean isAlias(String cmd) {
        return name.equalsIgnoreCase(cmd) || aliases.contains(cmd.toLowerCase());
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        if (getBukkitCommand() != null)
            getBukkitCommand().setDescription(description);
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
        if (getBukkitCommand() != null)
            getBukkitCommand().setUsage(usage);
    }

    public void sendUsage(String usage, CommandSender sender, RiseCommand command, String[] args) {
        List<String> strs = new ArrayList<>(20);
        RiseCommand  cmd  = command;
        while (cmd.hasParentCommand()) {
            strs.add(cmd.getName());
            cmd = cmd.getParentCommand();
        }
//        Command<CommandSender> cmd = command;
//        while (true)
//        {
//            strs.add(cmd.getName());
//            if (cmd instanceof SubCommand)
//            {
//                cmd = ((SubCommand<CommandSender>) cmd).getParent();
//                continue;
//            }
//        }
        StringBuilder sb = new StringBuilder(100);
        for (int i = strs.size() - 1; i >= 0; i--) {
            sb.append(strs.get(i));
            if (i != 0) {
                sb.append(' ');
            }
        }
        String s = sb.toString();
        MessageUtil.sendMessage(usage,
                sender,
                new MessageData("text", (s.isEmpty() ? "" : s + " ") + StringUtils.join(args, " ")));
    }

    public boolean checkPermission(CommandSender sender, String permission) {
        return sender instanceof ConsoleCommandSender || sender.hasPermission(permission);
    }

    public Command getBukkitCommand() {
        return bukkitCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        RiseCommand child = getChild(args);

        if (args.length == 0 || child == null)
            this.runCommand(sender, this, label, args);
        else {
            String[] tempargs = new String[args.length - 1];
            System.arraycopy(args, 1, tempargs, 0, args.length - 1);
            child.onCommand(sender, command, label, tempargs);
        }

        return true;
    }

    public abstract void runCommand(CommandSender sender, RiseCommand command, String label, String[] args);

    //TODO get tab completion going
    @Override
    public @Nullable List<String> onTabComplete(CommandSender commandSender,
                                                Command command,
                                                String label,
                                                String[] strings) {
        return null;
    }
}
