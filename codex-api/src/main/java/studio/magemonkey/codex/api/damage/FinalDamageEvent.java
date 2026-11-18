package studio.magemonkey.codex.api.damage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * The final numbers of a hit, as a damage plugin has calculated them, just before it writes them to the
 * Bukkit damage event.
 * <p>
 * A damage plugin (a <em>provider</em>, such as Divinity) fires this event from its own
 * {@link EntityDamageEvent} handler, after it has worked out dodge, critical hits, blocking, defenses and
 * the like, and before it sets the damage of the Bukkit event. Other plugins can then read and change the
 * damage of the hit by type, without depending on the provider. A skill plugin such as Fabled, for
 * example, runs its on-hit triggers and buffs here instead of changing the Bukkit event's damage itself.
 * <p>
 * <h2>One event for every hit</h2>
 * This event fires for every hit on a living entity that is still going at {@code HIGHEST}: from a provider
 * if one will fire it for the hit, else from Codex itself ({@link FinalDamageFallback}). Without a provider,
 * Codex fires it with all of the Bukkit damage in {@link #DEFAULT_TYPE}, cancels the Bukkit event if this
 * event is cancelled, and otherwise sets the Bukkit damage to the new total if it changed. So a consumer
 * listens to this event only, whether or not a provider such as Divinity is installed, and never changes
 * the Bukkit damage itself. It does not need {@link #willFire(EntityDamageEvent)}, which stays for providers
 * and diagnostics.
 * <p>
 * The event may come for hits a consumer does not care about: hits of 0 damage, fake damage events a plugin
 * fires only to ask whether an attack would be allowed, and hits whose request asks for
 * {@link WeaponStatsMode#RAW}. Consumers filter those themselves.
 * <p>
 * <b>Ordering.</b> Both providers and Codex fire this event from an {@code EntityDamageEvent} handler at
 * {@code HIGHEST}. Codex loads before every plugin that depends on it, and Bukkit calls listeners of the
 * same priority in the order they were registered, so among {@code HIGHEST} damage listeners Codex's
 * fallback runs before those of the plugins that depend on Codex. Listen to this event, not to
 * {@code EntityDamageEvent} at {@code HIGHEST}, to see the final numbers.
 *
 * <h2>Damage by type</h2>
 * The damage is kept per damage type, keyed by the provider's type id ({@code physical}, {@code fire} and
 * so on; what the ids are is up to the provider). The total is always the sum of the types:
 * <ul>
 *     <li>{@link #setDamage(String, double)} changes one type, and with it the total;</li>
 *     <li>{@link #setTotalDamage(double)} scales every type by the same factor, so that the types keep
 *     their share of the hit.</li>
 * </ul>
 * After the event, the provider deals {@link #getTotalDamage()} (and may run its own after-hit effects,
 * such as life steal, on the per-type amounts).
 *
 * <h2>Cancelling</h2>
 * Cancelling this event cancels the hit: whoever fired it (the provider, or Codex's fallback) cancels the
 * Bukkit event and deals no damage. The event itself only keeps the flag.
 *
 * <h2>Providers</h2>
 * Providers announce themselves with {@link #registerProvider(Plugin, Predicate)}, so that Codex knows
 * which hits it must not fire this event for; {@link #willFire(EntityDamageEvent)} answers that question.
 * A provider's predicate must answer true only for hits it really fires the event for (or cancels):
 * for any other hit, Codex's fallback is skipped too and consumers never see the hit.
 *
 * <h2>Subclassing</h2>
 * A provider may fire this class as it is, or a subclass of its own that adds what only it knows. Listeners
 * of {@code FinalDamageEvent} receive the subclass too, and listeners of the subclass keep working:
 * <ul>
 *     <li><b>Do not declare {@code getHandlerList()} in a subclass.</b> Bukkit registers a listener with the
 *     handler list of the nearest class, starting at the listened class and walking up, that declares a
 *     static {@code getHandlerList()} method, and it calls the event on the one list that
 *     {@link #getHandlers()} returns. With no {@code getHandlerList()} of its own, every listener of the
 *     subclass lands in this class's list, and Bukkit hands each listener only the events that are
 *     instances of the class it listens to. {@link #getHandlers()} is final for this reason.</li>
 *     <li>A subclass that keeps the per-type damage in storage of its own passes an empty map to the
 *     constructor and overrides {@link #getDamages()}, {@link #getDamage(String)} and
 *     {@link #setDamage(String, double)}, all three. {@link #getTotalDamage()} and
 *     {@link #setTotalDamage(double)} are built on those three and stay correct without being overridden.
 *     A subclass may still override them (for speed, or to split the damage of a hit that has none in its
 *     own way), as long as the total stays the sum of the types.</li>
 *     <li>{@link #isCancelled()} and {@link #setCancelled(boolean)} may be overridden in pairs, for example
 *     to share a flag the subclass already has.</li>
 * </ul>
 * <p>
 * This event is fired, read and changed on the server thread only. It uses nothing newer than the oldest
 * Minecraft version Codex supports.
 */
public class FinalDamageEvent extends Event implements Cancellable {
    /**
     * The type that {@link #setTotalDamage(double)} puts the damage in when the hit has no damage types at
     * all.
     */
    public static final String DEFAULT_TYPE = "default";

    private static final HandlerList HANDLERS = new HandlerList();

    /** Registered providers, in the order they registered. */
    private static final Map<Plugin, Predicate<? super EntityDamageEvent>> PROVIDERS = new LinkedHashMap<>();

    @NotNull
    private final LivingEntity        victim;
    @Nullable
    private final LivingEntity        attacker;
    @Nullable
    private final Projectile          projectile;
    @NotNull
    private final DamageCause         cause;
    @NotNull
    private final EntityDamageEvent   damageEvent;
    @Nullable
    private final DamageRequest       request;
    @NotNull
    private final DamageOrigin        origin;
    @NotNull
    private final WeaponStatsMode     weaponStatsMode;
    @NotNull
    private final Map<String, Double> damages;
    private       boolean             cancelled;

    /**
     * Creates the event. Only a provider, or Codex's fallback, creates it, right before it calls it.
     *
     * @param victim          the entity that is hit
     * @param attacker        the entity the hit is attributed to: the shooter of a projectile, the caster of a
     *                        skill. Null if there is none.
     * @param projectile      the projectile that hit, or null if it was not a projectile
     * @param cause           the cause of the hit. Usually that of the Bukkit event, or the one the
     *                        {@link DamageRequest} asks for.
     * @param damageEvent     the Bukkit event being handled
     * @param request         the request that describes the hit (see {@link DamageContexts#claim}), or null if
     *                        the hit was not announced
     * @param origin          what deals the damage: the request's origin, or the provider's best guess when
     *                        there is no request
     * @param weaponStatsMode how the attacker's equipment took part: the request's mode, or
     *                        {@link WeaponStatsMode#ADD} when there is no request
     * @param damages         the final damage by type id, in the order the provider wants them listed. The map
     *                        is copied. Amounts must be finite numbers and not negative. A subclass with storage of
     *                        its own passes an empty map (see the class description).
     */
    public FinalDamageEvent(@NotNull LivingEntity victim,
                            @Nullable LivingEntity attacker,
                            @Nullable Projectile projectile,
                            @NotNull DamageCause cause,
                            @NotNull EntityDamageEvent damageEvent,
                            @Nullable DamageRequest request,
                            @NotNull DamageOrigin origin,
                            @NotNull WeaponStatsMode weaponStatsMode,
                            @NotNull Map<String, Double> damages) {
        super(false);
        this.victim = Objects.requireNonNull(victim, "victim");
        this.attacker = attacker;
        this.projectile = projectile;
        this.cause = Objects.requireNonNull(cause, "cause");
        this.damageEvent = Objects.requireNonNull(damageEvent, "damageEvent");
        this.request = request;
        this.origin = Objects.requireNonNull(origin, "origin");
        this.weaponStatsMode = Objects.requireNonNull(weaponStatsMode, "weaponStatsMode");
        this.damages = new LinkedHashMap<>();
        Objects.requireNonNull(damages, "damages").forEach((type, amount) -> {
            Objects.requireNonNull(type, "damage type");
            this.damages.put(type, checkAmount(amount == null ? Double.NaN : amount));
        });
    }

    /**
     * @return the entity that is hit
     */
    @NotNull
    public LivingEntity getVictim() {
        return victim;
    }

    /**
     * @return the entity the hit is attributed to (the shooter of a projectile, the caster of a skill), or
     * null if there is none
     */
    @Nullable
    public LivingEntity getAttacker() {
        return attacker;
    }

    /**
     * @return the projectile that hit, or null if the hit was not from a projectile
     */
    @Nullable
    public Projectile getProjectile() {
        return projectile;
    }

    /**
     * @return the cause of the hit
     */
    @NotNull
    public DamageCause getCause() {
        return cause;
    }

    /**
     * The Bukkit event being handled. Read it, but leave its damage alone: whoever fired this event sets it
     * from this event's total when this event is over. To stop the hit, cancel this event.
     *
     * @return the Bukkit damage event of the hit
     */
    @NotNull
    public EntityDamageEvent getDamageEvent() {
        return damageEvent;
    }

    /**
     * @return the request that describes the hit, or null if the hit was not announced through
     * {@link DamageContexts}
     */
    @Nullable
    public DamageRequest getRequest() {
        return request;
    }

    /**
     * @return what deals the damage: the request's origin, or the provider's best guess when there is no
     * request
     */
    @NotNull
    public DamageOrigin getOrigin() {
        return origin;
    }

    /**
     * @return how the attacker's equipment took part in the hit: the request's mode, or
     * {@link WeaponStatsMode#ADD} when there is no request
     */
    @NotNull
    public WeaponStatsMode getWeaponStatsMode() {
        return weaponStatsMode;
    }

    /**
     * The amounts the dealer asked for, as opposed to the final amounts of {@link #getDamages()}.
     *
     * @return the request's amounts by type, or an empty map if there is no request
     */
    @NotNull
    public Map<String, Double> getRequestedAmounts() {
        return request == null ? Collections.emptyMap() : request.getAmounts();
    }

    /**
     * The final damage of the hit by type id, in the provider's order. Types with an amount of 0 may be
     * listed.
     * <p>
     * A subclass with storage of its own overrides this together with {@link #getDamage(String)} and
     * {@link #setDamage(String, double)}.
     *
     * @return the damage by type. The map cannot be changed; use {@link #setDamage(String, double)}.
     */
    @NotNull
    public Map<String, Double> getDamages() {
        return Collections.unmodifiableMap(damages);
    }

    /**
     * @param type a damage type id
     * @return the final damage of that type, or 0 if the hit has none of it
     */
    public double getDamage(@NotNull String type) {
        Double amount = damages.get(type);
        return amount == null ? 0 : amount;
    }

    /**
     * Sets the final damage of one type. The total changes by the difference. A type the hit did not have
     * is added. Setting a type to 0 removes its damage but may keep it listed.
     * <p>
     * Which ids a provider knows is up to it. A provider may ignore, or map, an id it does not know.
     *
     * @param type   a damage type id
     * @param amount the amount, which must be a finite number and not negative
     * @throws IllegalArgumentException if the amount is negative, infinite or not a number
     */
    public void setDamage(@NotNull String type, double amount) {
        Objects.requireNonNull(type, "type");
        damages.put(type, checkAmount(amount));
    }

    /**
     * @return the final damage of the hit: the sum of {@link #getDamages()}
     */
    public double getTotalDamage() {
        double total = 0;
        for (double amount : getDamages().values()) {
            total += amount;
        }
        return total;
    }

    /**
     * Sets the final damage of the hit by scaling every type by the same factor, so that each keeps its
     * share. Setting 0 sets every type to 0.
     * <p>
     * A hit whose total is 0 has no shares to keep. Then the new total is split evenly between the listed
     * types, or, if there are none, given to {@link #DEFAULT_TYPE}.
     *
     * @param total the new total, which must be a finite number and not negative
     * @throws IllegalArgumentException if the total is negative, infinite or not a number
     */
    public void setTotalDamage(double total) {
        checkAmount(total);
        // Copy first: setDamage may change the map getDamages() is a view of.
        List<Map.Entry<String, Double>> types = new ArrayList<>(new LinkedHashMap<>(getDamages()).entrySet());
        double current = 0;
        for (Map.Entry<String, Double> entry : types) {
            current += entry.getValue();
        }

        if (current > 0) {
            double factor = total / current;
            for (Map.Entry<String, Double> entry : types) {
                setDamage(entry.getKey(), entry.getValue() * factor);
            }
        } else if (!types.isEmpty()) {
            double share = total / types.size();
            for (Map.Entry<String, Double> entry : types) {
                setDamage(entry.getKey(), share);
            }
        } else if (total > 0) {
            setDamage(DEFAULT_TYPE, total);
        }
    }

    /**
     * @return whether the hit is cancelled. The provider then cancels the Bukkit event and deals no damage.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Cancels the hit, or lets it through again. A cancelled hit deals no damage: the provider cancels the
     * Bukkit event and skips its after-hit effects.
     *
     * @param cancel whether to cancel the hit
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Final, so that every subclass shares this class's handler list. See the class description.
     */
    @Override
    @NotNull
    public final HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * The handler list Bukkit registers listeners of this event, and of every subclass, with.
     *
     * @return the handler list
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    // ------------------------------------------------------------------------------------------------------
    // Providers
    // ------------------------------------------------------------------------------------------------------

    /**
     * Announces a plugin that fires this event. Call it from {@code onEnable}, and call
     * {@link #unregisterProvider(Plugin)} from {@code onDisable}. Registering again replaces the earlier
     * predicate.
     * <p>
     * The predicate answers, for a Bukkit damage event, whether the provider will fire this event for it,
     * should the hit still be going (not cancelled) when the provider's handler runs. It is asked from other
     * plugins' damage handlers, possibly before the provider's handler has run, so it must not change
     * anything, must be cheap, and must not rely on what the provider works out during the hit. It should
     * answer true for every hit the provider calculates; answering true for a hit the provider then leaves
     * alone means the asking plugin skips it entirely.
     * <p>
     * Use {@link #registerProvider(Plugin)} if the provider fires the event for every hit.
     *
     * @param provider the plugin that fires the event
     * @param fires    whether the provider fires the event for a given Bukkit damage event
     */
    public static void registerProvider(@NotNull Plugin provider,
                                        @NotNull Predicate<? super EntityDamageEvent> fires) {
        PROVIDERS.put(Objects.requireNonNull(provider, "provider"), Objects.requireNonNull(fires, "fires"));
    }

    /**
     * Announces a plugin that fires this event for every damage event it sees that is still going when its
     * handler runs. Same as {@code registerProvider(provider, event -> true)}.
     *
     * @param provider the plugin that fires the event
     */
    public static void registerProvider(@NotNull Plugin provider) {
        registerProvider(provider, event -> true);
    }

    /**
     * Withdraws a provider. Doing so for a plugin that is not registered does nothing.
     *
     * @param provider the plugin that no longer fires the event
     */
    public static void unregisterProvider(@NotNull Plugin provider) {
        PROVIDERS.remove(provider);
    }

    /**
     * @return whether any enabled plugin has registered as a provider
     */
    public static boolean hasProvider() {
        for (Plugin provider : PROVIDERS.keySet()) {
            if (provider.isEnabled()) return true;
        }
        return false;
    }

    /**
     * Whether a provider will fire this event for a hit, should the hit not be cancelled before the
     * provider's handler runs.
     * <p>
     * Codex's fallback asks this to decide whether it fires the event itself. Consumers do not need it: the
     * event comes for every hit either way (see the class description). It stays for providers and for
     * diagnostics.
     * A provider that has been disabled without unregistering is not counted.
     *
     * @param event the Bukkit damage event of the hit
     * @return whether an enabled provider says it fires this event for the hit
     */
    public static boolean willFire(@NotNull EntityDamageEvent event) {
        Objects.requireNonNull(event, "event");
        for (Map.Entry<Plugin, Predicate<? super EntityDamageEvent>> entry : PROVIDERS.entrySet()) {
            if (entry.getKey().isEnabled() && entry.getValue().test(event)) return true;
        }
        return false;
    }

    private static double checkAmount(double amount) {
        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Damage must be a finite number, 0 or more, got " + amount);
        }
        return amount;
    }
}
