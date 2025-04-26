package studio.magemonkey.codex.registry.provider;

import org.bukkit.entity.LivingEntity;

public interface AttributeProvider {
    double scaleAttribute(String name, LivingEntity player, double value);
}
