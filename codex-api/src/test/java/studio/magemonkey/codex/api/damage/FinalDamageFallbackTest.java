package studio.magemonkey.codex.api.damage;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.projectiles.BlockProjectileSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

class FinalDamageFallbackTest {
    private final LivingEntity           victim     = living();
    private final LivingEntity           attacker   = living();
    private final PluginManager          manager    = mock(PluginManager.class);
    private final List<FinalDamageEvent> fired      = new ArrayList<>();
    private final List<Plugin>           registered = new ArrayList<>();
    private final List<DamageScope>      scopes     = new ArrayList<>();
    private final FinalDamageFallback    fallback   = new FinalDamageFallback(manager);

    /** What the listeners of the fired event do. */
    private Consumer<FinalDamageEvent> listener = event -> {};

    FinalDamageFallbackTest() {
        doAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            if (event instanceof FinalDamageEvent) {
                fired.add((FinalDamageEvent) event);
                listener.accept((FinalDamageEvent) event);
            }
            return null;
        }).when(manager).callEvent(any(Event.class));
    }

    @AfterEach
    void cleanUp() {
        registered.forEach(FinalDamageEvent::unregisterProvider);
        scopes.forEach(DamageScope::close);
    }

    private static LivingEntity living() {
        LivingEntity entity = mock(LivingEntity.class);
        when(entity.getUniqueId()).thenReturn(UUID.randomUUID());
        return entity;
    }

    private static EntityDamageEvent damage(Entity target, DamageCause cause, double amount) {
        EntityDamageEvent event = mock(EntityDamageEvent.class);
        when(event.getEntity()).thenReturn(target);
        when(event.getCause()).thenReturn(cause);
        when(event.getDamage()).thenReturn(amount);
        return event;
    }

    private static EntityDamageByEntityEvent damageBy(Entity target, Entity damager, DamageCause cause,
                                                      double amount) {
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(target);
        when(event.getDamager()).thenReturn(damager);
        when(event.getCause()).thenReturn(cause);
        when(event.getDamage()).thenReturn(amount);
        return event;
    }

    private void provider(boolean enabled, boolean fires) {
        Plugin plugin = mock(Plugin.class);
        when(plugin.isEnabled()).thenReturn(enabled);
        registered.add(plugin);
        FinalDamageEvent.registerProvider(plugin, event -> fires);
    }

    private DamageRequest announce(Entity target, DamageRequest request) {
        scopes.add(DamageContexts.open(target, request));
        return request;
    }

    // --- When it fires --------------------------------------------------------------------------------------

    @Test
    void firesOnce_whenNoProviderIsRegistered() {
        EntityDamageEvent event = damage(victim, DamageCause.FALL, 4);

        fallback.onDamage(event);

        assertEquals(1, fired.size());
        FinalDamageEvent finalEvent = fired.get(0);
        assertSame(victim, finalEvent.getVictim());
        assertNull(finalEvent.getAttacker());
        assertNull(finalEvent.getProjectile());
        assertEquals(DamageCause.FALL, finalEvent.getCause());
        assertSame(event, finalEvent.getDamageEvent());
        assertNull(finalEvent.getRequest());
        assertEquals(WeaponStatsMode.ADD, finalEvent.getWeaponStatsMode());
        assertEquals(1, finalEvent.getDamages().size());
        assertEquals(4, finalEvent.getDamage(FinalDamageEvent.DEFAULT_TYPE));
    }

    @Test
    void doesNotFire_whenAProviderWillFire() {
        provider(true, true);

        fallback.onDamage(damageBy(victim, attacker, DamageCause.ENTITY_ATTACK, 5));

        assertTrue(fired.isEmpty());
        verify(manager, never()).callEvent(any());
    }

    @Test
    void fires_whenTheRegisteredProviderIsDisabled() {
        provider(false, true);

        fallback.onDamage(damageBy(victim, attacker, DamageCause.ENTITY_ATTACK, 5));

        assertEquals(1, fired.size());
    }

    @Test
    void fires_whenTheProviderSaysNo() {
        provider(true, false);

        fallback.onDamage(damageBy(victim, attacker, DamageCause.ENTITY_ATTACK, 5));

        assertEquals(1, fired.size());
    }

    @Test
    void ignoresTargetsThatAreNotLiving() {
        Entity boat = mock(Entity.class);
        EntityDamageEvent event = damageBy(boat, attacker, DamageCause.ENTITY_ATTACK, 5);

        fallback.onDamage(event);

        assertTrue(fired.isEmpty());
        verify(event, never()).setDamage(anyDouble());
        verify(event, never()).setCancelled(anyBoolean());
    }

    @Test
    void firesForZeroDamage() {
        fallback.onDamage(damage(victim, DamageCause.CUSTOM, 0));

        assertEquals(1, fired.size());
        assertEquals(0, fired.get(0).getTotalDamage());
    }

    // --- Writing back ---------------------------------------------------------------------------------------

    @Test
    void writesBackAChangedTotal() {
        listener = event -> event.setTotalDamage(event.getTotalDamage() * 2);
        EntityDamageEvent event = damageBy(victim, attacker, DamageCause.ENTITY_ATTACK, 3);

        fallback.onDamage(event);

        verify(event).setDamage(6.0);
        verify(event, never()).setCancelled(anyBoolean());
    }

    @Test
    void writesBackDamageAddedInAnotherType() {
        listener = event -> event.setDamage("fire", 2);
        EntityDamageEvent event = damage(victim, DamageCause.FIRE, 3);

        fallback.onDamage(event);

        verify(event).setDamage(5.0);
    }

    @Test
    void leavesTheBukkitEventAlone_whenNothingChanged() {
        listener = event -> event.setTotalDamage(event.getTotalDamage() + FinalDamageFallback.EPSILON / 10);
        EntityDamageEvent event = damageBy(victim, attacker, DamageCause.ENTITY_ATTACK, 3);

        fallback.onDamage(event);

        assertEquals(1, fired.size());
        verify(event, never()).setDamage(anyDouble());
        verify(event, never()).setCancelled(anyBoolean());
    }

    @Test
    void cancelPropagates() {
        listener = event -> {
            event.setTotalDamage(10);
            event.setCancelled(true);
        };
        EntityDamageEvent event = damageBy(victim, attacker, DamageCause.ENTITY_ATTACK, 3);

        fallback.onDamage(event);

        verify(event).setCancelled(true);
        verify(event, never()).setDamage(anyDouble());
    }

    @Test
    void negativeDamage_isPassedOnAsZero_andLeftAloneWhenUnchanged() {
        EntityDamageEvent event = damage(victim, DamageCause.CUSTOM, -2);

        fallback.onDamage(event);

        assertEquals(0, fired.get(0).getTotalDamage());
        verify(event, never()).setDamage(anyDouble());
    }

    @Test
    void infiniteDamage_isSkipped() {
        fallback.onDamage(damage(victim, DamageCause.VOID, Double.POSITIVE_INFINITY));

        assertTrue(fired.isEmpty());
    }

    // --- The request ----------------------------------------------------------------------------------------

    @Test
    void peeksTheRequest_andLeavesItUnclaimed() {
        DamageRequest request = announce(victim, DamageRequest.builder()
                .origin(DamageOrigin.SKILL)
                .weaponStats(WeaponStatsMode.REPLACE)
                .amount("fire", 3)
                .build());
        EntityDamageEvent event = damageBy(victim, attacker, DamageCause.ENTITY_ATTACK, 3);

        fallback.onDamage(event);

        FinalDamageEvent finalEvent = fired.get(0);
        assertSame(request, finalEvent.getRequest());
        assertEquals(DamageOrigin.SKILL, finalEvent.getOrigin());
        assertEquals(WeaponStatsMode.REPLACE, finalEvent.getWeaponStatsMode());
        assertTrue(DamageContexts.isAnnounced(victim), "the fallback must not claim the request");
        assertSame(request, DamageContexts.claim(event));
    }

    @Test
    void rawRequest_stillFires() {
        DamageRequest request = announce(victim, DamageRequest.builder()
                .origin(DamageOrigin.SKILL)
                .weaponStats(WeaponStatsMode.RAW)
                .amount("default", 7)
                .build());

        fallback.onDamage(damageBy(victim, attacker, DamageCause.ENTITY_ATTACK, 7));

        assertEquals(1, fired.size());
        assertSame(request, fired.get(0).getRequest());
        assertEquals(WeaponStatsMode.RAW, fired.get(0).getWeaponStatsMode());
    }

    // --- Attacker, projectile and origin --------------------------------------------------------------------

    @Test
    void livingDamager_isMeleeAttacker() {
        fallback.onDamage(damageBy(victim, attacker, DamageCause.ENTITY_ATTACK, 2));

        FinalDamageEvent finalEvent = fired.get(0);
        assertSame(attacker, finalEvent.getAttacker());
        assertNull(finalEvent.getProjectile());
        assertEquals(DamageOrigin.WEAPON_MELEE, finalEvent.getOrigin());
    }

    @Test
    void projectileWithLivingShooter_isProjectileFromTheShooter() {
        Arrow arrow = mock(Arrow.class);
        when(arrow.getShooter()).thenReturn(attacker);

        fallback.onDamage(damageBy(victim, arrow, DamageCause.PROJECTILE, 2));

        FinalDamageEvent finalEvent = fired.get(0);
        assertSame(attacker, finalEvent.getAttacker());
        assertSame(arrow, finalEvent.getProjectile());
        assertEquals(DamageOrigin.WEAPON_PROJECTILE, finalEvent.getOrigin());
    }

    @Test
    void projectileFromADispenser_isEnvironment() {
        Arrow arrow = mock(Arrow.class);
        when(arrow.getShooter()).thenReturn(mock(BlockProjectileSource.class));

        fallback.onDamage(damageBy(victim, arrow, DamageCause.PROJECTILE, 2));

        FinalDamageEvent finalEvent = fired.get(0);
        assertNull(finalEvent.getAttacker());
        assertSame(arrow, finalEvent.getProjectile());
        assertEquals(DamageOrigin.ENVIRONMENT, finalEvent.getOrigin());
    }

    @Test
    void nonLivingDamager_isEnvironment() {
        fallback.onDamage(damageBy(victim, mock(TNTPrimed.class), DamageCause.ENTITY_EXPLOSION, 2));

        FinalDamageEvent finalEvent = fired.get(0);
        assertNull(finalEvent.getAttacker());
        assertNull(finalEvent.getProjectile());
        assertEquals(DamageOrigin.ENVIRONMENT, finalEvent.getOrigin());
    }

    @Test
    void plainDamageEvent_isEnvironment() {
        fallback.onDamage(damage(victim, DamageCause.DROWNING, 2));

        assertEquals(DamageOrigin.ENVIRONMENT, fired.get(0).getOrigin());
    }
}
