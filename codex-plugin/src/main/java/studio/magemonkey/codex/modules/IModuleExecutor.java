package studio.magemonkey.codex.modules;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.commands.api.IGeneralCommand;

public class IModuleExecutor<P extends CodexPlugin<P>> extends IGeneralCommand<P> {

    protected IModule<P> module;

    public IModuleExecutor(@NotNull IModule<P> module, @NotNull String[] labels) {
        this(module, labels, null);
    }

    public IModuleExecutor(@NotNull IModule<P> module, @NotNull String[] labels, @Nullable String permission) {
        super(module.plugin, labels, permission);
        this.module = module;
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    @NotNull
    public String description() {
        return "";
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

    }
}
