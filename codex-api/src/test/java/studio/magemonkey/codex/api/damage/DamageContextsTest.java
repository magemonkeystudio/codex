package studio.magemonkey.codex.api.damage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DamageContextsTest {
    private final LivingEntity  target  = entity();
    private final LivingEntity  other   = entity();
    private final DamageRequest request = DamageRequest.builder().amount("fire", 4).build();

    private static LivingEntity entity() {
        LivingEntity entity = mock(LivingEntity.class);
        when(entity.getUniqueId()).thenReturn(UUID.randomUUID());
        return entity;
    }

    /** A damage event for the entity. Each call is a different event. */
    private static EntityDamageEvent hit(LivingEntity entity) {
        EntityDamageEvent event = mock(EntityDamageEvent.class);
        when(event.getEntity()).thenReturn(entity);
        return event;
    }

    @Test
    void claim_withNothingOpen_returnsNull() {
        assertNull(DamageContexts.claim(hit(target)));
        assertFalse(DamageContexts.isAnnounced(target));
    }

    @Test
    void claim_returnsTheRequestToTheFirstEventOfTheTarget() {
        try (DamageScope scope = DamageContexts.open(target, request)) {
            assertTrue(DamageContexts.isAnnounced(target));
            assertFalse(scope.isClaimed());

            assertSame(request, DamageContexts.claim(hit(target)));

            assertTrue(scope.isClaimed());
            assertFalse(DamageContexts.isAnnounced(target));
        }
    }

    @Test
    void claim_bySeveralListenersOfTheSameEvent_returnsTheRequestEachTime() {
        try (DamageScope ignored = DamageContexts.open(target, request)) {
            EntityDamageEvent event = hit(target);

            assertSame(request, DamageContexts.claim(event));
            assertSame(request, DamageContexts.claim(event));
        }
    }

    @Test
    void claim_byASecondEventOfTheSameTarget_returnsNull() {
        // A hit action that damages the same target again from inside the first hit
        try (DamageScope ignored = DamageContexts.open(target, request)) {
            assertSame(request, DamageContexts.claim(hit(target)));

            assertNull(DamageContexts.claim(hit(target)));
        }
    }

    @Test
    void claim_byAnEventOfAnotherEntity_returnsNullAndLeavesTheRequest() {
        // Thorns: the nested hit lands on the attacker, before or after the real event is handled
        try (DamageScope scope = DamageContexts.open(target, request)) {
            assertNull(DamageContexts.claim(hit(other)));
            assertFalse(scope.isClaimed());

            assertSame(request, DamageContexts.claim(hit(target)));
        }
    }

    @Test
    void close_withdrawsARequestNobodyClaimed() {
        // The damage call fired no event, or every listener ignored the request
        DamageScope scope = DamageContexts.open(target, request);
        scope.close();

        assertTrue(scope.isClosed());
        assertFalse(scope.isClaimed());
        assertFalse(DamageContexts.isAnnounced(target));
        assertNull(DamageContexts.claim(hit(target)));
    }

    @Test
    void close_afterACancelledEvent_leavesNothingBehind() {
        EntityDamageEvent cancelled = hit(target);
        when(cancelled.isCancelled()).thenReturn(true);

        try (DamageScope ignored = DamageContexts.open(target, request)) {
            DamageContexts.claim(cancelled);
        }

        // Neither the cancelled event nor the next hit sees the old request
        assertNull(DamageContexts.claim(cancelled));
        assertNull(DamageContexts.claim(hit(target)));
    }

    @Test
    void close_happensWhenTheDamageCallThrows() {
        assertThrows(IllegalStateException.class, () -> {
            try (DamageScope ignored = DamageContexts.open(target, request)) {
                throw new IllegalStateException("a listener blew up");
            }
        });

        assertFalse(DamageContexts.isAnnounced(target));
        assertNull(DamageContexts.claim(hit(target)));
    }

    @Test
    void close_twice_doesNotRemoveANewerScope() {
        DamageScope first = DamageContexts.open(target, request);
        first.close();

        try (DamageScope ignored = DamageContexts.open(target, request)) {
            first.close();

            assertTrue(DamageContexts.isAnnounced(target));
        }
    }

    @Test
    void close_outOfOrder_removesOnlyThatScope() {
        DamageRequest second = DamageRequest.builder().amount("ice", 2).build();
        DamageScope   older  = DamageContexts.open(target, request);

        try (DamageScope ignored = DamageContexts.open(target, second)) {
            older.close();

            assertSame(second, DamageContexts.claim(hit(target)));
            assertNull(DamageContexts.claim(hit(target)));
        }
    }

    @Test
    void nestedScopeOnTheSameTarget_goesToTheNestedEvent() {
        // A bleed that announces its own hit on the target while the first hit is still being handled
        DamageRequest bleed = DamageRequest.builder().origin(DamageOrigin.EFFECT).amount("physical", 1).build();

        try (DamageScope outer = DamageContexts.open(target, request)) {
            EntityDamageEvent firstHit = hit(target);
            assertSame(request, DamageContexts.claim(firstHit));

            try (DamageScope inner = DamageContexts.open(target, bleed)) {
                assertSame(bleed, DamageContexts.claim(hit(target)));
                // A listener of the first hit that runs later still gets the first request
                assertSame(request, DamageContexts.claim(firstHit));
                assertTrue(inner.isClaimed());
            }

            assertSame(request, DamageContexts.claim(firstHit));
            assertTrue(outer.isClaimed());
        }
        assertNull(DamageContexts.claim(hit(target)));
    }

    @Test
    void twoUnclaimedScopesOnTheSameTarget_theNewestIsClaimedFirst() {
        DamageRequest second = DamageRequest.builder().amount("ice", 2).build();

        try (DamageScope ignoredOuter = DamageContexts.open(target, request);
             DamageScope ignoredInner = DamageContexts.open(target, second)) {
            assertSame(second, DamageContexts.claim(hit(target)));
            assertSame(request, DamageContexts.claim(hit(target)));
        }
    }

    @Test
    void scopesOnDifferentTargets_doNotMix() {
        DamageRequest forOther = DamageRequest.builder().amount("ice", 2).build();

        try (DamageScope ignoredA = DamageContexts.open(target, request);
             DamageScope ignoredB = DamageContexts.open(other, forOther)) {
            assertSame(forOther, DamageContexts.claim(hit(other)));
            assertSame(request, DamageContexts.claim(hit(target)));
        }
    }
}
