package studio.magemonkey.codex.registry;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.Codex;
import studio.magemonkey.codex.api.damage.DamageContexts;
import studio.magemonkey.codex.api.damage.DamageRequest;
import studio.magemonkey.codex.api.damage.DamageScope;
import studio.magemonkey.codex.api.damage.DamageSources;
import studio.magemonkey.codex.registry.provider.DamageTypeProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DamageRegistry {
    private static final Map<String, DamageTypeProvider> PROVIDERS = new HashMap<>();

    public static void registerProvider(DamageTypeProvider provider) {
        String namespace = provider.getNamespace().toUpperCase(Locale.US);
        if (PROVIDERS.get(namespace) != null) {
            throw new IllegalArgumentException("DamageProvider with namespace " + namespace + " already exists!");
        }

        PROVIDERS.put(namespace, provider);
        Codex.info("[DamageRegistry] Successfully registered provider for " + namespace + " damage");
    }

    public static void unregisterProvider(Class<? extends DamageTypeProvider> providerClass) {
        PROVIDERS.entrySet().removeIf(entry -> entry.getValue().getClass().equals(providerClass));
    }

    public static boolean dealDamage(@NotNull LivingEntity entity,
                                     double amount,
                                     String damageType,
                                     @Nullable LivingEntity damager) {
        boolean damaged = false;

        for (Map.Entry<String, DamageTypeProvider> entry : PROVIDERS.entrySet()) {
            String namespace = entry.getKey();
            if (damageType.length() <= namespace.length() + 1) continue;
            if (damageType.substring(0, namespace.length() + 1).equalsIgnoreCase(namespace + '_')) {
                damaged |= entry.getValue().dealDamage(entity, amount, damageType, damager);
            }
        }

        return damaged;
    }

    /**
     * Deals the damage a request describes, with the request announced through {@link DamageContexts} for
     * the duration of the hit.
     * <p>
     * The target takes {@link DamageRequest#getTotal()} damage through Bukkit, attributed to the request's
     * source if it has one. If the request names a cause and the server is 1.20.4 or newer, the damage is dealt
     * with a real damage source of the matching vanilla type, so the event carries that cause; otherwise the
     * cause is only carried in the request. A plugin that calculates damage can pick the request up from the damage event
     * with {@link DamageContexts#claim}. If none does, the hit is ordinary damage of that amount.
     * <p>
     * Unlike {@link #dealDamage(LivingEntity, double, String, LivingEntity)}, this does not go through the
     * registered providers, so it works whether or not any plugin provides the damage types named in the
     * request.
     *
     * @param target  the entity to damage
     * @param request the description of the hit
     * @return whether a damage event claimed the request. False does not mean the target was not damaged:
     * the damage call has been made either way, so do not deal the damage again in response
     */
    public static boolean deal(@NotNull LivingEntity target, @NotNull DamageRequest request) {
        try (DamageScope scope = DamageContexts.open(target, request)) {
            LivingEntity source = request.getSource();
            DamageCause  cause  = request.getCause();
            if (cause != null && DamageSources.get().damage(target, request.getTotal(), cause, source)) {
                return scope.isClaimed();
            }
            if (source != null) {
                target.damage(request.getTotal(), source);
            } else {
                target.damage(request.getTotal());
            }
            return scope.isClaimed();
        }
    }
}
