package studio.magemonkey.codex;

import studio.magemonkey.codex.commands.CommandManager;
import studio.magemonkey.codex.commands.api.IGeneralCommand;
import studio.magemonkey.codex.commands.list.MainCommand;
import studio.magemonkey.codex.config.ConfigManager;
import studio.magemonkey.codex.config.api.IConfigTemplate;
import studio.magemonkey.codex.core.config.CoreLang;
import studio.magemonkey.codex.hooks.HookManager;
import studio.magemonkey.codex.hooks.NHook;
import studio.magemonkey.codex.hooks.external.IMythicHook;
import studio.magemonkey.codex.hooks.external.VaultHK;
import studio.magemonkey.codex.hooks.external.WorldGuardHK;
import studio.magemonkey.codex.hooks.external.citizens.CitizensHK;
import studio.magemonkey.codex.manager.api.Loggable;
import studio.magemonkey.codex.manager.api.gui.NGUI;
import studio.magemonkey.codex.manager.editor.EditorHandler;
import studio.magemonkey.codex.modules.ModuleManager;
import studio.magemonkey.codex.nms.NMS;
import studio.magemonkey.codex.nms.packets.PacketManager;
import studio.magemonkey.codex.util.actions.ActionsManager;
import studio.magemonkey.codex.util.actions.Parametized;
import studio.magemonkey.codex.util.craft.CraftManager;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

@NoArgsConstructor
public abstract class CodexPlugin<P extends CodexPlugin<P>> extends JavaPlugin implements Loggable {
    public static boolean IS_BUNGEE = false;
    public static String  BUNGEE_ID = "server";

    private       Logger  logger;
    private       boolean isEngine;
    private final boolean isSpigot = true;

    @Getter
    protected ConfigManager<P>  configManager;
    @Getter
    protected CommandManager<P> commandManager;
    @Getter
    protected ModuleManager<P>  moduleManager;
    protected EditorHandler<P>  editorHandler;

    public boolean isEngine() {
        return this.isEngine;
    }

    public boolean isSpigot() {
        return isSpigot;
    }

    @NotNull
    public static CodexEngine getEngine() {
        return CodexEngine.get();
    }

    public CodexPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onEnable() {
        long loadTook = System.currentTimeMillis();
        this.logger = this.getLogger();
        this.isEngine = this instanceof CodexEngine;

//		if (!(this.isSpigot = this.getServer().getVersion().toLowerCase().contains("spigot"))) {
//			this.warn("============== DISCLAIMER ==============");
//			this.warn("> You're running an unknown Spigot fork.");
//			this.warn("> This plugin is designed to work on Spigot (spigotmc.org) only!");
//			this.warn("> You won't get any support for any issues unless they persist on regular Spigot.");
//			this.warn("========================================");
//		}

        CodexEngine engine = getEngine();
        if (this.isEngine()) {
            if (!engine.loadCore()) {
                this.getPluginManager().disablePlugin(this);
                return;
            }
        } else {
            engine.hookChild(this);
            this.info("Powered by: " + engine.getName());
        }
        this.loadManagers();

        if (this.isEngine()) {
            ((CodexEngine) this).registerEvents();
        }
        this.info("Plugin loaded in " + (System.currentTimeMillis() - loadTook) + " ms!");
    }

    @Override
    public void onDisable() {
        this.unloadManagers();
    }

    public abstract void enable();

    public abstract void disable();

    public void reload() {
        if (this.isEngine()) {
            this.setConfig();
            return;
        }
        this.unloadManagers();
        this.loadManagers();
    }

    public abstract void setConfig();

    public abstract void registerHooks();

    public abstract void registerCommands(@NotNull IGeneralCommand<P> mainCommand);

    public abstract void registerEditor();

    @NotNull
    public abstract IConfigTemplate cfg();

    @NotNull
    public abstract CoreLang lang();

    @Override
    public void info(@NotNull String msg) {
        this.logger.info(msg);
    }

    @Override
    public void warn(@NotNull String msg) {
        this.logger.warning(msg);
    }

    @Override
    public void error(@NotNull String msg) {
        this.logger.severe(msg);
    }

    @Nullable
    public <T extends NHook<P>> T registerHook(@NotNull String pluginName, @NotNull Class<T> clazz) {
        return this.getHooks().register(this, pluginName, clazz);
    }

    private void unregisterListeners() {
        // Force close custom GUIs
        // To prevent take items after unregister listeners
        for (Player p : this.getServer().getOnlinePlayers()) {
            if (p != null) {
                InventoryView inv = p.getOpenInventory();
                if (inv == null || inv.getTopInventory() == null) continue;
                InventoryHolder ih = inv.getTopInventory().getHolder();
                if (ih instanceof NGUI) {
                    p.closeInventory();
                }
            }
        }
        HandlerList.unregisterAll(this);
    }

