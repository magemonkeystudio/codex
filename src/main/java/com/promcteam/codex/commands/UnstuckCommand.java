package com.promcteam.codex.commands;

import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.config.legacy.LegacyConfigManager;
import com.promcteam.codex.util.messages.MessageData;
import com.promcteam.codex.util.messages.MessageUtil;
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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/*
unstuck:
  cooldown: 30
  warmup: 5
 */
public class UnstuckCommand implements CommandExecutor, Listener {
    private final Map<UUID, LinkedList<Location>> locs      = new HashMap<>();
    private final Map<UUID, Location>             warmingUp = new HashMap<>();
    private final Map<UUID, Long>                 cooldown  = new HashMap<>();
    private final Map<UUID, BukkitTask>           tasks     = new HashMap<>();

    private final FileConfiguration config;
    private final File              destFile;

    public UnstuckCommand() {
        destFile = new File(CodexEngine.get().getDataFolder(), "unstuck.log");
        config = LegacyConfigManager.loadConfigFile(destFile, null);
    }

    private void saveFile() {
        try {
            config.save(destFile);
        } catch (IOException e) {
            CodexEngine.get().getLogger().warning("Failed to save unstuck file! " + e.getMessage());
        }
    }

    /**
     * Purge old locations every 3 days or more
     */
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
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String[] args) {
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
                MessageUtil.sendMessage("general.commands.unstuck.err.cooldown", sender, new MessageData("time", seconds));
                return true;
            } else
                cooldown.remove(id);
        }

        if (warmingUp.containsKey(id)) {
            MessageUtil.sendMessage("general.commands.unstuck.err.warmingUp", sender);
            return true;
        }

        warmingUp.put(id, player.getLocation().getBlock().getLocation());
        MessageUtil.sendMessage("general.commands.unstuck.warmup",
                sender,
                new MessageData("time", CodexEngine.get().cfg().getJYML().getLong("unstuck.warmup")));

        String locStr = locToString(player.getLocation());
        CodexEngine.get().getLogger().info("STUCK - " + player.getName() + " executed '/stuck' at " + locStr);

        purge();

        List<String> list = config.getStringList("locs");

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
                        TimeUnit.SECONDS.toMillis(CodexEngine.get().cfg().getJYML().getLong("unstuck.cooldown"))
                                + System.currentTimeMillis());
                MessageUtil.sendMessage("general.commands.unstuck.teleported", sender);
            }
        }.runTaskLater(CodexEngine.get(), CodexEngine.get().cfg().getJYML().getLong("unstuck.warmup") * 20);

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
                MessageUtil.sendMessage("general.commands.unstuck.err.cancelled", event.getPlayer());
            }
        }
    }

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
