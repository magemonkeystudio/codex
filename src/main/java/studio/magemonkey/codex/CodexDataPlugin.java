package studio.magemonkey.codex;

import lombok.NoArgsConstructor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.data.IDataHandler;
import studio.magemonkey.codex.data.users.IAbstractUser;
import studio.magemonkey.codex.data.users.IUserManager;

import java.io.File;

@NoArgsConstructor
public abstract class CodexDataPlugin<P extends CodexDataPlugin<P, U>, U extends IAbstractUser<P>> extends CodexPlugin<P> {

    protected IUserManager<P, U> userManager;

    public CodexDataPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    protected abstract boolean setupDataHandlers();

    protected void shutdownDataHandlers() {
        if (this.userManager != null) this.userManager.shutdown();

        IDataHandler<P, U> dataHandler = this.getData();
        if (dataHandler != null) dataHandler.shutdown();
    }

    public abstract IDataHandler<P, U> getData();

    @NotNull
    public IUserManager<P, U> getUserManager() {
        return this.userManager;
    }
}
