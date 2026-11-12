package studio.magemonkey.codex.api.damage;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * For servers older than 1.20.4, which have no damage sources. It deals no damage and knows no damage type.
 * The entities of a hit are read from the event the way plugins have always done it.
 */
final class LegacyDamageSourceAdapter implements DamageSourceAdapter {

    @Override
    public boolean isSupported() {
        return false;
    }

    @Override
    public boolean damage(@NotNull Damageable target,
                          double amount,
                          @NotNull DamageCause cause,
                          @Nullable Entity source) {
        return false;
    }

    @Override
    @Nullable
    public String getDamageType(@NotNull EntityDamageEvent event) {
        return null;
    }

    @Override
    @Nullable
    public Entity getDirectEntity(@NotNull EntityDamageEvent event) {
        return event instanceof EntityDamageByEntityEvent ? ((EntityDamageByEntityEvent) event).getDamager() : null;
    }

    @Override
    @Nullable
    public Entity getCausingEntity(@NotNull EntityDamageEvent event) {
        Entity direct = getDirectEntity(event);
        if (direct instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) direct).getShooter();
            return shooter instanceof Entity ? (Entity) shooter : null;
        }
        return direct;
    }
}
