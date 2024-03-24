package com.promcteam.codex.commands.list;

import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.commands.api.ISubCommand;
import com.promcteam.codex.utils.StringUT;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class AboutCommand<P extends CodexPlugin<P>> extends ISubCommand<P> {

    public AboutCommand(@NotNull P plugin) {
        super(plugin, new String[]{"about"});
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Core_Command_About_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    public void perform(@NotNull CommandSender sender, String label, @NotNull String[] args) {
        List<String> info = StringUT.color(Arrays.asList(
                "&7",
                "&e" + plugin.getName() + " &6v" + plugin.getDescription().getVersion() + " &ecreated by &6" + plugin.getAuthor(),
                "&eType &6/" + plugin.getLabel() + " help&e to list plugin commands.",
                "&7",
                "&2Powered by &a&l" + CodexEngine.get().getName() + "&2, © 2019-2023 &a" + CodexPlugin.TM
        ));

        info.forEach(line -> sender.sendMessage(line));
    }
}
