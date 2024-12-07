package studio.magemonkey.codex;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.api.NMSProvider;
import studio.magemonkey.codex.bungee.BungeeListener;
import studio.magemonkey.codex.bungee.BungeeUtil;
import studio.magemonkey.codex.commands.UnstuckCommand;
import studio.magemonkey.codex.commands.api.IGeneralCommand;
import studio.magemonkey.codex.config.legacy.LegacyConfigManager;
import studio.magemonkey.codex.core.Version;
import studio.magemonkey.codex.core.config.CoreConfig;
import studio.magemonkey.codex.core.config.CoreLang;
import studio.magemonkey.codex.hooks.HookManager;
import studio.magemonkey.codex.hooks.Hooks;
import studio.magemonkey.codex.hooks.external.*;
import studio.magemonkey.codex.hooks.external.citizens.CitizensHK;
import studio.magemonkey.codex.items.CodexItemManager;
import studio.magemonkey.codex.legacy.item.*;
import studio.magemonkey.codex.legacy.placeholder.PlaceholderRegistry;
import studio.magemonkey.codex.legacy.riseitem.DarkRiseItemImpl;
import studio.magemonkey.codex.listeners.ArmorListener;
import studio.magemonkey.codex.listeners.BoatListener;
import studio.magemonkey.codex.listeners.InteractListener;
import studio.magemonkey.codex.listeners.JoinListener;
import studio.magemonkey.codex.manager.api.menu.MenuManager;
import studio.magemonkey.codex.manager.editor.EditorManager;
import studio.magemonkey.codex.mccore.chat.ChatCommander;
import studio.magemonkey.codex.mccore.chat.ChatListener;
import studio.magemonkey.codex.mccore.config.Config;
import studio.magemonkey.codex.mccore.scoreboard.BoardListener;
import studio.magemonkey.codex.mccore.scoreboard.CycleTask;
import studio.magemonkey.codex.mccore.scoreboard.ScoreboardCommander;
import studio.magemonkey.codex.mccore.scoreboard.UpdateTask;
import studio.magemonkey.codex.migration.MigrationUtil;
import studio.magemonkey.codex.nms.packets.PacketManager;
import studio.magemonkey.codex.util.Debugger;
import studio.magemonkey.codex.util.ItemUT;
import studio.magemonkey.codex.util.Reflex;
import studio.magemonkey.codex.util.actions.ActionsManager;
import studio.magemonkey.codex.util.craft.CraftManager;
import studio.magemonkey.codex.util.messages.MessageUtil;

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

//  Main Codex logic
public class CodexEngine extends CodexPlugin<CodexEngine> implements Listener {
    private static final Hashtable<String, Config> configs = new Hashtable<>();
    private static       CodexEngine               instance;
    private final        Set<CodexPlugin<?>>       plugins;
    @Getter
    private              PluginManager             pluginManager;
    @Getter
    private              PacketManager             packetManager;
    @Getter
    private              ActionsManager            actionsManager;
    @Getter
    private              CraftManager              craftManager;
    @Getter
    private              MenuManager               menuManager;
    @Getter
    private              VaultHK                   vault;
    @Getter
    private              CitizensHK                citizens;
    @Getter
    private              WorldGuardHK              worldGuard;
    @Getter
    private              IMythicHook               mythicMobs;
    private              CoreConfig                cfg;
    private              CoreLang                  lang;
    @Getter
    private              HookManager               hooksManager;
    @Getter
    private              CodexItemManager          itemManager;

    /**
     * -- GETTER --
     * Checks whether Codex's chat management is enabled
     *
     * @return true if enabled, false otherwise
     */
    @Getter
    private boolean chatEnabled;
    /**
     * -- GETTER --
     * Retrieves the message to be shown when a command is on cooldown
     *
     * @return command cooldown message
     */
    @Getter
    private String  commandMessage = "&4Please wait &6{time} seconds &4before using the command again.";
    @Getter
    private boolean scoreboardsEnabled;

