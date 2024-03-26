package com.promcteam.codex.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class BungeeCore extends Plugin implements Listener {
    public void onEnable() {
        getProxy().registerChannel(BungeeUtil.CHANNEL);
        getProxy().getPluginManager().registerListener(this, this);
        getLogger().info("Codex-Bungee has been enabled!");
    }

    @EventHandler
    public void incoming(PluginMessageEvent event) {
        if (!event.getTag().equals(BungeeUtil.CHANNEL))
            return;
        Connection sender   = event.getSender();
        Connection receiver = event.getReceiver();
        byte[]     data     = event.getData();
        String     tag      = event.getTag();

        String dat = "";

        ByteArrayDataInput in       = ByteStreams.newDataInput(data);
        byte[]             msgbytes = new byte[data.length];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        String          read;
        try {
            String idstr   = msgin.readUTF();
            UUID   id      = UUID.fromString(idstr);
            String server  = msgin.readUTF();
            String command = msgin.readUTF();


            if (command.equals("Broadcast")) {
                for (ServerInfo info : getProxy().getServers().values()) {
                    info.sendData(event.getTag(), event.getData(), false);
                }
            } else if (command.equals("PlayerMessage")) {
                String        target       = msgin.readUTF();
                ProxiedPlayer targetPlayer = getProxy().getPlayer(target);
                if (targetPlayer != null && targetPlayer.isConnected()) {
                    targetPlayer.sendMessage(TextComponent.fromLegacyText(msgin.readUTF()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
