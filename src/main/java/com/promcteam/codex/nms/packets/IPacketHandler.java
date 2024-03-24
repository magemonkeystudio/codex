package com.promcteam.codex.nms.packets;

import com.promcteam.codex.nms.packets.events.EnginePlayerPacketEvent;
import com.promcteam.codex.nms.packets.events.EngineServerPacketEvent;
import org.jetbrains.annotations.NotNull;

public interface IPacketHandler {

    void managePlayerPacket(@NotNull EnginePlayerPacketEvent event);

    void manageServerPacket(@NotNull EngineServerPacketEvent event);
}
