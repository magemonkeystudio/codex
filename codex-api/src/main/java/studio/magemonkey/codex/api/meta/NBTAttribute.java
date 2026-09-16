package studio.magemonkey.codex.api.meta;

import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.compat.VersionManager;
import studio.magemonkey.codex.core.Version;

import java.util.UUID;

public enum NBTAttribute {
    ARMOR(
            "armor",
            "1f1173-9999-3333-5555-99cb0245f9c1",
            Version.V1_21_R2),

    ARMOR_TOUGHNESS(
            "generic.armorToughness",
            "1f1173-9999-3333-5555-99cb0245f9c2",
            Version.V1_16_R3),

    ATTACK_DAMAGE(
            "generic.attackDamage",
            "1f1173-9999-3333-5555-99cb0245f9c3",
            Version.V1_16_R3),

    ATTACK_KNOCKBACK(
            "generic.attackKnockback",
            "1f1173-9999-3333-5555-99cb0245f9c8",
            Version.V1_16_R3),

    ATTACK_SPEED(
            "generic.attackSpeed",
            "1f1173-9999-3333-5555-99cb0245f9c4",
            Version.V1_16_R3),

    BLOCK_BREAK_SPEED(
            "block_break_speed",
            "1f1173-9999-3333-5555-99cb0245f9c9",
            Version.V1_20_R4),

    BLOCK_INTERACTION_RANGE(
            "block_interaction_range",
            "1f1173-9999-3333-5555-99cb0245f9ca",
            Version.V1_20_R4),

    BURNING_TIME(
            "burning_time",
            "1f1173-9999-3333-5555-99cb0245f9cb",
            Version.V1_21_R1),

    ENTITY_INTERACTION_RANGE(
            "entity_interaction_range",
            "1f1173-9999-3333-5555-99cb0245f9cc",
            Version.V1_20_R4),

    EXPLOSION_KNOCKBACK_RESISTANCE(
            "explosion_knockback_resistance",
            "1f1173-9999-3333-5555-99cb0245f9cd",
            Version.V1_21_R1),

    FALL_DAMAGE_MULTIPLIER(
            "fall_damage_multiplier",
            "1f1173-9999-3333-5555-99cb0245f9ce",
            Version.V1_20_R4),

    FLYING_SPEED(
            "generic.flyingSpeed",
            "1f1173-9999-3333-5555-99cb0245f9cf",
            Version.V1_16_R3),

    FOLLOW_RANGE(
            "generic.followRange",
            "1f1173-9999-3333-5555-99cb0245f9d0",
            Version.V1_16_R3),

    GRAVITY(
            "gravity",
            "1f1173-9999-3333-5555-99cb0245f9d1",
            Version.V1_20_R4),

    JUMP_STRENGTH(
            "generic.jumpStrength",
            "1f1173-9999-3333-5555-99cb0245f9d2",
            Version.V1_16_R3,
            "HORSE_JUMP_STRENGTH"),

    MOVEMENT_SPEED(
            "generic.movementSpeed",
            "1f1173-9999-3333-5555-99cb0245f9c5",
            Version.V1_16_R3),

    KNOCKBACK_RESISTANCE(
            "generic.knockbackResistance",
            "1f1173-9999-3333-5555-99cb0245f9c7",
            Version.V1_16_R3),

    LUCK(
            "luck",
            "1f1173-9999-3333-5555-99cb0245f9d3",
            Version.V1_21_R2),

    MAX_ABSORPTION(
            "max_absorption",
            "1f1173-9999-3333-5555-99cb0245f9d4",
            Version.V1_20_R2),

    MAX_HEALTH(
            "generic.maxHealth",
            "1f1173-9999-3333-5555-99cb0245f9c6",
            Version.V1_16_R3),

    MINING_EFFICIENCY(
            "mining_efficiency",
            "1f1173-9999-3333-5555-99cb0245f9d5",
            Version.V1_21_R1),

    MOVEMENT_EFFICIENCY(
            "movement_efficiency",
            "1f1173-9999-3333-5555-99cb0245f9d6",
            Version.V1_21_R1),

    OXYGEN_BONUS(
            "oxygen_bonus",
            "1f1173-9999-3333-5555-99cb0245f9d7",
            Version.V1_21_R1),

    SAFE_FALL_DISTANCE(
            "safe_fall_distance",
            "1f1173-9999-3333-5555-99cb0245f9d8",
            Version.V1_20_R4),

    SCALE(
            "scale",
            "1f1173-9999-3333-5555-99cb0245f9d9",
            Version.V1_20_R4),

    SNEAKING_SPEED(
            "sneaking_speed",
            "1f1173-9999-3333-5555-99cb0245f9da",
            Version.V1_21_R1),

    STEP_HEIGHT(
            "step_height",
            "1f1173-9999-3333-5555-99cb0245f9db",
            Version.V1_20_R4),

    SUBMERGED_MINING_SPEED(
            "submerged_mining_speed",
            "1f1173-9999-3333-5555-99cb0245f9dc",
            Version.V1_21_R1),

    SWEEPING_DAMAGE_RATIO(
            "sweeping_damage_ratio",
            "1f1173-9999-3333-5555-99cb0245f9dd",
            Version.V1_21_R1),

    WATER_MOVEMENT_EFFICIENCY(
            "water_movement_efficiency",
            "1f1173-9999-3333-5555-99cb0245f9de",
            Version.V1_21_R1),

    SPAWN_REINFORCEMENTS(
            "zombie.spawnReinforcements",
            "1f1173-9999-3333-5555-99cb0245f9df",
            Version.V1_16_R3,
            "ZOMBIE_SPAWN_REINFORCEMENTS"),
    ;

    private final String    nmsName;
    private final Version   introducedVersion;
    private final String[]  nmsLookupNames;
    private       Attribute att;
    private final String    uuid;

    NBTAttribute(@NotNull String nmsName,
                 @NotNull String uuid,
                 @NotNull Version introducedVersion,
                 @NotNull String... fallbacks) {
        this.nmsName = nmsName;
        this.introducedVersion = introducedVersion;
        this.nmsLookupNames = new String[fallbacks.length + 1];
        this.nmsLookupNames[0] = this.name();
        System.arraycopy(fallbacks, 0, this.nmsLookupNames, 1, fallbacks.length);
        this.uuid = uuid;
    }

    @NotNull
    public String getNmsName() {
        return this.nmsName;
    }

    @NotNull
    public Attribute getAttribute() {
        if (!Version.CURRENT.isAtLeast(this.introducedVersion)) {
            throw new IllegalArgumentException("Attribute '" + this.name()
                    + "' is only supported from " + this.introducedVersion.name() + " and above.");
        }

        if (this.att != null) return this.att;

        IllegalArgumentException last = null;
        for (String lookupName : this.nmsLookupNames) {
            try {
                this.att = VersionManager.getNms().getAttribute(lookupName);
                return this.att;
            } catch (IllegalArgumentException ex) {
                last = ex;
            }
        }

        throw new IllegalArgumentException("Unknown attribute mappings for '" + this.name() + "'.", last);
    }

    @NotNull
    public UUID getUUID(@NotNull EquipmentSlot slot) {
        return UUID.fromString(slot.ordinal() + this.uuid);
    }
}
