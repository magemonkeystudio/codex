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
}
