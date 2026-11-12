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
        assertTrue(request.hasKnockback());
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
        assertFalse(request.hasKnockback());
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

    @Test
    void weaponDamage_isLeftAloneByDefault() {
        DamageRequest request = DamageRequest.builder().build();

        assertFalse(request.adjustsWeaponDamage());
        assertEquals(1D, request.getWeaponScale());
        assertEquals(0D, request.getWeaponBonus());
        assertEquals(20D, request.adjustWeaponDamage("physical", 20));
    }

    @Test
    void weaponScale_multipliesEveryType() {
        DamageRequest request = DamageRequest.builder().weaponScale(0.5).build();

        assertTrue(request.adjustsWeaponDamage());
        assertEquals(10D, request.adjustWeaponDamage("physical", 20));
        assertEquals(5D, request.adjustWeaponDamage("fire", 10));
    }

    @Test
    void weaponBonus_isAddedToEveryTypeTheWeaponDeals_andMayBeNegative() {
        DamageRequest plus  = DamageRequest.builder().weaponBonus(5).build();
        DamageRequest minus = DamageRequest.builder().weaponBonus(-5).build();

        assertTrue(plus.adjustsWeaponDamage());
        assertEquals(25D, plus.adjustWeaponDamage("physical", 20));
        assertEquals(15D, minus.adjustWeaponDamage("physical", 20));
        assertEquals(-2D, minus.adjustWeaponDamage("fire", 3), "Clamping is left to the caller");
    }

    @Test
    void weaponBonus_doesNotGiveAWeaponADamageTypeItDoesNotDeal() {
        DamageRequest request = DamageRequest.builder().weaponBonus(5).weaponScale(2).build();

        assertEquals(0D, request.adjustWeaponDamage("fire", 0));
    }

    @Test
    void weaponScaleAndBonus_scaleFirstThenAdd() {
        DamageRequest request = DamageRequest.builder().weaponScale(0.5).weaponBonus(3).build();

        // 20 * 0.5 + 3
        assertEquals(13D, request.adjustWeaponDamage("physical", 20));
    }

    @Test
    void weaponScaleAndBonus_forOneType_replaceTheOnesForEveryType() {
        DamageRequest request = DamageRequest.builder()
                .weaponScale(0.5)
                .weaponBonus(1)
                .weaponScale("fire", 2)
                .weaponBonus("ice", 10)
                .build();

        assertTrue(request.adjustsWeaponDamage());
        // physical: both general values. fire: its own scale, the general bonus. ice: the general scale,
        // its own bonus.
        assertEquals(11D, request.adjustWeaponDamage("physical", 20));
        assertEquals(41D, request.adjustWeaponDamage("fire", 20));
        assertEquals(20D, request.adjustWeaponDamage("ice", 20));
        assertEquals(2D, request.getWeaponScale("fire"));
        assertEquals(0.5D, request.getWeaponScale("ice"));
        assertEquals(10D, request.getWeaponBonus("ice"));
        assertEquals(1D, request.getWeaponBonus("fire"));
    }

    @Test
    void weaponScaleForOneTypeOnly_countsAsAnAdjustment() {
        assertTrue(DamageRequest.builder().weaponScale("fire", 1).build().adjustsWeaponDamage());
        assertTrue(DamageRequest.builder().weaponBonus("fire", 0).build().adjustsWeaponDamage());
    }

    @Test
    void weaponScale_rejectsNegativeAndNotANumber_weaponBonusRejectsNotANumber() {
        DamageRequest.Builder builder = DamageRequest.builder();

        assertThrows(IllegalArgumentException.class, () -> builder.weaponScale(-1));
        assertThrows(IllegalArgumentException.class, () -> builder.weaponScale(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> builder.weaponScale("fire", -1));
        assertThrows(IllegalArgumentException.class, () -> builder.weaponBonus(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> builder.weaponBonus("fire", Double.NaN));
    }
}
