package com.promcteam.risecore.command;

import com.promcteam.risecore.Core;
import com.promcteam.risecore.legacy.util.message.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class ReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof ConsoleCommandSender || sender.hasPermission("risecore.reload")) {
            Core.getInstance().reloadConfiguration();
            MessageUtil.sendMessage("reload", sender);
        } else {
            MessageUtil.sendMessage("noPermissions", sender);
        }

        return true;
    }
}
