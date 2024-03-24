package com.promcteam.codex.commands.list;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.commands.api.ISubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand<P extends CodexPlugin<P>> extends ISubCommand<P> {

    public ReloadCommand(@NotNull P plugin) {
        super(plugin, new String[]{"reload"}, plugin.getNameRaw() + ".admin");
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Core_Command_Reload_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    public void perform(@NotNull CommandSender sender, String label, @NotNull String[] args) {
        plugin.reload();
        plugin.lang().Core_Command_Reload_Done.send(sender);
    }
}
