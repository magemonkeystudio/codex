package studio.magemonkey.codex.bungee;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import studio.magemonkey.codex.CodexEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BungeeUtil {

    public static final  String                    CHANNEL      = "magemonkey:codex";
    private static final List<ByteArrayDataOutput> queued       = new ArrayList<>();
    private static       boolean                   queueRunning = false;

    static {
        queue();
    }

    public static boolean sendMessage(String... data) {
        return sendMessage(CHANNEL, data);
    }

    public static boolean sendMessage(String channel, String[] data) {
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        return sendMessage(channel, player, data);
    }

    public static boolean sendMessage(String channel, Player sender, String... data) {
        return sendMessage(channel, null, sender, data);
    }

    private static boolean sendMessage(String channel, UUID id, Player sender, String... data) {
        if (!CodexEngine.IS_BUNGEE) return false;
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF(id == null ? UUID.randomUUID().toString() : id.toString());
        out.writeUTF(CodexEngine.BUNGEE_ID);
//        out.writeUTF(event);
        for (String dat : data)
            out.writeUTF(dat);

//        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (sender == null)
            sender = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

        if (sender == null) { //QUEUE
            queued.add(out);
            queue();
            return true;
        }

        sender.sendPluginMessage(CodexEngine.get(), channel, out.toByteArray());
        return true;
    }

    private static void sendMessage(ByteArrayDataOutput out) {
        sendMessage(CHANNEL, out);
    }

    private static void sendMessage(String channel, ByteArrayDataOutput out) {
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

        if (player == null)
            return;

        sendMessage(channel, player, out);
    }

    private static void sendMessage(String channel, Player sender, ByteArrayDataOutput out) {
        sender.sendPluginMessage(CodexEngine.get(), channel, out.toByteArray());
    }

    public static void broadcastMessage(String message) {
        sendMessage("Broadcast", message);
    }

    public static void sendPlayerMessage(String target, String message) {
        sendMessage("PlayerMessage", target, message);
    }

    public static void sendResponse(UUID id, String responseType) {
        sendResponse(CHANNEL, id, responseType);
    }

    public static void sendResponse(String channel, UUID id, String responseType) {
        sendMessage(channel, id, null, responseType);
    }

    private static void sendFirstQueue() {
        if (queued.isEmpty()) return;

        ByteArrayDataOutput out = queued.get(0);
        if (out == null) {
            queued.remove(0);
            return;
        }
        sendMessage(out);

        queued.remove(out);
    }

    private static void queue() {
        if (queueRunning)
            return;

        new BukkitRunnable() {
            public void run() {
                if (Bukkit.getOnlinePlayers().isEmpty()) return;

                while (!queued.isEmpty()) {
                    sendFirstQueue();
                }

                queueRunning = false;
                this.cancel();
            }
        }.runTaskTimerAsynchronously(CodexEngine.get(), 20L, 20L);
    }

}
