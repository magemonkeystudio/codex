package studio.magemonkey.codex.legacy;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.config.legacy.LegacyConfigManager;
import studio.magemonkey.codex.util.messages.MessageData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class RisePlugin extends JavaPlugin {

    public Logger log;

    public RisePlugin() {
        log = this.getLogger();
    }

    public void info(String msg) {
        log.info(msg);
    }

    public void error(String msg) {
        log.severe(msg);
    }

    public BukkitTask runSync(Runnable run) {
        return Bukkit.getScheduler().runTask(this, run);
    }

    public BukkitTask runTaskLater(double delay, Runnable run) {
        if (delay == 0)
            return runSync(run);
        return Bukkit.getScheduler().runTaskLater(this, run, ((long) delay * 20L));
    }

    public BukkitTask runTaskAsynchronously(Runnable run) {
        return Bukkit.getScheduler().runTaskAsynchronously(this, run);
    }


    public boolean checkPermission(CommandSender sender, String s) {
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(s)) {
            CodexEngine.get().getMessageUtil().sendMessage("noPermissions", sender, new MessageData("permission", s));
            return false;
        }

        return true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        loadLang();
    }

    public FileConfiguration getLang() {
        File lang = new File(getDataFolder() + File.separator + "lang", "lang_en.yml");
        if (!lang.exists()) {
            InputStream in = getResource("lang/lang_en.yml");
            if (in == null)
                return null;

            lang.getParentFile().mkdirs();
            try {
                lang.createNewFile();
                FileWriter writer = new FileWriter(lang);
                int        read;
                while ((read = in.read()) != -1) {
                    writer.write(read);
                }
                writer.close();
                in.close();
            } catch (IOException e) {
                getLogger().severe("Could not save lang_en.yml");
                e.printStackTrace();
            }
        }
        FileConfiguration conf = YamlConfiguration.loadConfiguration(lang);
        return conf;
    }

    public void loadLang() {
        FileConfiguration conf = getLang();
        if (conf == null)
            return;
        CodexEngine.get().getMessageUtil().load(conf, this);
    }

    public void reloadLang() {
        FileConfiguration conf = getLang();
        if (conf == null)
            return;
        CodexEngine.get().getMessageUtil().reload(conf, this);
    }

    public void reloadMessages() {
        FileConfiguration lang =
                LegacyConfigManager.loadConfigFile(new File(getDataFolder() + File.separator + "lang", "lang_en.yml"),
                        getResource("lang/lang_en.yml"));
        CodexEngine.get().getMessageUtil().reload(lang, this);
    }

}