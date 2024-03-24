package com.promcteam.codex;

import lombok.NoArgsConstructor;
import com.promcteam.codex.data.IDataHandler;
import com.promcteam.codex.data.users.IAbstractUser;
import com.promcteam.codex.data.users.IUserManager;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;

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
