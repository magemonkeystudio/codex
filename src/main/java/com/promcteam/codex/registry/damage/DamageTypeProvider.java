package com.promcteam.codex.registry.damage;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DamageTypeProvider {

    String pluginName();

    String getNamespace();

    /**
     *
     * @param entity entity to deal damage to
     * @param amount amount of damage to deal
     * @param damageType type of damage to deal, may or may not be namespaced
     * @param damager entity to which the damage should be attributed
     * @return whether the damage could be dealt
     */
    boolean dealDamage(@NotNull LivingEntity entity, double amount, String damageType, @Nullable LivingEntity damager);
}
