package org.bukkit.damage;

import org.bukkit.NamespacedKey;

/**
 * Test stand-in for the Bukkit class of this name, which the Bukkit version Codex compiles against does not
 * have. It has the members the real one has on 1.20.4 and newer, as far as Codex uses them. Not every
 * constant exists here, on purpose: real servers differ in which ones they have too.
 */
public final class DamageType {
    public static final DamageType IN_FIRE        = new DamageType("in_fire");
    public static final DamageType MAGIC          = new DamageType("magic");
    public static final DamageType INDIRECT_MAGIC = new DamageType("indirect_magic");
    public static final DamageType THORNS         = new DamageType("thorns");
    public static final DamageType PLAYER_ATTACK  = new DamageType("player_attack");
    public static final DamageType MOB_ATTACK     = new DamageType("mob_attack");
    public static final DamageType ARROW          = new DamageType("arrow");
    public static final DamageType EXPLOSION      = new DamageType("explosion");

    private final NamespacedKey key;

    private DamageType(String key) {
        this.key = NamespacedKey.minecraft(key);
    }

    public NamespacedKey getKey() {
        return key;
    }
}
