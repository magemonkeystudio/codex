package studio.magemonkey.codex.manager.types;

import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;

public enum MobGroup {

    ANIMAL,
    MONSTER,
    NPC,
    WATER,
    AMBIENT,
    OTHER,
    ;

    @NotNull
    public static MobGroup getMobGroup(@NotNull Entity entity) {
        if (entity instanceof Animals) {
            return MobGroup.ANIMAL;
        }
        if (entity instanceof Monster) {
            return MobGroup.MONSTER;
        }
        if (entity instanceof Ambient) {
            return MobGroup.AMBIENT;
        }
        if (entity instanceof WaterMob) {
            return MobGroup.WATER;
        }
        if (entity instanceof NPC) {
            return MobGroup.NPC;
        }
        return MobGroup.OTHER;
    }
}
