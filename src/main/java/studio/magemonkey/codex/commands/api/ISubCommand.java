package studio.magemonkey.codex.commands.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.CodexPlugin;

import java.util.List;

public abstract class ISubCommand<P extends CodexPlugin<P>> extends IAbstractCommand<P> {

    protected IGeneralCommand<P> parent;

    public ISubCommand(@NotNull P plugin, @NotNull List<String> aliases) {
        this(plugin, aliases.toArray(new String[aliases.size()]));
    }

    public ISubCommand(@NotNull P plugin, @NotNull String[] aliases) {
        this(plugin, aliases, null);
    }

    public ISubCommand(@NotNull P plugin, @NotNull List<String> aliases, @Nullable String permission) {
        this(plugin, aliases.toArray(new String[aliases.size()]), permission);
    }

    public ISubCommand(@NotNull P plugin, @NotNull String[] aliases, @Nullable String permission) {
        super(plugin, aliases, permission);
    }

    @NotNull
    public IGeneralCommand<P> getParent() {
        return this.parent;
    }

    public void setParent(@NotNull IGeneralCommand<P> parent) {
        this.parent = parent;
    }
}