    private UnstuckCommand unstuck;
    private CycleTask      cTask;
    private UpdateTask     uTask;

    public CodexEngine() {
        setInstance();
        this.plugins = new HashSet<>();
    }

    public CodexEngine(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        setInstance();
        this.plugins = new HashSet<>();
    }

    private void setInstance() {
        instance = this;
        Codex.setPlugin(this);
        this.pluginManager = this.getServer().getPluginManager();
        try {
            if (new File("plugins/ProMCCore").exists()) {
                getLogger().info("Migrating ProMCCore to CodexCore");
                MigrationUtil.renameDirectory("plugins/ProMCCore", "plugins/CodexCore");
                MigrationUtil.replace("plugins/CodexCore/lang/messages_en.yml", "Core:", "CodexCore:");
            }
        } catch (Exception e) {
            getLogger().warning("Failed to migrate ProMCCore to Codex. " + e.getMessage());
        }
        try {
            if (new File("plugins/Codex").exists()) {
                getLogger().info("Migrating Codex to CodexCore");
                MigrationUtil.renameDirectory("plugins/Codex", "plugins/CodexCore");
                MigrationUtil.replace("plugins/CodexCore/lang/messages_en.yml", "Codex:", "CodexCore:");
            }
        } catch (Exception e) {
            getLogger().warning("Failed to migrate Codex to CodexCore. " + e.getMessage());
        }
        ItemUT.setEngine(this);
        Reflex.setEngine(this);
    }

    @NotNull
    public static CodexEngine get() {
        return instance;
    }

    final boolean loadCore() {
        unstuck = new UnstuckCommand();

        ConfigurationSerialization.registerClass(ItemBuilder.class);
        ConfigurationSerialization.registerClass(EnchantmentStorageBuilder.class, "Codex_EnchantmentStorageMeta");
        ConfigurationSerialization.registerClass(FireworkEffectBuilder.class, "Codex_FireworkEffectMeta");
        ConfigurationSerialization.registerClass(LeatherArmorBuilder.class, "Codex_LeatherArmorMeta");
        ConfigurationSerialization.registerClass(PotionDataBuilder.class, "Codex_PotionMeta");
        ConfigurationSerialization.registerClass(FireworkBuilder.class, "Codex_FireworkMeta");
        ConfigurationSerialization.registerClass(BookDataBuilder.class, "Codex_BookMeta");
        ConfigurationSerialization.registerClass(SkullBuilder.class, "Codex_SkullMeta");
        ConfigurationSerialization.registerClass(MapBuilder.class, "Codex_MapMeta");
        ConfigurationSerialization.registerClass(ItemBuilder.class, "Codex_Item");

        ConfigurationSerialization.registerClass(DarkRiseItemImpl.class, "DarkRiseItemImpl");
        ConfigurationSerialization.registerClass(DarkRiseItemImpl.DivineItemsMeta.class, "DarkRiseItemImpl_Divine");

        // Register bungee messaging channel
        getServer().getMessenger().registerOutgoingPluginChannel(this, BungeeUtil.CHANNEL);
        getServer().getMessenger().registerIncomingPluginChannel(this, BungeeUtil.CHANNEL, new BungeeListener());

        if (!this.setupNMS()) {
            this.error("Could not setup NMS version. Plugin will be disabled.");
            return false;
        }
        // This call actually sets up the NMS version in the NMSProvider
        try {
            NMSProvider.setup();
            getLogger().info("Using NMS implementation for version " + NMSProvider.getNms().getVersion());
        } catch (Exception e) {
            getLogger().severe("Failed to setup NMSProvider. Plugin will be disabled.");
            e.printStackTrace();
            return false;
        }

        MessageUtil.load(LegacyConfigManager.loadConfigFile(new File(getDataFolder() + File.separator + "lang",
                "messages_en.yml"), getResource("lang/messages_en.yml")), this);
        // Placeholder registration
        PlaceholderRegistry.load();

        setupManagers();

        return true;
    }

