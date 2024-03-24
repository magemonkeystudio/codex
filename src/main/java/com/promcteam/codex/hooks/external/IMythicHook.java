package com.promcteam.codex.hooks.external;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public interface IMythicHook {

    boolean isMythicMob(@NotNull Entity e);

    String getMythicNameByEntity(@NotNull Entity e);

    Object getMythicInstance(@NotNull Entity e);

    boolean isDropTable(@NotNull String table);

    double getLevel(@NotNull Entity e);

    @NotNull
    List<String> getMythicIds();

    void setSkillDamage(@NotNull Entity e, double d);

    void castSkill(@NotNull Entity e, @NotNull String skill);

    void killMythic(@NotNull Entity e);

    boolean isValid(@NotNull String name);

    @NotNull
    String getName(@NotNull String mobId);

    @Nullable
    Entity spawnMythicMob(@NotNull String name, @NotNull Location loc, int level);

    void taunt(LivingEntity target, LivingEntity source, double amount);

}
