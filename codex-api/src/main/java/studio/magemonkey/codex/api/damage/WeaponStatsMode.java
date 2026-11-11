package studio.magemonkey.codex.api.damage;

/**
 * How much of the attacker's equipment the plugin that calculates the damage should bring into a hit
 * described by a {@link DamageRequest}.
 * <p>
 * Codex only carries this value. What each mode does is up to the plugin that reads the request; the
 * descriptions below are what that plugin is asked to do.
 */
public enum WeaponStatsMode {
    /** Add the requested amounts on top of the damage of the attacker's weapon. */
    ADD,
    /** Use only the requested amounts. The weapon's own damage is left out. */
    REPLACE,
    /**
     * Use only the requested amounts and skip the weapon's on-hit effects, but still apply the defender's
     * side of the calculation, such as defense, dodge and block.
     */
    IGNORE,
    /** Deal the requested amount as it is. The calculating plugin should not touch the hit at all. */
    RAW
}
