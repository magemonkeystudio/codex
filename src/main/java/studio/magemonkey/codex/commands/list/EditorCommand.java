package studio.magemonkey.codex.commands.list;

import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.commands.api.ISubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EditorCommand<P extends CodexPlugin<P>> extends ISubCommand<P> {

    public EditorCommand(@NotNull P plugin) {
        super(plugin, new String[]{"editor"}, plugin.getNameRaw() + ".cmd.editor");
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Codex_Command_Editor_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    public void perform(@NotNull CommandSender sender, String label, @NotNull String[] args) {
        Player player = (Player) sender;
        this.plugin.openEditor(player);
    }

}
