package mc.promcteam.engine;

import lombok.NoArgsConstructor;
import mc.promcteam.engine.data.IDataHandler;
import mc.promcteam.engine.data.users.IAbstractUser;
import mc.promcteam.engine.data.users.IUserManager;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@NoArgsConstructor
public abstract class NexDataPlugin<P extends NexDataPlugin<P, U>, U extends IAbstractUser<P>> extends NexPlugin<P> {

    protected IUserManager<P, U> userManager;

    public NexDataPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
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
