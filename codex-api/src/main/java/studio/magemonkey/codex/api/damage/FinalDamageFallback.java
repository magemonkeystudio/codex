package studio.magemonkey.codex.api.damage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;

/**
 * Fires {@link FinalDamageEvent} for every hit on a living entity that no provider fires it for, so that
 * consumers always listen to that one event and never ask whether a provider is present.
 * <p>
 * Codex registers this listener when it enables. It listens to {@link EntityDamageEvent} at
 * {@link EventPriority#HIGHEST}, ignoring cancelled events, and for each hit:
 * <ol>
 *     <li>does nothing if {@link FinalDamageEvent#willFire(EntityDamageEvent)} says a provider will fire
 *     the event, or if the target is not a {@link LivingEntity};</li>
 *     <li>otherwise fires a {@link FinalDamageEvent} whose only damage type is
 *     {@link FinalDamageEvent#DEFAULT_TYPE}, holding {@link EntityDamageEvent#getDamage()};</li>
 *     <li>afterwards cancels the Bukkit event if the {@code FinalDamageEvent} was cancelled, or else sets the
 *     Bukkit damage to the new total, but only if the total changed. An untouched hit leaves the Bukkit
 *     event alone, so its vanilla damage modifiers stay as they are.</li>
 * </ol>
 * The request comes from {@link DamageContexts#peek(EntityDamageEvent)}, never {@code claim}: the
 * fallback only reads it, and leaves it to whoever calculates the hit.
 *
 * <h2>What the fallback fires for</h2>
 * <ul>
 *     <li><b>{@link WeaponStatsMode#RAW} requests still fire.</b> RAW asks the calculating plugin not to
 *     bring the attacker's weapon stats into the hit. It does not hide the hit from consumers: without a
 *     provider, skill plugins such as Fabled have always seen these hits and run their triggers on them,
 *     and they keep doing so. A consumer that wants to leave RAW hits alone checks
 *     {@link FinalDamageEvent#getWeaponStatsMode()}.</li>
 *     <li><b>Hits of 0 damage or less, and fake damage events</b> (such as those a plugin fires only to ask
 *     whether an attack would be allowed) are not filtered out. Consumers filter what they do not want.</li>
 * </ul>
 */
public final class FinalDamageFallback implements Listener {
    /** How far the total may drift from the Bukkit damage before it counts as changed. */
    static final double EPSILON = 1e-9;

    @NotNull
    private final PluginManager pluginManager;

    /**
     * @param pluginManager the plugin manager the {@link FinalDamageEvent} is called through
     */
    public FinalDamageFallback(@NotNull PluginManager pluginManager) {
        this.pluginManager = Objects.requireNonNull(pluginManager, "pluginManager");
    }

    /**
     * Fires {@link FinalDamageEvent} for the hit unless a provider will. See the class description.
     *
     * @param event the Bukkit damage event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (!Double.isFinite(event.getDamage())) return;
        if (FinalDamageEvent.willFire(event)) return;

        LivingEntity victim     = (LivingEntity) event.getEntity();
        LivingEntity attacker   = null;
        Projectile   projectile = null;
        if (event instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager instanceof Projectile) {
                projectile = (Projectile) damager;
                if (projectile.getShooter() instanceof LivingEntity) {
                    attacker = (LivingEntity) projectile.getShooter();
                }
            } else if (damager instanceof LivingEntity) {
                attacker = (LivingEntity) damager;
            }
        }

        DamageRequest request = DamageContexts.peek(event);
        DamageOrigin origin = request != null ? request.getOrigin() : inferOrigin(attacker, projectile);
        WeaponStatsMode mode = request != null ? request.getWeaponStats() : WeaponStatsMode.ADD;
        // FinalDamageEvent holds no negative amounts: a negative hit is passed on as 0, and only written back
        // if a listener changes that 0.
        double damage = Math.max(0, event.getDamage());

        FinalDamageEvent finalEvent = new FinalDamageEvent(victim, attacker, projectile, event.getCause(), event,
                request, origin, mode,
                Collections.singletonMap(FinalDamageEvent.DEFAULT_TYPE, damage));
        pluginManager.callEvent(finalEvent);

        if (finalEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        double total = finalEvent.getTotalDamage();
        if (Math.abs(total - damage) > EPSILON) {
            event.setDamage(total);
        }
    }

    /**
     * The origin of a hit nobody described: melee for a living attacker, projectile for a projectile with a
     * living shooter, the environment otherwise.
     */
    @NotNull
    static DamageOrigin inferOrigin(@Nullable LivingEntity attacker, @Nullable Projectile projectile) {
        if (attacker == null) return DamageOrigin.ENVIRONMENT;
        return projectile != null ? DamageOrigin.WEAPON_PROJECTILE : DamageOrigin.WEAPON_MELEE;
    }
}
