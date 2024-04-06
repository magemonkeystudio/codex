package studio.magemonkey.codex.commands.list;

import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.commands.api.ISubCommand;
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
        return plugin.lang().Codex_Command_Reload_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        plugin.reload();
        plugin.lang().Codex_Command_Reload_Done.send(sender);
    }
}
