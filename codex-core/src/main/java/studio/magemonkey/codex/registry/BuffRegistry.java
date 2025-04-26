package studio.magemonkey.codex.registry;

import org.bukkit.entity.LivingEntity;
import studio.magemonkey.codex.Codex;
import studio.magemonkey.codex.registry.provider.BuffProvider;

import java.util.ArrayList;
import java.util.List;

public class BuffRegistry {
    private static final List<BuffProvider> PROVIDERS = new ArrayList<>();

    public static void registerProvider(BuffProvider provider) {
        PROVIDERS.add(provider);
        Codex.info(
                "[BuffRegistry] Registered BuffProvider: " + provider.getClass().getSimpleName());
    }

    public static void unregisterProvider(BuffProvider provider) {
        PROVIDERS.remove(provider);
    }

    public static void unregisterProvider(Class<? extends BuffProvider> provider) {
        PROVIDERS.removeIf(p -> p.getClass().equals(provider));
    }

    public static double scaleValue(String name, LivingEntity entity, double value) {
        double scaled = value;

        for (BuffProvider provider : PROVIDERS) {
            scaled = provider.scaleValue(name, entity, scaled);
        }

        return scaled;
    }

    /**
     * Scales the damage dealt directly for the target entity. This method should be used when
     * modifications to the damage need to be made outside the vanilla "defense" calculations.
     * The primary example of this is Fabled's shield system. Damage is reduced directly instead
     * of being defended. This is essentially used for absorption effects.
     * <p>
     * It is important to note that we are _specifically_ not using the {@link #scaleValue} method
     * here, as this method will only scale the value if the {@link BuffProvider} overrides the implementation.
     *
     * @param name    name of the buff
     * @param entity  entity to scale the damage for
     * @param damage  damage being dealt
     * @return the modified damage
     */
    public static double scaleDamageForDefense(String name, LivingEntity entity, double damage) {
        double scaled = damage;

        for (BuffProvider provider : PROVIDERS) {
            scaled = provider.scaleDamageForDefense(name, entity, damage);
        }

        return scaled;
    }
}
