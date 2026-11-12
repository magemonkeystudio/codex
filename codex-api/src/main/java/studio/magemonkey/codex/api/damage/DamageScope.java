package studio.magemonkey.codex.api.damage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * An open {@link DamageRequest} for one target, from {@link DamageContexts#open}. Closing it withdraws the
 * request. Always use it in a try-with-resources statement, so that it is closed even when the damage
 * event is cancelled or something throws.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class DamageScope implements AutoCloseable {
    @Getter(AccessLevel.PACKAGE)
    @NotNull
    private final UUID              target;
    @Getter
    @NotNull
    private final DamageRequest     request;
    /** The event that claimed the request, or null while nothing has. */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private       EntityDamageEvent claimedBy;
    @Getter
    private       boolean           closed;

    /**
     * Whether a damage event picked the request up. If it is still false after the call to
     * {@code damage()}, nothing read the request and the hit was handled as ordinary damage.
     */
    public boolean isClaimed() {
        return claimedBy != null;
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
}
