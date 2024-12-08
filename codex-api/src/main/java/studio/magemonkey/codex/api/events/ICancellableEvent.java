package studio.magemonkey.codex.api.events;

import org.bukkit.event.Cancellable;

public abstract class ICancellableEvent extends IEvent implements Cancellable {

    private boolean cancelled = false;

    public ICancellableEvent() {
        this(false);
    }

    public ICancellableEvent(boolean async) {
        super(async);
        //this.setCancelled(false);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
