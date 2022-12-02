package mc.promcteam.engine.nms.packets.events;

import mc.promcteam.engine.manager.api.event.ICancellableEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class EnginePacketEvent extends ICancellableEvent {

    private Player reciever;
    private Object packet;

    public EnginePacketEvent(@NotNull Player reciever, @NotNull Object packet) {
        super(true);
        this.packet = packet;
        this.reciever = reciever;
    }

    @NotNull
    public Player getReciever() {
        return this.reciever;
    }

    @NotNull
    public Object getPacket() {
        return this.packet;
    }

    public void setPacket(@NotNull Object packet) {
        this.packet = packet;
    }
}
