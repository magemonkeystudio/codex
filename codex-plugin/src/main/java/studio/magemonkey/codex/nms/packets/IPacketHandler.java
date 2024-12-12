package studio.magemonkey.codex.nms.packets;

import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.api.events.EnginePlayerPacketEvent;
import studio.magemonkey.codex.api.events.EngineServerPacketEvent;

public interface IPacketHandler {

    void managePlayerPacket(@NotNull EnginePlayerPacketEvent event);

    void manageServerPacket(@NotNull EngineServerPacketEvent event);
}