    @SuppressWarnings("unchecked")
    private void loadManagers() {
        // Setup plugin Hooks.
        this.registerHooks();

        try {
            // Setup ConfigManager before any other managers.
            this.configManager = new ConfigManager<>((P) this);
            this.configManager.setup();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            this.getPluginManager().disablePlugin(this);
            return;
        }

        if (this.cfg().cmds == null || this.cfg().cmds.length == 0) {
            this.error("Could not register plugin commands!");
            this.getPluginManager().disablePlugin(this);
            return;
        }

        // Connect to the database if present.
        CodexDataPlugin<?, ?> dataPlugin = null;
        if (this instanceof CodexDataPlugin) {
            dataPlugin = (CodexDataPlugin<?, ?>) this;
            if (!dataPlugin.setupDataHandlers()) {
                this.error("Could not setup plugin Data Handler!");
                this.getPluginManager().disablePlugin(this);
                return;
            }
        }

        this.registerEditor();

        // Register plugin commands.
        this.commandManager = new CommandManager<>((P) this);
        this.commandManager.setup();

        // Register plugin modules.
        this.moduleManager = new ModuleManager<>((P) this);
        this.moduleManager.setup();

        // Custom plugin loaders.
        this.enable();

        // Load plugin users only when full plugin is loaded.
        if (dataPlugin != null) {
            dataPlugin.getUserManager().loadOnlineUsers();
        }

        Parametized.clearCache();
    }

    private void unloadManagers() {
        this.getServer().getScheduler().cancelTasks(this); // First stop all plugin tasks

        if (this.getModuleManager() != null) this.getModuleManager().shutdown();
        this.disable();
        if (this.getCommandManager() != null) this.getCommandManager().shutdown();
        if (this.getEditorHandler() != null) this.getEditorHandler().shutdown();

        // Unregister all plugin traits and NPC listeners.
        CitizensHK citizensHook = this.getCitizens();
        if (citizensHook != null) {
            citizensHook.unregisterTraits(this);
            citizensHook.unregisterListeners(this);
        }

        // Unregister all plugin hooks.
        if (!this.isEngine()) {
            this.getHooks().shutdown(this);
        }

        // Unregister ALL plugin listeners.
        this.unregisterListeners();

        // Save user data and disconnect from the database.
        if (this instanceof CodexDataPlugin) {
            CodexDataPlugin<?, ?> userData = (CodexDataPlugin<?, ?>) this;
            userData.shutdownDataHandlers();
        }
    }

    @NotNull
    public String getAuthor() {
        List<String> list = this.getDescription().getAuthors();
        return list.isEmpty() ? "MageMonkeyStudio" : list.get(0);
    }

    @NotNull
    public String getNameRaw() {
        return this.getName().toLowerCase().replace(" ", "").replace("-", "");
    }

    @NotNull
    public String getLabel() {
        return this.getLabels()[0];
    }

    @NotNull
    public String[] getLabels() {
        return this.cfg().cmds;
    }

    @NotNull
    public NMS getNMS() {
        return getEngine().getNMS();
    }

    @NotNull
    public MainCommand<P> getMainCommand() {
        return this.getCommandManager().getMainCommand();
    }

    public CraftManager getCraftManager() {
        return getEngine().getCraftManager();
    }

    public ActionsManager getActionsManager() {
        return getEngine().getActionsManager();
    }

    @NotNull
    public PacketManager getPacketManager() {
        return getEngine().getPacketManager();
    }

    @NotNull
    public PluginManager getPluginManager() {
        return getEngine().getPluginManager();
    }

    @NotNull
    public HookManager getHooks() {
        return getEngine().getHooksManager();
    }

    public boolean isHooked(@NotNull Class<? extends NHook<?>> clazz) {
        return this.getHooks().isHooked(this, clazz);
    }

    public boolean isHooked(@NotNull String plugin) {
        return this.getHooks().isHooked(this, plugin);
    }

    @Nullable
    public <T extends NHook<?>> T getHook(@NotNull Class<T> clazz) {
        return this.getHooks().getHook(this, clazz);
    }

    @Nullable
    public NHook<? extends CodexPlugin<?>> getHook(@NotNull String name) {
        return this.getHooks().getHook(this, name);
    }

    @Nullable
    public VaultHK getVault() {
        return getEngine().getVault();
    }

    @Nullable
    public CitizensHK getCitizens() {
        return getEngine().getCitizens();
    }

    @Nullable
    public WorldGuardHK getWorldGuard() {
        return getEngine().getWorldGuard();
    }

    @Nullable
    public IMythicHook getMythicMobs() {
        return getEngine().getMythicMobs();
    }

    public boolean hasEditor() {
        return this.editorHandler != null;
    }

    public void openEditor(@NotNull Player p) {
        if (!this.hasEditor()) {
            throw new IllegalStateException("This plugin does not provides GUI Editor!");
        }
        this.editorHandler.open(p, 1);
    }

    @Nullable
    public EditorHandler<P> getEditorHandler() {
        return this.editorHandler;
    }

    public ClassLoader getClazzLoader() {
        return this.getClassLoader();
    }
}
