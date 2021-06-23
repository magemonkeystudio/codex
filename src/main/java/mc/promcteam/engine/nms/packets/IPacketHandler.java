package mc.promcteam.engine.nms.packets;

import mc.promcteam.engine.nms.packets.events.EnginePlayerPacketEvent;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.nms.packets.events.EngineServerPacketEvent;

public interface IPacketHandler {

	void managePlayerPacket(@NotNull EnginePlayerPacketEvent event);
	
	void manageServerPacket(@NotNull EngineServerPacketEvent event);
}
