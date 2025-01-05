package studio.magemonkey.codex.registry.provider;

import org.bukkit.entity.LivingEntity;

public interface BuffProvider {
    double scaleValue(String name, LivingEntity player, double value);
}
