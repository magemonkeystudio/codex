package studio.magemonkey.codex.commands.list;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.commands.api.ISubCommand;
import studio.magemonkey.codex.util.StringUT;

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
        return plugin.lang().Codex_Command_About_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> info = StringUT.color(Arrays.asList(
                "&7",
                "&e" + plugin.getName() + " &6v" + plugin.getDescription().getVersion() + " &ecreated by &6"
                        + plugin.getAuthor(),
                "&eType &6/" + plugin.getLabel() + " help&e to list plugin commands.",
                "&7",
                "&2Powered by &a&l" + CodexEngine.get().getName() + "&2, © 2019-2022 &a" + CodexEngine.get()
                        .getAuthor()
        ));

        info.forEach(sender::sendMessage);
    }
}
