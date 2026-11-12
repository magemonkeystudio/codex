package studio.magemonkey.codex.api.damage;

import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

/**
 * The stand-in classes in {@code org.bukkit.damage} of the test sources make the test classpath look like a
 * 1.20.4+ server. A class loader that cannot see them makes it look like an older one.
 */
class DamageSourceAdapterTest {
    private DamageSourceAdapter modern;
    private DamageSourceAdapter legacy;

    /** An entity as it is on 1.20.4 and newer: it can be damaged with a damage source. */
    abstract static class ModernEntity implements LivingEntity {
        public abstract void damage(double amount, DamageSource source);
    }

    /** A damage event as it is on 1.20.4 and newer: it has a damage source. */
    abstract static class ModernEvent extends EntityDamageByEntityEvent {
        ModernEvent() {
            super(null, null, null, 0);
        }

        public abstract DamageSource getDamageSource();
    }

    /** Hides the stand-in classes, like a server that does not have them. */
    private static final ClassLoader OLD_SERVER = new ClassLoader(DamageSourceAdapterTest.class.getClassLoader()) {
        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("org.bukkit.damage.")) throw new ClassNotFoundException(name);
            return super.loadClass(name, resolve);
        }
    };

    @BeforeEach
    void setup() {
        modern = DamageSources.create(getClass().getClassLoader());
        legacy = DamageSources.create(OLD_SERVER);
    }

    private static DamageSource dealt(ModernEntity target) {
        ArgumentCaptor<DamageSource> source = ArgumentCaptor.forClass(DamageSource.class);
        verify(target).damage(anyDouble(), source.capture());
        return source.getValue();
    }

    private static ModernEvent eventWith(DamageSource source) {
        ModernEvent event = mock(ModernEvent.class);
        when(event.getDamageSource()).thenReturn(source);
        return event;
    }

    // ---- Choosing the adapter

    @Test
    void create_picksTheModernAdapterWhereDamageSourcesExist() {
        assertTrue(modern.isSupported());
        assertFalse(legacy.isSupported());
    }

    // ---- Dealing damage, modern

    @Test
    void modern_damage_usesADamageSourceOfTheMatchingType_attributedToTheSource() {
        ModernEntity target = mock(ModernEntity.class);
        Zombie       caster = mock(Zombie.class);

        assertTrue(modern.damage(target, 6, DamageCause.THORNS, caster));

        verify(target).damage(eq(6D), any(DamageSource.class));
        DamageSource source = dealt(target);
        assertSame(DamageType.THORNS, source.getDamageType());
        assertSame(caster, source.getCausingEntity());
        assertSame(caster, source.getDirectEntity());
    }

    @Test
    void modern_damage_withoutASource_leavesTheEntitiesOut() {
        ModernEntity target = mock(ModernEntity.class);

        assertTrue(modern.damage(target, 6, DamageCause.FIRE, null));

        DamageSource source = dealt(target);
        assertSame(DamageType.IN_FIRE, source.getDamageType());
        assertNull(source.getCausingEntity());
        assertNull(source.getDirectEntity());
    }

    @Test
    void modern_damage_magic_isIndirectOnlyWhenSomeoneCausedIt() {
        ModernEntity byNobody = mock(ModernEntity.class);
        ModernEntity byCaster = mock(ModernEntity.class);

        modern.damage(byNobody, 1, DamageCause.MAGIC, null);
        modern.damage(byCaster, 1, DamageCause.MAGIC, mock(Player.class));

        assertSame(DamageType.MAGIC, dealt(byNobody).getDamageType());
        assertSame(DamageType.INDIRECT_MAGIC, dealt(byCaster).getDamageType());
    }

    @Test
    void modern_damage_entityAttack_dependsOnWhoAttacks() {
        ModernEntity byPlayer = mock(ModernEntity.class);
        ModernEntity byMob    = mock(ModernEntity.class);
        ModernEntity byNobody = mock(ModernEntity.class);

        assertTrue(modern.damage(byPlayer, 1, DamageCause.ENTITY_ATTACK, mock(Player.class)));
        assertTrue(modern.damage(byMob, 1, DamageCause.ENTITY_ATTACK, mock(Zombie.class)));
        assertFalse(modern.damage(byNobody, 1, DamageCause.ENTITY_ATTACK, null));

        assertSame(DamageType.PLAYER_ATTACK, dealt(byPlayer).getDamageType());
        assertSame(DamageType.MOB_ATTACK, dealt(byMob).getDamageType());
        verify(byNobody, never()).damage(anyDouble(), any(DamageSource.class));
    }

    @Test
    void modern_damage_fallsBackToAnOlderTypeWhereTheServerLacksTheNewerOne() {
        // The stand-in has EXPLOSION but no PLAYER_EXPLOSION, like a server from before a type existed
        ModernEntity target = mock(ModernEntity.class);

        assertTrue(modern.damage(target, 1, DamageCause.ENTITY_EXPLOSION, mock(Player.class)));

        assertSame(DamageType.EXPLOSION, dealt(target).getDamageType());
    }

    @Test
    void modern_damage_doesNothingForACauseWithNoMatchingType() {
        ModernEntity target = mock(ModernEntity.class);

        // No fitting type at all, and one whose type this server does not have
        assertFalse(modern.damage(target, 1, DamageCause.PROJECTILE, mock(Player.class)));
        assertFalse(modern.damage(target, 1, DamageCause.LAVA, null));

        verify(target, never()).damage(anyDouble(), any(DamageSource.class));
        verify(target, never()).damage(anyDouble());
    }

    @Test
    void modern_damage_doesNothingForATargetThatCannotTakeADamageSource() {
        LivingEntity target = mock(LivingEntity.class);

        assertFalse(modern.damage(target, 1, DamageCause.MAGIC, null));

        verifyNoInteractions(target);
    }

    @Test
    void modern_damage_letsAnExceptionFromTheDamageCallThrough() {
        ModernEntity target = mock(ModernEntity.class);
        doThrow(new IllegalStateException("a listener blew up")).when(target).damage(anyDouble(), any(DamageSource.class));

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> modern.damage(target, 1, DamageCause.MAGIC, null));
        assertEquals("a listener blew up", thrown.getMessage());
    }

    // ---- Reading an event, modern

    @Test
    void modern_readsTheDamageTypeAndEntitiesFromTheEventsSource() {
        Arrow  arrow   = mock(Arrow.class);
        Player shooter = mock(Player.class);
        ModernEvent event = eventWith(new DamageSource(DamageType.ARROW, shooter, arrow));

        assertEquals("minecraft:arrow", modern.getDamageType(event));
        assertSame(arrow, modern.getDirectEntity(event));
        assertSame(shooter, modern.getCausingEntity(event));
    }

    @Test
    void modern_reportsNoEntitiesForASourceWithoutAny() {
        ModernEvent event = eventWith(new DamageSource(DamageType.IN_FIRE, null, null));
        // The legacy reading of this event would find an entity; the source is what counts
        when(event.getDamager()).thenReturn(mock(Zombie.class));

        assertEquals("minecraft:in_fire", modern.getDamageType(event));
        assertNull(modern.getDirectEntity(event));
        assertNull(modern.getCausingEntity(event));
    }

    @Test
    void modern_withAnEventThatHasNoSource_readsItTheLegacyWay() {
        Zombie attacker = mock(Zombie.class);
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);

        assertNull(modern.getDamageType(event));
        assertSame(attacker, modern.getDirectEntity(event));
        assertSame(attacker, modern.getCausingEntity(event));
    }

    // ---- Legacy

    @Test
    void legacy_damage_doesNothing() {
        ModernEntity target = mock(ModernEntity.class);

        for (DamageCause cause : DamageCause.values()) {
            assertFalse(legacy.damage(target, 6, cause, mock(Player.class)), cause.name());
        }

        verifyNoInteractions(target);
    }

    @Test
    void legacy_knowsNoDamageType_evenIfTheEventHasOne() {
        assertNull(legacy.getDamageType(eventWith(new DamageSource(DamageType.ARROW, null, null))));
    }

    @Test
    void legacy_melee_theAttackerIsBothDirectAndCausing() {
        Zombie attacker = mock(Zombie.class);
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);

        assertSame(attacker, legacy.getDirectEntity(event));
        assertSame(attacker, legacy.getCausingEntity(event));
    }

    @Test
    void legacy_projectile_theShooterIsTheCausingEntity() {
        Arrow  arrow   = mock(Arrow.class);
        Player shooter = mock(Player.class);
        when(arrow.getShooter()).thenReturn(shooter);
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(arrow);

        assertSame(arrow, legacy.getDirectEntity(event));
        assertSame(shooter, legacy.getCausingEntity(event));
    }

    @Test
    void legacy_projectileWithoutAnEntityAsShooter_hasNoCausingEntity() {
        // Shot by a dispenser, or the shooter is gone
        Arrow arrow = mock(Arrow.class);
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(arrow);

        assertSame(arrow, legacy.getDirectEntity(event));
        assertNull(legacy.getCausingEntity(event));
    }

    @Test
    void legacy_damageWithoutAnAttacker_hasNoEntities() {
        EntityDamageEvent event = mock(EntityDamageEvent.class);

        assertNull(legacy.getDirectEntity(event));
        assertNull(legacy.getCausingEntity(event));
    }
}
