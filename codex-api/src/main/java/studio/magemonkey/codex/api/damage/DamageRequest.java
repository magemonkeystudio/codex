package studio.magemonkey.codex.api.damage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A description of a hit, written by the plugin that deals it and read by the plugin that calculates it.
 * <p>
 * Without a request, a plugin handling an {@code EntityDamageEvent} can only guess what a call to
 * {@code damage()} was meant to be. A request states it up front. It is handed over through
 * {@link DamageContexts}.
 * <p>
 * A request is immutable. Build one with {@link #builder()}.
 */
@Getter
@ToString
public final class DamageRequest {
    /** What kind of thing deals the damage. */
    @NotNull
    private final DamageOrigin        origin;
    /** The entity the damage is attributed to, or null if there is none. */
    @Nullable
    private final LivingEntity        source;
    /** The cause the dealer wants the damage to have, or null to leave it as Bukkit reports it. */
    @Nullable
    private final DamageCause         cause;
    /**
     * The damage of the hit, by damage type, in the order the amounts were added. The map cannot be
     * changed.
     * <p>
     * A type is the id the dealer used, exactly as given. It may carry a provider's namespace
     * ({@code DIVINITY_fire}) or be a plain classification ({@code default}).
     */
    @NotNull
    private final Map<String, Double> amounts;
    /** How the attacker's equipment should take part in the hit. */
    @NotNull
    private final WeaponStatsMode     weaponStats;
    /**
     * What every damage type of the attacker's weapon is multiplied by, unless a type has its own factor.
     * 1 leaves the weapon's damage as it is. See {@link #adjustWeaponDamage}.
     */
    private final double              weaponScale;
    /**
     * What is added to every damage type of the attacker's weapon, unless a type has its own amount. It may
     * be negative. See {@link #adjustWeaponDamage}.
     */
    private final double              weaponBonus;
    @Getter(AccessLevel.NONE)
    private final Map<String, Double> weaponScales;
    @Getter(AccessLevel.NONE)
    private final Map<String, Double> weaponBonuses;
    @Getter(AccessLevel.NONE)
    private final boolean             knockback;

    private DamageRequest(Builder builder) {
        this.origin = builder.origin;
        this.source = builder.source;
        this.cause = builder.cause;
        this.amounts = Collections.unmodifiableMap(new LinkedHashMap<>(builder.amounts));
        this.weaponStats = builder.weaponStats;
        this.weaponScale = builder.weaponScale;
        this.weaponBonus = builder.weaponBonus;
        this.weaponScales = Collections.unmodifiableMap(new LinkedHashMap<>(builder.weaponScales));
        this.weaponBonuses = Collections.unmodifiableMap(new LinkedHashMap<>(builder.weaponBonuses));
        this.knockback = builder.knockback;
    }

    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return whether the hit should knock the target back
     */
    public boolean hasKnockback() {
        return knockback;
    }

    /**
     * @param type a damage type id of the plugin that calculates the damage, without a namespace
     * @return what the weapon's damage of that type is multiplied by
     */
    public double getWeaponScale(@NotNull String type) {
        return weaponScales.getOrDefault(type, weaponScale);
    }

    /**
     * @param type a damage type id of the plugin that calculates the damage, without a namespace
     * @return what is added to the weapon's damage of that type
     */
    public double getWeaponBonus(@NotNull String type) {
        return weaponBonuses.getOrDefault(type, weaponBonus);
    }

    /**
     * @return whether this request changes the weapon's damage at all
     */
    public boolean adjustsWeaponDamage() {
        return weaponScale != 1 || weaponBonus != 0 || !weaponScales.isEmpty() || !weaponBonuses.isEmpty();
    }

    /**
     * Applies this request's scale and bonus to one damage type of the attacker's weapon: the amount is
     * multiplied by the scale, then the bonus is added.
     * <p>
     * A type the weapon does not deal stays at 0, so a bonus never gives a weapon a damage type it does not
     * have. The result can be negative when the bonus is; what to do with that is up to the caller.
     * <p>
     * This only means something where the weapon's damage is part of the hit, which is
     * {@link WeaponStatsMode#ADD}.
     *
     * @param type   the damage type id, without a namespace
     * @param amount the weapon's damage of that type
     * @return the adjusted amount
     */
    public double adjustWeaponDamage(@NotNull String type, double amount) {
        if (amount == 0) return amount;
        return amount * getWeaponScale(type) + getWeaponBonus(type);
    }

    /**
     * @return the sum of all amounts
     */
    public double getTotal() {
        double total = 0;
        for (double amount : amounts.values()) {
            total += amount;
        }
        return total;
    }

    @Setter
    @Accessors(fluent = true, chain = true)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {
        @Setter(AccessLevel.NONE)
        private final Map<String, Double> amounts     = new LinkedHashMap<>();
        /** Defaults to {@link DamageOrigin#EXTERNAL}. */
        @NonNull
        private       DamageOrigin        origin      = DamageOrigin.EXTERNAL;
        @Nullable
        private       LivingEntity        source;
        @Nullable
        private       DamageCause         cause;
        /** Defaults to {@link WeaponStatsMode#ADD}. */
        @NonNull
        private       WeaponStatsMode     weaponStats = WeaponStatsMode.ADD;
        @Setter(AccessLevel.NONE)
        private final Map<String, Double> weaponScales  = new LinkedHashMap<>();
        @Setter(AccessLevel.NONE)
        private final Map<String, Double> weaponBonuses = new LinkedHashMap<>();
        @Setter(AccessLevel.NONE)
        private       double              weaponScale   = 1;
        @Setter(AccessLevel.NONE)
        private       double              weaponBonus   = 0;
        /** Defaults to true. */
        private       boolean             knockback   = true;

        /**
         * Adds damage of one type. Adding the same type again adds to its amount.
         *
         * @param type   the damage type id, kept exactly as given
         * @param amount the amount, which must be a number and not negative
         */
        @NotNull
        public Builder amount(@NonNull String type, double amount) {
            if (Double.isNaN(amount) || amount < 0) {
                throw new IllegalArgumentException("Damage amount must be 0 or more, got " + amount);
            }
            amounts.merge(type, amount, Double::sum);
            return this;
        }

        /**
         * Multiplies every damage type of the attacker's weapon. Defaults to 1.
         *
         * @param factor the factor, which must be a number and not negative
         */
        @NotNull
        public Builder weaponScale(double factor) {
            this.weaponScale = checkScale(factor);
            return this;
        }

        /**
         * Multiplies one damage type of the attacker's weapon, in place of the factor for every type.
         *
         * @param type   the damage type id of the plugin that calculates the damage, without a namespace
         * @param factor the factor, which must be a number and not negative
         */
        @NotNull
        public Builder weaponScale(@NonNull String type, double factor) {
            weaponScales.put(type, checkScale(factor));
            return this;
        }

        /**
         * Adds to every damage type the attacker's weapon deals. Defaults to 0.
         *
         * @param amount the amount, which may be negative
         */
        @NotNull
        public Builder weaponBonus(double amount) {
            this.weaponBonus = checkBonus(amount);
            return this;
        }

        /**
         * Adds to one damage type of the attacker's weapon, in place of the amount for every type.
         *
         * @param type   the damage type id of the plugin that calculates the damage, without a namespace
         * @param amount the amount, which may be negative
         */
        @NotNull
        public Builder weaponBonus(@NonNull String type, double amount) {
            weaponBonuses.put(type, checkBonus(amount));
            return this;
        }

        private static double checkScale(double factor) {
            if (Double.isNaN(factor) || factor < 0) {
                throw new IllegalArgumentException("Weapon scale must be 0 or more, got " + factor);
            }
            return factor;
        }

        private static double checkBonus(double amount) {
            if (Double.isNaN(amount)) {
                throw new IllegalArgumentException("Weapon bonus must be a number");
            }
            return amount;
        }

        @NotNull
        public DamageRequest build() {
            return new DamageRequest(this);
        }
    }
}
