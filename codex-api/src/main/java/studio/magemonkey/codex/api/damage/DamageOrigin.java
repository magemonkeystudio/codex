package studio.magemonkey.codex.api.damage;

/**
 * What kind of thing is dealing the damage described by a {@link DamageRequest}.
 */
public enum DamageOrigin {
    /** A swing with the weapon in hand. */
    WEAPON_MELEE,
    /** A projectile launched from a weapon, such as an arrow or a thrown trident. */
    WEAPON_PROJECTILE,
    /** A skill or spell. */
    SKILL,
    /** A follow-up effect of an earlier hit: bleeding, burning, thorns, area damage and the like. */
    EFFECT,
    /** The world: falling, fire, drowning and so on. */
    ENVIRONMENT,
    /** Anything else, such as a command or a plugin that does not fit the other origins. */
    EXTERNAL
}
