package studio.magemonkey.codex.registry;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import studio.magemonkey.codex.Codex;
import studio.magemonkey.codex.registry.provider.BuffProvider;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class BuffRegistryTest {
    private final List<BuffProvider> registered = new ArrayList<>();
    private       MockedStatic<Codex> codex;
    private       LivingEntity        entity;

    @BeforeEach
    void setup() {
        // Registering a provider logs through the plugin, which does not exist in a plain unit test
        codex = mockStatic(Codex.class);
        entity = mock(LivingEntity.class);
    }

    @AfterEach
    void tearDown() {
        registered.forEach(BuffRegistry::unregisterProvider);
        codex.close();
    }

    @Test
    void scaleDamageForDefense_withNoProviders_returnsTheDamageUnchanged() {
        assertEquals(10D, BuffRegistry.scaleDamageForDefense("physical-damage", entity, 10D));
    }

    @Test
    void scaleDamageForDefense_withOneProvider_appliesIt() {
        register(absorbing(4));

        assertEquals(6D, BuffRegistry.scaleDamageForDefense("physical-damage", entity, 10D));
    }

    @Test
    void scaleDamageForDefense_chainsEveryProvider() {
        // Two shields: the first absorbs 4 of the 10, the second absorbs 3 of what is left
        register(absorbing(4));
        register(absorbing(3));

        assertEquals(3D, BuffRegistry.scaleDamageForDefense("physical-damage", entity, 10D));
    }

    @Test
    void scaleDamageForDefense_providerThatDoesNotOverrideIt_doesNotUndoEarlierProviders() {
        register(absorbing(4));
        // A provider that only scales values and leaves the default damage-for-defense behaviour
        register((name, entity, value) -> value);

        assertEquals(6D, BuffRegistry.scaleDamageForDefense("physical-damage", entity, 10D));
    }

    @Test
    void scaleValue_chainsEveryProvider() {
        register((name, entity, value) -> value * 2);
        register((name, entity, value) -> value + 1);

        assertEquals(21D, BuffRegistry.scaleValue("damage", entity, 10D));
    }

    private void register(BuffProvider provider) {
        BuffRegistry.registerProvider(provider);
        registered.add(provider);
    }

    private static BuffProvider absorbing(double amount) {
        return new BuffProvider() {
            @Override
            public double scaleValue(String name, @NotNull LivingEntity entity, double value) {
                return value;
            }

            @Override
            public double scaleDamageForDefense(String name, @NotNull LivingEntity entity, double damage) {
                return Math.max(0, damage - amount);
            }
        };
    }
}
