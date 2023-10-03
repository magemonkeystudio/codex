package mc.promcteam.engine.registry.attribute;

import org.bukkit.entity.LivingEntity;

public interface AttributeProvider {
    double scaleAttribute(String name, LivingEntity player, double value);
}
