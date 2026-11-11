package studio.magemonkey.codex.api.damage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A description of a hit, written by the plugin that deals it and read by the plugin that calculates it.
 * <p>
 * Without a request, a plugin handling an {@code EntityDamageEvent} can only guess what a call to
 * {@code damage()} was meant to be. A request states it up front. It is handed over through
 * {@link DamageContexts}.
 * <p>
 * A request is immutable. Build one with {@link #builder()}.
 */
public final class DamageRequest {
    private final DamageOrigin        origin;
    private final LivingEntity        source;
    private final DamageCause         cause;
    private final Map<String, Double> amounts;
    private final WeaponStatsMode     weaponStats;
    private final boolean             knockback;

    private DamageRequest(Builder builder) {
        this.origin = builder.origin;
        this.source = builder.source;
        this.cause = builder.cause;
        this.amounts = Collections.unmodifiableMap(new LinkedHashMap<>(builder.amounts));
        this.weaponStats = builder.weaponStats;
        this.knockback = builder.knockback;
    }

    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return what kind of thing deals the damage
     */
    @NotNull
    public DamageOrigin getOrigin() {
        return origin;
    }

    /**
     * @return the entity the damage is attributed to, or null if there is none
     */
    @Nullable
    public LivingEntity getSource() {
        return source;
    }

    /**
     * @return the cause the dealer wants the damage to have, or null to leave it as Bukkit reports it
     */
    @Nullable
    public DamageCause getCause() {
        return cause;
    }

    /**
     * The damage of the hit, by damage type, in the order the amounts were added.
     * <p>
     * A type is the id the dealer used, exactly as given. It may carry a provider's namespace
     * ({@code DIVINITY_fire}) or be a plain classification ({@code default}).
     *
     * @return an unmodifiable map of damage type to amount
     */
    @NotNull
    public Map<String, Double> getAmounts() {
        return amounts;
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

    /**
     * @return how the attacker's equipment should take part in the hit
     */
    @NotNull
    public WeaponStatsMode getWeaponStats() {
        return weaponStats;
    }

    /**
     * @return whether the hit should knock the target back
     */
    public boolean hasKnockback() {
        return knockback;
    }

    @Override
    public String toString() {
        return "DamageRequest{origin=" + origin
                + ", source=" + (source == null ? null : source.getUniqueId())
                + ", cause=" + cause
                + ", amounts=" + amounts
                + ", weaponStats=" + weaponStats
                + ", knockback=" + knockback + '}';
    }

    public static final class Builder {
        private final Map<String, Double> amounts     = new LinkedHashMap<>();
        private       DamageOrigin        origin      = DamageOrigin.EXTERNAL;
        private       LivingEntity        source;
        private       DamageCause         cause;
        private       WeaponStatsMode     weaponStats = WeaponStatsMode.ADD;
        private       boolean             knockback   = true;

        private Builder() {}

        /**
         * Defaults to {@link DamageOrigin#EXTERNAL}.
         */
        @NotNull
        public Builder origin(@NotNull DamageOrigin origin) {
            this.origin = Objects.requireNonNull(origin, "origin");
            return this;
        }

        @NotNull
        public Builder source(@Nullable LivingEntity source) {
            this.source = source;
            return this;
        }

        @NotNull
        public Builder cause(@Nullable DamageCause cause) {
            this.cause = cause;
            return this;
        }

        /**
         * Adds damage of one type. Adding the same type again adds to its amount.
         *
         * @param type   the damage type id, kept exactly as given
         * @param amount the amount, which must be a number and not negative
         */
        @NotNull
        public Builder amount(@NotNull String type, double amount) {
            Objects.requireNonNull(type, "type");
            if (Double.isNaN(amount) || amount < 0) {
                throw new IllegalArgumentException("Damage amount must be 0 or more, got " + amount);
            }
            amounts.merge(type, amount, Double::sum);
            return this;
        }

        /**
         * Defaults to {@link WeaponStatsMode#ADD}.
         */
        @NotNull
        public Builder weaponStats(@NotNull WeaponStatsMode weaponStats) {
            this.weaponStats = Objects.requireNonNull(weaponStats, "weaponStats");
            return this;
        }

        /**
         * Defaults to true.
         */
        @NotNull
        public Builder knockback(boolean knockback) {
            this.knockback = knockback;
            return this;
        }

        @NotNull
        public DamageRequest build() {
            return new DamageRequest(this);
        }
    }
}
