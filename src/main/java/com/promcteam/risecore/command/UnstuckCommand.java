package com.promcteam.risecore.command;

import com.promcteam.risecore.ConfigManager;
import com.promcteam.risecore.Core;
import com.promcteam.risecore.legacy.util.message.MessageData;
import com.promcteam.risecore.legacy.util.message.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class UnstuckCommand implements CommandExecutor, Listener {
    /*
    unstuck:
      cooldown: 30
      warmup: 5
     */

    private final HashMap<UUID, LinkedList<Location>> locs      = new HashMap<>();
    private final HashMap<UUID, Location>             warmingUp = new HashMap<>();
    private final HashMap<UUID, Long>                 cooldown  = new HashMap<>();
    private final HashMap<UUID, BukkitTask> tasks     = new HashMap<>();

    private final FileConfiguration config;
    private final File              destFile;

    public UnstuckCommand() {
        destFile = new File(Core.getInstance().getDataFolder(), "unstuck.log");
        config = ConfigManager.loadConfigFile(destFile, null);
    }

    public void saveFile() {
        try {
            config.save(destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void purge() {
        if (!config.contains("lastPurge"))
            config.set("lastPurge", System.currentTimeMillis());
        long lastPurge = config.getLong("lastPurge", System.currentTimeMillis());
        long diff      = System.currentTimeMillis() - lastPurge;
        long days      = TimeUnit.MILLISECONDS.toDays(diff);
        long left      = diff - TimeUnit.DAYS.toMillis(days);
        if (days >= 3 && left >= 0) {
            config.set("locs", new ArrayList<>());
            config.set("lastPurge", System.currentTimeMillis());
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You have to be a player to use that command!");
            return true;
        }

        Player player = (Player) sender;
        UUID   id     = player.getUniqueId();

        if (cooldown.containsKey(id)) {
            long time = cooldown.get(id) - System.currentTimeMillis();
            if (time > 0) {
                long seconds = TimeUnit.MILLISECONDS.toSeconds(time);
                MessageUtil.sendMessage("core.commands.unstuck.err.cooldown", sender, new MessageData("time", seconds));
                return true;
            } else
                cooldown.remove(id);
        }

        if (warmingUp.containsKey(id)) {
            MessageUtil.sendMessage("core.commands.unstuck.err.warmingUp", sender);
            return true;
        }

        warmingUp.put(id, player.getLocation().getBlock().getLocation());
        MessageUtil.sendMessage("core.commands.unstuck.warmup",
                sender,
                new MessageData("time", Core.config.getLong("unstuck.warmup")));

        String locStr = locToString(player.getLocation());
        Core.log.info("STUCK - " + player.getName() + " executed '/stuck' at " + locStr);

        purge();

        List<String> list = config.getStringList("locs");
        if (list == null) list = new ArrayList<>();

        list.add(locStr);
        config.set("locs", list);
        saveFile();

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(locs.get(player.getUniqueId()).getFirst());
                tasks.remove(id);
                warmingUp.remove(id);
                cooldown.put(id,
                        TimeUnit.SECONDS.toMillis(Core.config.getLong("unstuck.cooldown"))
                                + System.currentTimeMillis());
                MessageUtil.sendMessage("core.commands.unstuck.teleported", sender);
            }
        }.runTaskLater(Core.getInstance(), Core.config.getLong("unstuck.warmup") * 20);

        tasks.put(id, task);

        return true;
    }

    @EventHandler
    public void cancelWarmup(PlayerMoveEvent event) {
        if (warmingUp.containsKey(event.getPlayer().getUniqueId())) {
            boolean same = event.getPlayer()
                    .getLocation()
                    .getBlock()
                    .getLocation()
                    .equals(warmingUp.get(event.getPlayer().getUniqueId()));
            if (!same) {
                BukkitTask task = tasks.get(event.getPlayer().getUniqueId());
                if (task != null) task.cancel();
                warmingUp.remove(event.getPlayer().getUniqueId());
                MessageUtil.sendMessage("core.commands.unstuck.err.cancelled", event.getPlayer());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void move(PlayerMoveEvent event) {
        boolean notGround = event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).isPassable();
        if (notGround) return;

        Player p = event.getPlayer();
        if (!locs.containsKey(p.getUniqueId())) locs.put(p.getUniqueId(), new LinkedList<>());

        LinkedList<Location> pLocs = locs.get(p.getUniqueId());
        if (pLocs.contains(p.getLocation().getBlock().getLocation())) return;

        pLocs.add(p.getLocation().getBlock().getLocation());

        if (pLocs.size() > 10)
            pLocs.removeFirst();

        locs.put(p.getUniqueId(), pLocs);
    }


    public String locToString(Location loc) {
        String builder = loc.getWorld().getName() + "," + loc.getX()
                + "," + loc.getY()
                + "," + loc.getZ();
        return builder;
    }
}
