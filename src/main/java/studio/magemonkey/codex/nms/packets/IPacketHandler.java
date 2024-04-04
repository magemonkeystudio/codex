package studio.magemonkey.codex.nms.packets;

import studio.magemonkey.codex.nms.packets.events.EnginePlayerPacketEvent;
import studio.magemonkey.codex.nms.packets.events.EngineServerPacketEvent;
import org.jetbrains.annotations.NotNull;

public interface IPacketHandler {

    void managePlayerPacket(@NotNull EnginePlayerPacketEvent event);

    void manageServerPacket(@NotNull EngineServerPacketEvent event);
}
