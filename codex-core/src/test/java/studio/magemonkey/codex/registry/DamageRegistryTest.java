package studio.magemonkey.codex.registry;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import studio.magemonkey.codex.Codex;
import studio.magemonkey.codex.api.damage.DamageContexts;
import studio.magemonkey.codex.api.damage.DamageRequest;
import studio.magemonkey.codex.registry.provider.DamageTypeProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

class DamageRegistryTest {
    private MockedStatic<Codex> codex;
    private LivingEntity        target;
    private LivingEntity        caster;

    @BeforeEach
    void setup() {
        // Registering a provider logs through the plugin, which does not exist in a plain unit test
        codex = mockStatic(Codex.class);
        target = entity();
        caster = entity();
    }

    @AfterEach
    void tearDown() {
        DamageRegistry.unregisterProvider(RecordingProvider.class);
        codex.close();
    }

    private static LivingEntity entity() {
        LivingEntity entity = mock(LivingEntity.class);
        when(entity.getUniqueId()).thenReturn(UUID.randomUUID());
        return entity;
    }

    /** Makes the target's damage methods behave like a server: they fire an event a listener can claim. */
    private List<DamageRequest> claimOnDamage() {
        List<DamageRequest> seen = new ArrayList<>();
        Runnable fire = () -> {
            EntityDamageEvent event = mock(EntityDamageEvent.class);
            when(event.getEntity()).thenReturn(target);
            seen.add(DamageContexts.claim(event));
        };
        doAnswer(inv -> {
            fire.run();
            return null;
        }).when(target).damage(anyDouble());
        doAnswer(inv -> {
            fire.run();
            return null;
        }).when(target).damage(anyDouble(), any(LivingEntity.class));
        return seen;
    }

    @Test
    void deal_damagesTheTargetForTheTotal_attributedToTheSource() {
        DamageRequest request = DamageRequest.builder().source(caster).amount("fire", 4).amount("ice", 2).build();

        DamageRegistry.deal(target, request);

        verify(target).damage(6D, caster);
        verify(target, never()).damage(anyDouble());
    }

    @Test
    void deal_withoutASource_damagesTheTargetUnattributed() {
        DamageRegistry.deal(target, DamageRequest.builder().amount("fire", 4).build());

        verify(target).damage(4D);
    }

    @Test
    void deal_withACause_onAServerWithoutDamageSources_damagesTheTargetTheOrdinaryWay() {
        // The Bukkit version these tests run on has no damage sources
        DamageRequest request = DamageRequest.builder()
                .source(caster)
                .cause(EntityDamageEvent.DamageCause.MAGIC)
                .amount("fire", 4)
                .build();

        DamageRegistry.deal(target, request);

        verify(target).damage(4D, caster);
    }

    @Test
    void deal_announcesTheRequestForTheDurationOfTheHitOnly() {
        List<DamageRequest> seen    = claimOnDamage();
        DamageRequest       request = DamageRequest.builder().source(caster).amount("fire", 4).build();

        assertTrue(DamageRegistry.deal(target, request));

        assertEquals(1, seen.size());
        assertSame(request, seen.get(0));
        assertFalse(DamageContexts.isAnnounced(target));
    }

    @Test
    void deal_whenNothingClaimsTheRequest_returnsFalseAndLeavesNothingBehind() {
        assertFalse(DamageRegistry.deal(target, DamageRequest.builder().amount("fire", 4).build()));

        assertFalse(DamageContexts.isAnnounced(target));
    }

    @Test
    void deal_whenTheDamageCallThrows_stillWithdrawsTheRequest() {
        doThrow(new IllegalStateException("a listener blew up")).when(target).damage(anyDouble());

        assertThrows(IllegalStateException.class,
                () -> DamageRegistry.deal(target, DamageRequest.builder().amount("fire", 4).build()));

        assertFalse(DamageContexts.isAnnounced(target));
    }

    @Test
    void deal_doesNotGoThroughProviders() {
        RecordingProvider provider = new RecordingProvider();
        DamageRegistry.registerProvider(provider);

        DamageRegistry.deal(target, DamageRequest.builder().amount("TEST_fire", 4).build());

        assertEquals(0, provider.calls);
    }

    @Test
    void dealDamage_stillGoesToTheProviderOfTheNamespace_andAnnouncesNothing() {
        RecordingProvider provider = new RecordingProvider();
        DamageRegistry.registerProvider(provider);

        assertTrue(DamageRegistry.dealDamage(target, 4, "TEST_fire", caster));
        assertFalse(DamageRegistry.dealDamage(target, 4, "OTHER_fire", caster));
        assertFalse(DamageRegistry.dealDamage(target, 4, "fire", caster));

        assertEquals(1, provider.calls);
        assertFalse(provider.announcedDuringCall);
        verify(target, never()).damage(anyDouble());
        verify(target, never()).damage(anyDouble(), any(LivingEntity.class));
    }

    static class RecordingProvider implements DamageTypeProvider {
        int     calls;
        boolean announcedDuringCall;

        @Override
        public String pluginName() {
            return "Test";
        }

        @Override
        public String getNamespace() {
            return "test";
        }

        @Override
        public boolean dealDamage(@NotNull LivingEntity entity,
                                  double amount,
                                  String damageType,
                                  @Nullable LivingEntity damager) {
            calls++;
            announcedDuringCall = DamageContexts.isAnnounced(entity);
            return true;
        }
    }
}
