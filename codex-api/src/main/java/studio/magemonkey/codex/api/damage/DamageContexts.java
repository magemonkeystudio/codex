package studio.magemonkey.codex.api.damage;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Hands a {@link DamageRequest} from the plugin that deals a hit to the plugin that calculates it.
 * <p>
 * The dealer opens a scope around its call to {@code damage()}:
 * <pre>{@code
 * try (DamageScope scope = DamageContexts.open(target, request)) {
 *     target.damage(request.getTotal(), caster);
 * }
 * }</pre>
 * A listener of the resulting damage event asks for the request with {@link #claim(EntityDamageEvent)}.
 * <p>
 * The request belongs to the first damage event of the target that claims it. Every listener of that same
 * event gets it; any other event gets nothing, whether it is a second hit on the same target from inside
 * the first one, or a hit on a different entity. If nothing claims the request, nothing happens: the hit
 * is ordinary damage.
 * <p>
 * This class does not listen to anything and is not thread safe. Use it from the server thread only.
 */
public final class DamageContexts {
    private static final Map<UUID, Deque<DamageScope>> OPEN = new HashMap<>();

    private DamageContexts() {}

    /**
     * Announces a hit on the target. Close the returned scope when the call to {@code damage()} returns.
     *
     * @param target  the entity about to be damaged
     * @param request the description of the hit
     * @return the scope to close
     */
    @NotNull
    public static DamageScope open(@NotNull Entity target, @NotNull DamageRequest request) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(request, "request");
        DamageScope scope = new DamageScope(target.getUniqueId(), request);
        OPEN.computeIfAbsent(scope.target(), id -> new ArrayDeque<>()).push(scope);
        return scope;
    }

    /**
     * Returns the request that describes this damage event, if there is one.
     * <p>
     * The first event to ask takes the most recently opened request for its entity that has not been taken
     * yet. Asking again with the same event returns the same request, so several listeners can read it.
     * <p>
     * Do not call this for an event that is not a real hit, such as one a plugin fires only to ask whether
     * an attack would be allowed. It would take the request away from the real hit.
     *
     * @param event the damage event being handled
     * @return the request for this event, or null if the event was not announced
     */
    @Nullable
    public static DamageRequest claim(@NotNull EntityDamageEvent event) {
        Deque<DamageScope> scopes = OPEN.get(event.getEntity().getUniqueId());
        if (scopes == null) return null;

        // Newest first. An event that already holds a request keeps it, even if a newer scope has been
        // opened on the same entity since.
        for (DamageScope scope : scopes) {
            if (scope.claimedBy() == event) return scope.getRequest();
        }
        for (DamageScope scope : scopes) {
            if (scope.claimedBy() == null) {
                scope.claim(event);
                return scope.getRequest();
            }
        }
        return null;
    }

    /**
     * Whether a request is open for the entity and has not been claimed yet.
     */
    public static boolean isAnnounced(@NotNull Entity target) {
        Deque<DamageScope> scopes = OPEN.get(target.getUniqueId());
        if (scopes == null) return false;
        for (DamageScope scope : scopes) {
            if (scope.claimedBy() == null) return true;
        }
        return false;
    }

    static void remove(@NotNull DamageScope scope) {
        Deque<DamageScope> scopes = OPEN.get(scope.target());
        if (scopes == null) return;
        for (Iterator<DamageScope> it = scopes.iterator(); it.hasNext(); ) {
            if (it.next() == scope) {
                it.remove();
                break;
            }
        }
        if (scopes.isEmpty()) OPEN.remove(scope.target());
    }
}
