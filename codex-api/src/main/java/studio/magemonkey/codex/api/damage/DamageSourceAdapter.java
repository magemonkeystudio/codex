package studio.magemonkey.codex.api.damage;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Access to Bukkit's damage sources, which exist from Minecraft 1.20.4. Use it through {@link DamageSources},
 * which picks the implementation that fits the running server.
 */
public interface DamageSourceAdapter {

    /**
     * @return whether the server has damage sources at all
     */
    boolean isSupported();

    /**
     * Damages the target with a real damage source, so that the damage event carries the wanted cause and the
     * death message fits it.
     * <p>
     * The cause is translated to the vanilla damage type closest to it. That type brings its vanilla rules
     * along: damage of a fire type does nothing to a target with fire resistance, for example.
     *
     * @param target the entity to damage
     * @param amount the amount of damage
     * @param cause  the cause the damage should have
     * @param source the entity the damage is attributed to, or null
     * @return true if the damage call was made. False if the server has no damage sources or the cause has no
     * matching damage type; nothing was done then, and the caller should deal the damage another way
     */
    boolean damage(@NotNull Damageable target, double amount, @NotNull DamageCause cause, @Nullable Entity source);

    /**
     * @return the key of the event's vanilla damage type, such as {@code minecraft:thorns}, or null if the
     * server has no damage sources
     */
    @Nullable String getDamageType(@NotNull EntityDamageEvent event);

    /**
     * The entity that touched the victim: the arrow of a bow shot, the attacker of a melee hit.
     *
     * @return the direct entity, or null if no entity was involved
     */
    @Nullable Entity getDirectEntity(@NotNull EntityDamageEvent event);

    /**
     * The entity responsible for the damage: the shooter of an arrow, the attacker of a melee hit.
     *
     * @return the causing entity, or null if no entity was involved
     */
    @Nullable Entity getCausingEntity(@NotNull EntityDamageEvent event);
}
