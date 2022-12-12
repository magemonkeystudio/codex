package mc.promcteam.engine;

import lombok.Getter;
import mc.promcteam.engine.api.armor.ArmorListener;
import mc.promcteam.engine.commands.api.IGeneralCommand;
import mc.promcteam.engine.commands.list.Base64Command;
import mc.promcteam.engine.core.Version;
import mc.promcteam.engine.core.config.CoreConfig;
import mc.promcteam.engine.core.config.CoreLang;
import mc.promcteam.engine.hooks.HookManager;
import mc.promcteam.engine.hooks.Hooks;
import mc.promcteam.engine.hooks.external.*;
import mc.promcteam.engine.hooks.external.citizens.CitizensHK;
import mc.promcteam.engine.manager.editor.EditorManager;
import mc.promcteam.engine.mccore.chat.ChatCommander;
import mc.promcteam.engine.mccore.chat.ChatListener;
import mc.promcteam.engine.mccore.commands.CommandLog;
import mc.promcteam.engine.mccore.config.Config;
import mc.promcteam.engine.mccore.scoreboard.BoardListener;
import mc.promcteam.engine.mccore.scoreboard.CycleTask;
import mc.promcteam.engine.mccore.scoreboard.ScoreboardCommander;
import mc.promcteam.engine.mccore.scoreboard.UpdateTask;
import mc.promcteam.engine.mccore.util.VersionManager;
import mc.promcteam.engine.nms.NMS;
import mc.promcteam.engine.nms.packets.PacketManager;
import mc.promcteam.engine.utils.ItemUT;
import mc.promcteam.engine.utils.Reflex;
import mc.promcteam.engine.utils.actions.ActionsManager;
import mc.promcteam.engine.utils.craft.CraftManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class NexEngine extends NexPlugin<NexEngine> implements Listener {

    private static final Hashtable<String, Config> configs = new Hashtable<>();
    private static       NexEngine                 instance;
    private final        Set<NexPlugin<?>>         plugins;
    @Getter
    NMS           NMS;
    @Getter
    PluginManager pluginManager;
    @Getter
    PacketManager  packetManager;
    @Getter
    ActionsManager actionsManager;
    @Getter
    CraftManager craftManager;
    @Getter
    VaultHK      vault;
    @Getter
    CitizensHK   citizens;
    @Getter
    WorldGuardHK worldGuard;
    @Getter
    IMythicHook  mythicMobs;
    private CoreConfig  cfg;
    private CoreLang    lang;
    @Getter
    private HookManager hooksManager;

    private boolean chatEnabled;
    private String  commandMessage = "&4Please wait &6{time} seconds &4before using the command again.";
    @Getter
    private boolean scoreboardsEnabled;

    private CycleTask  cTask;
    private UpdateTask uTask;

    public NexEngine() {
        setInstance();
        this.plugins = new HashSet<>();
    }

    public NexEngine(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        setInstance();
        this.plugins = new HashSet<>();
    }

    private void setInstance() {
        instance = this;
        ItemUT.setEngine(this);
    }

    @NotNull
    public static NexEngine get() {
        return instance;
    }

    final boolean loadCore() {
        this.pluginManager = this.getServer().getPluginManager();

        if (!this.setupNMS()) {
            this.error("Could not setup NMS version. Plugin will be disabled.");
            return false;
        }

        this.getPluginManager().registerEvents(this, this);
        this.getPluginManager().registerEvents(new ArmorListener(), this);

        this.hooksManager = new HookManager(this);
        this.hooksManager.setup();

        this.packetManager = new PacketManager(this);
        this.packetManager.setup();

        this.actionsManager = new ActionsManager(this);
        this.actionsManager.setup();

        this.craftManager = new CraftManager(this);
        this.craftManager.setup();

        return true;
    }

    private boolean setupNMS() {
        Version current = Version.CURRENT;
        this.info("You are running MC version " + current);
        if (current == null) return false;
        if (current == Version.TEST) current = Version.values()[Version.values().length - 1];

        String   pack  = NMS.class.getPackage().getName();
        Class<?> clazz = Reflex.getClass(pack, current.name());
        if (clazz == null) return false;

        try {
            this.NMS = (NMS) clazz.getConstructor().newInstance();
            this.info("Loaded NMS version: " + current.name());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.NMS != null;
    }

    @Override
    public void enable() {
        EditorManager.setup();

        CommandLog.callback = msg -> VersionManager.initialize(msg);
        getServer().dispatchCommand(new CommandLog(), "version");

        getLogger().info("ProMCCore has been enabled!");
    }

    @Override
    public void disable() {
        // Unregister Custom Actions Engine
        if (this.actionsManager != null) {
            this.actionsManager.shutdown();
            this.actionsManager = null;
        }
        if (this.packetManager != null) {
            this.packetManager.shutdown();
        }
        if (this.hooksManager != null) {
            this.hooksManager.shutdown();
        }
        if (this.craftManager != null) {
            this.craftManager.shutdown();
            this.craftManager = null;
        }

        if (isScoreboardsEnabled()) {
            cTask.cancel();
            uTask.cancel();
        }

        EditorManager.shutdown();
    }

    @Override
    public void setConfig() {
        this.cfg = new CoreConfig(this);
        this.cfg.setup();

        commandMessage = this.cfg.getJYML().getString("Settings.command-cooldown-message", "&4Please wait &6{time} seconds &4before using the command again.");
        chatEnabled = this.cfg.getJYML().getBoolean("Features.chat-enabled", true);
        scoreboardsEnabled = this.cfg.getJYML().getBoolean("Features.scoreboards-enabled", true);

        if (chatEnabled) {
            new ChatCommander(this);
            new ChatListener(this);
        }
        if (scoreboardsEnabled) {
            new ScoreboardCommander(this);
            new BoardListener(this);
            cTask = new CycleTask(this);
            uTask = new UpdateTask(this);
        }

        this.lang = new CoreLang(this);
        this.lang.setup();
    }

    @Override
    public void registerHooks() {
        try {
            this.vault = this.registerHook(Hooks.VAULT, VaultHK.class);
        } catch (Exception e) {
        }
    }

    @Override
    public void registerCmds(@NotNull IGeneralCommand<NexEngine> mainCommand) {
        mainCommand.addSubCommand(new Base64Command(this));
    }

    @Override
    public void registerEditor() {

    }

    @Override
    @NotNull
    public CoreConfig cfg() {
        return this.cfg;
    }

    @Override
    @NotNull
    public CoreLang lang() {
        return this.lang;
    }


    void hookChild(@NotNull NexPlugin<?> child) {
        this.plugins.add(child);
    }

    @NotNull
    public Set<NexPlugin<?>> getChildPlugins() {
        return this.plugins;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHookLate(PluginEnableEvent e) {
        String name = e.getPlugin().getName();
        try {
            if (this.mythicMobs == null && name.equalsIgnoreCase(Hooks.MYTHIC_MOBS)) {
                boolean mythic4 = true;
                try {
                    Class.forName("io.lumine.xikage.mythicmobs.MythicMobs");
                } catch (ClassNotFoundException classNotFoundException) {
                    mythic4 = false;
                }

                this.mythicMobs = mythic4
                        ? this.registerHook(Hooks.MYTHIC_MOBS, MythicMobsHK.class)
                        : this.registerHook(Hooks.MYTHIC_MOBS, MythicMobsHKv5.class);
                return;
            }
            if (this.worldGuard == null && name.equalsIgnoreCase(Hooks.WORLD_GUARD)) {
                this.worldGuard = this.registerHook(Hooks.WORLD_GUARD, WorldGuardHK.class);
                return;
            }
            if (this.citizens == null && name.equalsIgnoreCase(Hooks.CITIZENS)) {
                this.citizens = this.registerHook(Hooks.CITIZENS, CitizensHK.class);
                return;
            }
        } catch (Exception ex) {
        }
    }

    /**
     * Checks whether or not MCCore's chat management is enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean isChatEnabled() {
        return chatEnabled;
    }

    /**
     * <p>Retrieves the configuration file for a plugin</p>
     * <p>If the config file hasn't been loaded yet, this will
     * load the file first.</p>
     * <p>Configs retrieved via this method are handled by MCCore
     * and automatically saved when MCCore disables.</p>
     * <p>This should not be used for settings configs
     * that admins may want to edit while the server is running
     * as the auto save will overwrite any changes they make.</p>
     *
     * @param file file name
     * @return config manager for the file
     */
    public Config getConfigFile(JavaPlugin plugin, String file) {
        if (!configs.containsKey(file.toLowerCase() + plugin.getName())) {
            Config config = new Config(plugin, file);
            registerConfig(config);
            return config;
        }
        return configs.get(file.toLowerCase() + plugin.getName());
    }

    /**
     * <p>Registers the Config with MCCore for auto saving.</p>
     * <p>If the Config was already registered, this method will
     * not do anything.</p>
     *
     * @param config config to register
     */
    public void registerConfig(Config config) {
        configs.put(config.getFile().toLowerCase() + config.getPlugin().getName(), config);
    }

    /**
     * Retrieves the message to be shown when a command is on cooldown
     *
     * @return command cooldown message
     */
    public String getCommandMessage() {
        return commandMessage;
    }
}
