package studio.magemonkey.codex.commands.list;

import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.commands.api.IGeneralCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class MainCommand<P extends CodexPlugin<P>> extends IGeneralCommand<P> {

    public MainCommand(@NotNull P plugin) {
        super(plugin, plugin.getLabels());
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    @NotNull
    public String description() {
        return "";
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    public void perform(@NotNull CommandSender sender, String label, @NotNull String[] args) {
        // We do not need to put here anything as this command has defaultCommand in CommandManager.
    }
}