    protected void registerEvents() {
        getPluginManager().registerEvents(this, this);
        getPluginManager().registerEvents(new ArmorListener(), this);
        getPluginManager().registerEvents(unstuck, this);
        getPluginManager().registerEvents(new BoatListener(), this);
        getPluginManager().registerEvents(new InteractListener(cfg().getJYML()), this);
        getPluginManager().registerEvents(new JoinListener(this, cfg().getJYML()), this);
    }

    private void setupManagers() {
        this.hooksManager = new HookManager(this);
        this.hooksManager.setup();

        this.itemManager = new CodexItemManager(this);
        this.itemManager.init();

        this.packetManager = new PacketManager(this);
        this.packetManager.setup();

        this.actionsManager = new ActionsManager(this);
        this.actionsManager.setup();

        this.craftManager = new CraftManager(this);
        this.craftManager.setup();

        this.menuManager = new MenuManager(this);
        this.menuManager.setup();
    }

    private boolean setupNMS() {
        Version  current    = Version.CURRENT;
        String[] split      = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        String   rawVersion = split[split.length - 1];
        this.info("You are running MC version " + current + " (RAW: " + rawVersion + ")");
        if (current == null) {
            this.error("===== CodexCore Initialization Failure =====");
            this.error(rawVersion + " is not currently supported. Is this a new version of Spigot?");
            this.error("If this is a new version, please be patient and wait for a new build supporting the new version");
            this.error("If this is a version older than 1.16.5, sorry. We don't support <1.16.5");
            this.error("============================================");
            return false;
        }

        return true;
    }

    @Override
    public void enable() {
        EditorManager.setup();
        getLogger().info("CodexCore has been enabled!");
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

        commandMessage = this.cfg.getJYML()
                .getString("Settings.command-cooldown-message",
                        "&4Please wait &6{time} seconds &4before using the command again.");
        chatEnabled = this.cfg.getJYML().getBoolean("Features.chat-enabled", true);
        scoreboardsEnabled = this.cfg.getJYML().getBoolean("Features.scoreboards-enabled", true);

        // Load config data
        setupBungee();

        boolean debug = cfg().getJYML().getBoolean("debug", false);
        Debugger.setDebug(debug);

        if (chatEnabled) {
            new ChatCommander(this);
            new ChatListener(this);
        }
        setupScoreboards();

        this.lang = new CoreLang(this);
        this.lang.setup();
    }

    private void setupScoreboards() {
        if (scoreboardsEnabled) {
            new ScoreboardCommander(this);
            new BoardListener(this);
            cTask = new CycleTask(this);
            uTask = new UpdateTask(this);
        }
    }

    private void setupBungee() {
        BungeeUtil.setBungeeId(cfg().getJYML().getString("bungee_id", "server"));
        BungeeUtil.setBungee(cfg().getJYML().getBoolean("bungee", false));
        BungeeUtil.setPlugin(this);
    }

    @Override
    public void registerHooks() {
        try {
            this.vault = this.registerHook(Hooks.VAULT, VaultHK.class);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void registerCommands(@NotNull IGeneralCommand<CodexEngine> mainCommand) {
        PluginCommand unstuckCommand = getCommand("stuck");
        if (unstuckCommand != null) unstuckCommand.setExecutor(unstuck);
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


    void hookChild(@NotNull CodexPlugin<?> child) {
        this.plugins.add(child);
    }

    @NotNull
    public Set<CodexPlugin<?>> getChildPlugins() {
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
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * <p>Retrieves the configuration file for a plugin</p>
     * <p>If the config file hasn't been loaded yet, this will
     * load the file first.</p>
     * <p>Configs retrieved via this method are handled by Codex
     * and automatically saved when Codex disables.</p>
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
     * <p>Registers the Config with Codex for auto saving.</p>
     * <p>If the Config was already registered, this method will
     * not do anything.</p>
     *
     * @param config config to register
     */
    public void registerConfig(Config config) {
        configs.put(config.getFile().toLowerCase() + config.getPlugin().getName(), config);
    }
}
