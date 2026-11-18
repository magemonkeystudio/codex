package studio.magemonkey.codex.api.damage;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FinalDamageEventTest {
    private static final double EPSILON = 1e-9;

    private final LivingEntity      victim   = mock(LivingEntity.class);
    private final LivingEntity      attacker = mock(LivingEntity.class);
    private final EntityDamageEvent bukkit   = mock(EntityDamageEvent.class);
    private final List<Plugin>      registered = new ArrayList<>();

    @AfterEach
    void unregisterProviders() {
        registered.forEach(FinalDamageEvent::unregisterProvider);
    }

    private static Map<String, Double> damages(Object... typeAndAmount) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (int i = 0; i < typeAndAmount.length; i += 2) {
            map.put((String) typeAndAmount[i], ((Number) typeAndAmount[i + 1]).doubleValue());
        }
        return map;
    }

    private FinalDamageEvent event(Map<String, Double> damages) {
        return new FinalDamageEvent(victim, attacker, null, DamageCause.ENTITY_ATTACK, bukkit, null,
                DamageOrigin.WEAPON_MELEE, WeaponStatsMode.ADD, damages);
    }

    private Plugin plugin(boolean enabled) {
        Plugin plugin = mock(Plugin.class);
        when(plugin.isEnabled()).thenReturn(enabled);
        registered.add(plugin);
        return plugin;
    }

    // --- Values ---------------------------------------------------------------------------------------------

    @Test
    void getters_returnWhatTheProviderGave() {
        DamageRequest request = DamageRequest.builder()
                .origin(DamageOrigin.SKILL)
                .weaponStats(WeaponStatsMode.REPLACE)
                .amount("fire", 3)
                .build();
        FinalDamageEvent event = new FinalDamageEvent(victim, attacker, null, DamageCause.MAGIC, bukkit,
                request, DamageOrigin.SKILL, WeaponStatsMode.REPLACE, damages("fire", 2.5));

        assertSame(victim, event.getVictim());
        assertSame(attacker, event.getAttacker());
        assertNull(event.getProjectile());
        assertEquals(DamageCause.MAGIC, event.getCause());
        assertSame(bukkit, event.getDamageEvent());
        assertSame(request, event.getRequest());
        assertEquals(DamageOrigin.SKILL, event.getOrigin());
        assertEquals(WeaponStatsMode.REPLACE, event.getWeaponStatsMode());
        assertEquals(Collections.singletonMap("fire", 3.0), event.getRequestedAmounts());
        assertEquals(2.5, event.getTotalDamage(), EPSILON);
        assertFalse(event.isAsynchronous());
    }

    @Test
    void getRequestedAmounts_withoutRequest_isEmpty() {
        assertTrue(event(damages("physical", 1)).getRequestedAmounts().isEmpty());
    }

    @Test
    void constructor_copiesTheMap() {
        Map<String, Double> map = damages("physical", 4);
        FinalDamageEvent event = event(map);
        map.put("fire", 10.0);

        assertEquals(Collections.singletonMap("physical", 4.0), event.getDamages());
    }

    @Test
    void constructor_rejectsBadAmounts() {
        assertThrows(IllegalArgumentException.class, () -> event(damages("physical", -1)));
        assertThrows(IllegalArgumentException.class, () -> event(damages("physical", Double.NaN)));
        Map<String, Double> withNull = new HashMap<>();
        withNull.put("physical", null);
        assertThrows(IllegalArgumentException.class, () -> event(withNull));
    }

    @Test
    void getDamages_cannotBeChanged() {
        FinalDamageEvent event = event(damages("physical", 4));
        assertThrows(UnsupportedOperationException.class, () -> event.getDamages().put("fire", 1.0));
    }

    // --- Per-type and total -------------------------------------------------------------------------------

    @Test
    void getDamage_ofAMissingType_isZero() {
        assertEquals(0, event(damages("physical", 4)).getDamage("fire"), EPSILON);
    }

    @Test
    void setDamage_changesTheTypeAndTheTotalByTheDifference() {
        FinalDamageEvent event = event(damages("physical", 6, "fire", 2));

        event.setDamage("fire", 5);

        assertEquals(6, event.getDamage("physical"), EPSILON);
        assertEquals(5, event.getDamage("fire"), EPSILON);
        assertEquals(11, event.getTotalDamage(), EPSILON);
    }

    @Test
    void setDamage_ofANewType_addsItAtTheEnd() {
        FinalDamageEvent event = event(damages("physical", 6));

        event.setDamage("ice", 1);

        assertEquals(List.of("physical", "ice"), new ArrayList<>(event.getDamages().keySet()));
        assertEquals(7, event.getTotalDamage(), EPSILON);
    }

    @Test
    void setDamage_rejectsBadAmounts() {
        FinalDamageEvent event = event(damages("physical", 6));
        assertThrows(IllegalArgumentException.class, () -> event.setDamage("physical", -0.5));
        assertThrows(IllegalArgumentException.class, () -> event.setDamage("physical", Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> event.setDamage("physical", Double.POSITIVE_INFINITY));
        assertEquals(6, event.getTotalDamage(), EPSILON);
    }

    @Test
    void setTotalDamage_scalesEveryTypeProportionally() {
        FinalDamageEvent event = event(damages("physical", 6, "fire", 2));

        event.setTotalDamage(4);

        assertEquals(3, event.getDamage("physical"), EPSILON);
        assertEquals(1, event.getDamage("fire"), EPSILON);
        assertEquals(4, event.getTotalDamage(), EPSILON);
    }

    @Test
    void setTotalDamage_toZero_zeroesEveryType() {
        FinalDamageEvent event = event(damages("physical", 6, "fire", 2));

        event.setTotalDamage(0);

        assertEquals(0, event.getDamage("physical"), EPSILON);
        assertEquals(0, event.getDamage("fire"), EPSILON);
        assertEquals(0, event.getTotalDamage(), EPSILON);
    }

    @Test
    void setTotalDamage_whenEveryTypeIsZero_splitsEvenly() {
        FinalDamageEvent event = event(damages("physical", 0, "fire", 0));

        event.setTotalDamage(5);

        assertEquals(2.5, event.getDamage("physical"), EPSILON);
        assertEquals(2.5, event.getDamage("fire"), EPSILON);
    }

    @Test
    void setTotalDamage_withNoTypes_usesTheDefaultType() {
        FinalDamageEvent event = event(Collections.emptyMap());

        event.setTotalDamage(3);

        assertEquals(Collections.singletonMap(FinalDamageEvent.DEFAULT_TYPE, 3.0), event.getDamages());

        FinalDamageEvent empty = event(Collections.emptyMap());
        empty.setTotalDamage(0);
        assertTrue(empty.getDamages().isEmpty());
    }

    @Test
    void setTotalDamage_rejectsBadAmounts() {
        FinalDamageEvent event = event(damages("physical", 6));
        assertThrows(IllegalArgumentException.class, () -> event.setTotalDamage(-1));
        assertThrows(IllegalArgumentException.class, () -> event.setTotalDamage(Double.NaN));
        assertEquals(6, event.getTotalDamage(), EPSILON);
    }

    @Test
    void setTotalDamage_goesThroughASubclassStorage() {
        StorageEvent event = new StorageEvent(victim, bukkit, 6, 2);

        event.setTotalDamage(4);

        assertEquals(3, event.physical, EPSILON);
        assertEquals(1, event.fire, EPSILON);
        assertEquals(4, event.getTotalDamage(), EPSILON);
        assertEquals(damages("physical", 3, "fire", 1), event.getDamages());

        event.setDamage("fire", 2);
        assertEquals(2, event.fire, EPSILON);
        assertEquals(5, event.getTotalDamage(), EPSILON);
    }

    // --- Cancelling ---------------------------------------------------------------------------------------

    @Test
    void cancel_isOffByDefault_andCanBeUndone() {
        FinalDamageEvent event = event(damages("physical", 6));
        assertFalse(event.isCancelled());

        event.setCancelled(true);
        assertTrue(event.isCancelled());

        event.setCancelled(false);
        assertFalse(event.isCancelled());
    }

    @Test
    void cancel_leavesTheBukkitEventToTheProvider() {
        FinalDamageEvent event = event(damages("physical", 6));

        event.setCancelled(true);

        org.mockito.Mockito.verifyNoInteractions(bukkit);
        assertEquals(6, event.getTotalDamage(), EPSILON);
    }

    // --- Handler list -------------------------------------------------------------------------------------

    @Test
    void handlerList_isSharedBySubclasses() {
        StorageEvent sub = new StorageEvent(victim, bukkit, 1, 1);
        assertSame(FinalDamageEvent.getHandlerList(), sub.getHandlers());
        assertSame(FinalDamageEvent.getHandlerList(), event(damages()).getHandlers());
    }

    /**
     * Registers listeners through a real {@link SimplePluginManager} and {@link JavaPluginLoader}, the way a
     * server does, to check Bukkit's rule on this API version: a listener of a subclass that does not declare
     * {@code getHandlerList()} is registered with the base class's list.
     */
    @Test
    void bukkit_deliversSubclassEventsToBaseListeners_andOnlySubclassEventsToSubclassListeners() throws Exception {
        Server server = mock(Server.class);
        when(server.getLogger()).thenReturn(Logger.getLogger("test"));
        when(server.isPrimaryThread()).thenReturn(true);
        SimplePluginManager manager = new SimplePluginManager(server, new SimpleCommandMap(server));
        when(server.getPluginManager()).thenReturn(manager);

        JavaPluginLoader loader = new JavaPluginLoader(server);
        Plugin plugin = mock(Plugin.class);
        when(plugin.isEnabled()).thenReturn(true);
        when(plugin.getDescription()).thenReturn(new PluginDescriptionFile("Test", "1.0", "test.Main"));
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        when(plugin.getPluginLoader()).thenReturn(loader);

        // Spigot's timings, which the plugin loader's executor starts, look the plugin manager up through
        // the static Bukkit server. Install the mock for this test only.
        Field serverField = Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        assertNull(serverField.get(null), "another test left a Bukkit server behind");
        serverField.set(null, server);

        RecordingListener listener = new RecordingListener();
        try {
            manager.registerEvents(listener, plugin);

            FinalDamageEvent base = event(damages("physical", 6));
            StorageEvent     sub  = new StorageEvent(victim, bukkit, 4, 0);
            manager.callEvent(base);
            manager.callEvent(sub);

            assertEquals(List.of(base, sub), listener.base);
            assertEquals(List.of(sub), listener.sub);
            // The base listener ran first and halved the subclass event; the subclass listener saw that.
            assertEquals(2, sub.physical, EPSILON);
            // Both handlers, the subclass one included, sit in the base class's list.
            assertEquals(2, FinalDamageEvent.getHandlerList().getRegisteredListeners().length);
        } finally {
            HandlerList.unregisterAll(listener);
            serverField.set(null, null);
        }
        assertEquals(0, FinalDamageEvent.getHandlerList().getRegisteredListeners().length);
    }

    public static class RecordingListener implements Listener {
        final List<FinalDamageEvent> base = new ArrayList<>();
        final List<StorageEvent>     sub  = new ArrayList<>();

        @EventHandler(priority = org.bukkit.event.EventPriority.LOW)
        public void onFinal(FinalDamageEvent event) {
            base.add(event);
            if (event instanceof StorageEvent) event.setTotalDamage(event.getTotalDamage() / 2);
        }

        @EventHandler(priority = org.bukkit.event.EventPriority.HIGH)
        public void onSub(StorageEvent event) {
            sub.add(event);
        }
    }

    // --- Providers ----------------------------------------------------------------------------------------

    @Test
    void providers_noneRegistered() {
        assertFalse(FinalDamageEvent.hasProvider());
        assertFalse(FinalDamageEvent.willFire(bukkit));
    }

    @Test
    void providers_registerAndUnregister() {
        Plugin provider = plugin(true);

        FinalDamageEvent.registerProvider(provider);
        assertTrue(FinalDamageEvent.hasProvider());
        assertTrue(FinalDamageEvent.willFire(bukkit));

        FinalDamageEvent.unregisterProvider(provider);
        assertFalse(FinalDamageEvent.hasProvider());
        assertFalse(FinalDamageEvent.willFire(bukkit));

        // Unregistering twice does nothing.
        FinalDamageEvent.unregisterProvider(provider);
    }

    @Test
    void providers_predicateDecidesPerHit() {
        Plugin provider = plugin(true);
        EntityDamageEvent fall = mock(EntityDamageEvent.class);
        when(fall.getCause()).thenReturn(DamageCause.FALL);
        when(bukkit.getCause()).thenReturn(DamageCause.ENTITY_ATTACK);

        FinalDamageEvent.registerProvider(provider, e -> e.getCause() == DamageCause.ENTITY_ATTACK);

        assertTrue(FinalDamageEvent.hasProvider());
        assertTrue(FinalDamageEvent.willFire(bukkit));
        assertFalse(FinalDamageEvent.willFire(fall));
    }

    @Test
    void providers_registeringAgainReplacesThePredicate() {
        Plugin provider = plugin(true);
        FinalDamageEvent.registerProvider(provider, e -> false);
        assertFalse(FinalDamageEvent.willFire(bukkit));

        FinalDamageEvent.registerProvider(provider);
        assertTrue(FinalDamageEvent.willFire(bukkit));
    }

    @Test
    void providers_aDisabledProviderDoesNotCount() {
        Plugin provider = plugin(false);
        FinalDamageEvent.registerProvider(provider);

        assertFalse(FinalDamageEvent.hasProvider());
        assertFalse(FinalDamageEvent.willFire(bukkit));
    }

    @Test
    void providers_anyOneProviderIsEnough() {
        FinalDamageEvent.registerProvider(plugin(true), e -> false);
        FinalDamageEvent.registerProvider(plugin(true), e -> true);

        assertTrue(FinalDamageEvent.willFire(bukkit));
    }

    /**
     * A provider's own event, the way Divinity's would look: it keeps the damage in fields of its own and
     * overrides only the three storage methods, and it does not declare {@code getHandlerList()}.
     */
    static class StorageEvent extends FinalDamageEvent {
        double physical;
        double fire;

        StorageEvent(LivingEntity victim, EntityDamageEvent bukkit, double physical, double fire) {
            super(victim, null, null, DamageCause.ENTITY_ATTACK, bukkit, null, DamageOrigin.WEAPON_MELEE,
                    WeaponStatsMode.ADD, Collections.emptyMap());
            this.physical = physical;
            this.fire = fire;
        }

        @Override
        public Map<String, Double> getDamages() {
            Map<String, Double> map = new LinkedHashMap<>();
            map.put("physical", physical);
            map.put("fire", fire);
            return Collections.unmodifiableMap(map);
        }

        @Override
        public double getDamage(String type) {
            switch (type) {
                case "physical":
                    return physical;
                case "fire":
                    return fire;
                default:
                    return 0;
            }
        }

        @Override
        public void setDamage(String type, double amount) {
            if (amount < 0) throw new IllegalArgumentException();
            switch (type) {
                case "physical":
                    physical = amount;
                    break;
                case "fire":
                    fire = amount;
                    break;
                default:
                    break;
            }
        }
    }
}
