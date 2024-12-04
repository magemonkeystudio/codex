package studio.magemonkey.codex.commands.list;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.commands.api.ISubCommand;
import studio.magemonkey.codex.util.StringUT;

public class HelpCommand<P extends CodexPlugin<P>> extends ISubCommand<P> {

    public HelpCommand(@NotNull P plugin) {
        super(plugin, new String[]{"help"}, plugin.getNameRaw() + ".user");
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Codex_Command_Help_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    protected void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            for (String s : plugin.lang().Codex_Command_Help_List.asList()) {
                if (s.equalsIgnoreCase("%cmds%")) {
                    for (ISubCommand<?> cmd : this.parent.getSubCommands()) {
                        if (!cmd.hasPerm(sender)) continue;

                        String f = plugin.lang().Codex_Command_Help_Format
                                .replace("%description%", cmd.description())
                                .replace("%usage%", cmd.usage())
                                .replace("%cmd%", cmd.labels()[0])
                                .replace("%label%", parent.labels()[0])
                                .getMsg();
                        sender.sendMessage(StringUT.oneSpace(f));
                    }
                } else {
                    sender.sendMessage(s);
                }
            }
        }
    }
}
