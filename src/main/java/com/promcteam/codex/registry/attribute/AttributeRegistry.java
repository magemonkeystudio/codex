package com.promcteam.codex.registry.attribute;

import com.promcteam.codex.CodexEngine;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AttributeRegistry {
    public static final String PHYSICAL_DAMAGE    = "physical-damage";
    public static final String PHYSICAL_DEFENSE   = "physical-defense";
    public static final String PROJECTILE_DAMAGE  = "projectile-damage";
    public static final String PROJECTILE_DEFENSE = "projectile-defense";
    public static final String MELEE_DAMAGE       = "melee-damage";
    public static final String MELEE_DEFENSE      = "melee-defense";

    private static List<AttributeProvider> providers = new ArrayList<>();

    public static void registerProvider(AttributeProvider provider) {
        providers.add(provider);
    }

    public static void unregisterProvider(AttributeProvider provider) {
        providers.remove(provider);
    }

    public static double scaleAttribute(String name, LivingEntity entity, double value) {
        Objects.requireNonNull(name, "Attribute name cannot be null");
        Objects.requireNonNull(entity, "Entity cannot be null");

        double scaled = value;
        for (AttributeProvider provider : providers) {
            try {
                scaled = provider.scaleAttribute(name, entity, scaled);
            } catch (Exception e) {
                CodexEngine.get()
                        .getLogger()
                        .warning("Unable to scale stat with provider: " + provider.getClass().getSimpleName());
                e.printStackTrace();
            }
        }

        return scaled;
    }
}
