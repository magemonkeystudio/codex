package studio.magemonkey.codex.api.damage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DamageRequestTest {

    @Test
    void defaults() {
        DamageRequest request = DamageRequest.builder().build();

        assertEquals(DamageOrigin.EXTERNAL, request.getOrigin());
        assertEquals(WeaponStatsMode.ADD, request.getWeaponStats());
        assertTrue(request.isKnockback());
        assertNull(request.getSource());
        assertNull(request.getCause());
        assertTrue(request.getAmounts().isEmpty());
        assertEquals(0D, request.getTotal());
    }

    @Test
    void carriesWhatItWasGiven() {
        LivingEntity caster = mock(LivingEntity.class);

        DamageRequest request = DamageRequest.builder()
                .origin(DamageOrigin.SKILL)
                .source(caster)
                .cause(DamageCause.MAGIC)
                .amount("DIVINITY_fire", 4)
                .weaponStats(WeaponStatsMode.REPLACE)
                .knockback(false)
                .build();

        assertEquals(DamageOrigin.SKILL, request.getOrigin());
        assertSame(caster, request.getSource());
        assertEquals(DamageCause.MAGIC, request.getCause());
        assertEquals(4D, request.getAmounts().get("DIVINITY_fire"));
        assertEquals(WeaponStatsMode.REPLACE, request.getWeaponStats());
        assertFalse(request.isKnockback());
    }

    @Test
    void amounts_keepTheirOrderAndTheirIdsAsGiven() {
        DamageRequest request = DamageRequest.builder()
                .amount("physical", 5)
                .amount("DIVINITY_fire", 3)
                .amount("default", 1.5)
                .build();

        List<String> types = new ArrayList<>(request.getAmounts().keySet());
        assertEquals(List.of("physical", "DIVINITY_fire", "default"), types);
        assertEquals(9.5D, request.getTotal());
    }

    @Test
    void amounts_ofTheSameTypeAreAdded() {
        DamageRequest request = DamageRequest.builder().amount("fire", 2).amount("fire", 3).build();

        assertEquals(1, request.getAmounts().size());
        assertEquals(5D, request.getAmounts().get("fire"));
    }

    @Test
    void amounts_rejectNegativeAndNotANumber() {
        DamageRequest.Builder builder = DamageRequest.builder();

        assertThrows(IllegalArgumentException.class, () -> builder.amount("fire", -1));
        assertThrows(IllegalArgumentException.class, () -> builder.amount("fire", Double.NaN));
    }

    @Test
    void request_isNotChangedByLaterUseOfItsBuilder() {
        DamageRequest.Builder builder = DamageRequest.builder().amount("fire", 2);
        DamageRequest         request = builder.build();

        builder.amount("ice", 7);

        assertEquals(1, request.getAmounts().size());
        assertThrows(UnsupportedOperationException.class, () -> request.getAmounts().put("ice", 1D));
    }
}
