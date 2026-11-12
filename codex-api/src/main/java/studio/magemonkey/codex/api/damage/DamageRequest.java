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
    @Getter(AccessLevel.NONE)
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
     * @return whether the hit should knock the target back
     */
    public boolean hasKnockback() {
        return knockback;
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

        @NotNull
        public DamageRequest build() {
            return new DamageRequest(this);
        }
    }
}
