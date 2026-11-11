package studio.magemonkey.codex.api.damage;

import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * An open {@link DamageRequest} for one target, from {@link DamageContexts#open}. Closing it withdraws the
 * request. Always use it in a try-with-resources statement, so that it is closed even when the damage
 * event is cancelled or something throws.
 */
public final class DamageScope implements AutoCloseable {
    private final UUID              target;
    private final DamageRequest     request;
    private       EntityDamageEvent claimedBy;
    private       boolean           closed;

    DamageScope(@NotNull UUID target, @NotNull DamageRequest request) {
        this.target = target;
        this.request = request;
    }

    @NotNull
    public DamageRequest getRequest() {
        return request;
    }

    /**
     * Whether a damage event picked the request up. If it is still false after the call to
     * {@code damage()}, nothing read the request and the hit was handled as ordinary damage.
     */
    public boolean isClaimed() {
        return claimedBy != null;
    }

    public boolean isClosed() {
        return closed;
    }

    /**
     * Withdraws the request. Closing a scope twice does nothing.
     */
    @Override
    public void close() {
        if (closed) return;
        closed = true;
        DamageContexts.remove(this);
    }

    @NotNull UUID target() {
        return target;
    }

    EntityDamageEvent claimedBy() {
        return claimedBy;
    }

    void claim(@NotNull EntityDamageEvent event) {
        this.claimedBy = event;
    }
}
