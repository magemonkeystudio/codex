package mc.promcteam.engine.nms.packets;

import mc.promcteam.engine.nms.packets.events.EnginePlayerPacketEvent;
import mc.promcteam.engine.nms.packets.events.EngineServerPacketEvent;
import org.jetbrains.annotations.NotNull;

public interface IPacketHandler {

    void managePlayerPacket(@NotNull EnginePlayerPacketEvent event);

    void manageServerPacket(@NotNull EngineServerPacketEvent event);
}
