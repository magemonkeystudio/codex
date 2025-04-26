package studio.magemonkey.codex.registry.provider;

import org.bukkit.entity.LivingEntity;

public interface BuffProvider {
    /**
     * Scales a value for an entity
     *
     * @param name  name of the buff
     * @param player entity to scale the value for
     * @param value value to scale
     * @return the scaled value
     */
    double scaleValue(String name, LivingEntity player, double value);

    /**
     * Scales the defense relative to the damage being dealt
     *
     * @param name    name of the buff
     * @param entity  entity to scale the defense for
     * @param damage  damage being dealt
     * @return the scaled defense
     */
    default double scaleDamageForDefense(String name, LivingEntity entity, double damage) {
        // Without being overridden, this method should make no modification to the damage
        return damage;
    }
}
