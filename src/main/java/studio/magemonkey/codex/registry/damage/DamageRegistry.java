package studio.magemonkey.codex.registry.damage;

import studio.magemonkey.codex.CodexEngine;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DamageRegistry {

    private static final Map<String, DamageTypeProvider> PROVIDERS = new HashMap<>();

    public static void registerProvider(DamageTypeProvider provider) {
        String namespace = provider.getNamespace().toUpperCase(Locale.US);
        if (PROVIDERS.get(namespace) != null) {
            throw new IllegalArgumentException("Provider with namespace " + namespace + " already exists!");
        }

        PROVIDERS.put(namespace, provider);
        CodexEngine.get()
                .getLogger()
                .info("[DamageRegistry] Successfully registered provider for " + namespace + " damage");
    }

    public static void unregisterProvider(Class<? extends DamageTypeProvider> providerClass) {
        PROVIDERS.entrySet().removeIf(entry -> entry.getValue().getClass().equals(providerClass));
    }

    public static boolean dealDamage(@NotNull LivingEntity entity,
                                     double amount,
                                     String damageType,
                                     @Nullable LivingEntity damager) {
        for (Map.Entry<String, DamageTypeProvider> entry : PROVIDERS.entrySet()) {
            String namespace = entry.getKey();
            if (damageType.length() <= namespace.length() + 1) continue;
            if (damageType.substring(0, namespace.length() + 1).equalsIgnoreCase(namespace + '_')) {
                return entry.getValue().dealDamage(entity, amount, damageType, damager);
            }
        }
        return false;
    }
}
