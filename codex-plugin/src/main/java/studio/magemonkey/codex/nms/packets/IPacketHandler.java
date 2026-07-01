package studio.magemonkey.codex.nms.packets;

import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.api.events.EnginePlayerPacketEvent;

public interface IPacketHandler {
    void managePlayerPacket(@NotNull EnginePlayerPacketEvent event);
}
