package com.promcteam.risecore;

import com.promcteam.codex.bungee.BungeeListener;
import com.promcteam.codex.bungee.BungeeUtil;
import com.promcteam.risecore.command.ReloadCommand;
import com.promcteam.risecore.command.UnstuckCommand;
import com.promcteam.risecore.config.CommandBlock;
import com.promcteam.risecore.item.DarkRiseItemImpl;
import com.promcteam.risecore.legacy.cmds.DelayedCommand;
import com.promcteam.risecore.legacy.util.Init;
import com.promcteam.risecore.legacy.util.message.MessageUtil;
import com.promcteam.risecore.listeners.BoatListener;
import com.promcteam.risecore.listeners.InteractListener;
import com.promcteam.risecore.listeners.JoinListener;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Core extends JavaPlugin {
    @Getter
    private static Core              instance;
    public static  Logger            log;
    public static  FileConfiguration config;

    public static boolean IS_BUNGEE = false;
    public static String  BUNGEE_ID = "server";

    @Getter
    private static List<DelayedCommand> firstJoin = new ArrayList<>(), onJoin = new ArrayList<>();
    @Getter
    private static List<CommandBlock> onInteract = new ArrayList<>();

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onEnable() {
        log = this.getLogger();

        config = ConfigManager.loadConfigFile(new File(getDataFolder(), "config.yml"), getResource("config.yml"));
        Debugger.setDebug(config.getBoolean("debug", false));
        register();
//        Vault.init();
        Init.load();
    }

    public static List<CommandBlock> getOnInteract(Material type) {
        ArrayList<CommandBlock> ret = new ArrayList<>();

        for (CommandBlock cmd : onInteract) {
            if (cmd.getMaterial() == type) {
                ret.add(cmd);
            }
        }

        return ret;
    }

    private void register() {
        UnstuckCommand unstuck = new UnstuckCommand();
        getCommand("corereload").setExecutor(new ReloadCommand());
        getCommand("stuck").setExecutor(unstuck);
        ConfigurationSerialization.registerClass(DarkRiseItemImpl.class, "DarkRiseItemImpl");
        ConfigurationSerialization.registerClass(DarkRiseItemImpl.DivineItemsMeta.class, "DarkRiseItemImpl_Divine");

        reloadConfiguration();
        BUNGEE_ID = config.getString("bungee_id", "server");
        IS_BUNGEE = config.getBoolean("bungee", false);
        getServer().getMessenger().registerOutgoingPluginChannel(this, BungeeUtil.CHANNEL);
        getServer().getMessenger().registerIncomingPluginChannel(this, BungeeUtil.CHANNEL, new BungeeListener());

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new JoinListener(), this);
        pm.registerEvents(new InteractListener(), this);
        pm.registerEvents(new BoatListener(), this);
        pm.registerEvents(unstuck, this);

    }

    @SuppressWarnings("unchecked")
    public void reloadConfiguration() {
        config = ConfigManager.loadConfigFile(new File(getDataFolder(), "config.yml"), getResource("config.yml"));
        MessageUtil.load(ConfigManager.loadConfigFile(new File(getDataFolder() + File.separator + "lang",
                "lang_en.yml"), getResource("lang/lang_en.yml")), this);
        firstJoin = DelayedCommand.deserializeMapList((List<Map<String, Object>>) config.get("onFirstJoin"));
        onJoin = DelayedCommand.deserializeMapList((List<Map<String, Object>>) config.get("onJoin"));
        onInteract = new ArrayList<>();
        List<Map<String, Object>> interactList = (List<Map<String, Object>>) config.get("onInteract");
        if (interactList != null) {
            for (Map<String, Object> cmd : interactList) {
                onInteract.add(new CommandBlock(cmd));
            }
        }
    }

}