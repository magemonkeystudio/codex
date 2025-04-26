package studio.magemonkey.codex.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.EOFException;
import java.util.UUID;

public class BungeeListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(BungeeUtil.CHANNEL))
            return;

        System.out.println("Received command on channel " + BungeeUtil.CHANNEL);
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        try {
            UUID id =
                    UUID.fromString(in.readUTF()); // We really don't need to do anything with this, aside from sending a response.
            System.out.println("ID: " + id);
            String senderServer = in.readUTF();
            System.out.println("Sender: " + senderServer);
            String command = in.readUTF();
            System.out.println("Command: " + command);

            if (command.equals("Broadcast")) {
                Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', in.readUTF()));
            }
        } catch (Exception e) {
            if (!(e instanceof EOFException))
                e.printStackTrace();
        }
    }
}
