package studio.magemonkey.codex.api.damage;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * For servers from 1.20.4 on. Codex is compiled against an older Bukkit, so the damage source classes are
 * reached by reflection: {@code org.bukkit.damage.DamageSource}, its builder, {@code DamageType},
 * {@code Damageable#damage(double, DamageSource)} and {@code EntityDamageEvent#getDamageSource()}.
 * <p>
 * Where an event or entity turns out not to have those methods, the legacy behaviour is used for that call.
 */
final class ModernDamageSourceAdapter implements DamageSourceAdapter {
    /** Damage cause name to the names of the DamageType constants to try, in order. */
    private static final Map<String, String[]> TYPES_WITHOUT_ENTITY = new HashMap<>();
    private static final Map<String, String[]> TYPES_WITH_ENTITY    = new HashMap<>();

    static {
        both("FIRE", "IN_FIRE");
        both("FIRE_TICK", "ON_FIRE");
        both("CAMPFIRE", "CAMPFIRE", "IN_FIRE");
        both("LAVA", "LAVA");
        both("HOT_FLOOR", "HOT_FLOOR");
        both("DROWNING", "DROWN");
        both("SUFFOCATION", "IN_WALL");
        both("CRAMMING", "CRAMMING");
        both("FALL", "FALL");
        both("FLY_INTO_WALL", "FLY_INTO_WALL");
        both("VOID", "OUT_OF_WORLD");
        both("WORLD_BORDER", "OUTSIDE_BORDER");
        both("KILL", "GENERIC_KILL");
        both("LIGHTNING", "LIGHTNING_BOLT");
        both("STARVATION", "STARVE");
        both("WITHER", "WITHER");
        both("FREEZE", "FREEZE");
        both("DRYOUT", "DRY_OUT");
        both("CONTACT", "CACTUS");
        both("FALLING_BLOCK", "FALLING_BLOCK");
        both("DRAGON_BREATH", "DRAGON_BREATH");
        both("SONIC_BOOM", "SONIC_BOOM");
        both("THORNS", "THORNS");
        both("POISON", "MAGIC");
        both("CUSTOM", "GENERIC");
        both("BLOCK_EXPLOSION", "EXPLOSION");
        TYPES_WITHOUT_ENTITY.put("MAGIC", new String[]{"MAGIC"});
        TYPES_WITH_ENTITY.put("MAGIC", new String[]{"INDIRECT_MAGIC", "MAGIC"});
        TYPES_WITHOUT_ENTITY.put("ENTITY_EXPLOSION", new String[]{"EXPLOSION"});
        TYPES_WITH_ENTITY.put("ENTITY_EXPLOSION", new String[]{"PLAYER_EXPLOSION", "EXPLOSION"});
        // ENTITY_ATTACK and ENTITY_SWEEP_ATTACK depend on who attacks, see damageTypeFor.
        // PROJECTILE, MELTING and SUICIDE have no damage type that fits without more information.
    }

    private final Class<?>             sourceClass;
    private final Class<?>             typeClass;
    private final Method               builder;
    private final Method               withCausingEntity;
    private final Method               withDirectEntity;
    private final Method               build;
    private final Method               getType;
    private final Method               getDirect;
    private final Method               getCausing;
    private final Method               getKey;
    private final Map<String, Object>  types         = new ConcurrentHashMap<>();
    private final Map<Class<?>, Method> damageMethods = new ConcurrentHashMap<>();
    private final Map<Class<?>, Method> sourceGetters = new ConcurrentHashMap<>();
    private final DamageSourceAdapter  legacy        = new LegacyDamageSourceAdapter();

    /**
     * @throws ReflectiveOperationException if the server has no damage sources
     */
    ModernDamageSourceAdapter(@NotNull ClassLoader loader) throws ReflectiveOperationException {
        sourceClass = Class.forName("org.bukkit.damage.DamageSource", false, loader);
        typeClass = Class.forName("org.bukkit.damage.DamageType", false, loader);
        Class<?> builderClass = Class.forName("org.bukkit.damage.DamageSource$Builder", false, loader);
        builder = sourceClass.getMethod("builder", typeClass);
        withCausingEntity = builderClass.getMethod("withCausingEntity", Entity.class);
        withDirectEntity = builderClass.getMethod("withDirectEntity", Entity.class);
        build = builderClass.getMethod("build");
        getType = sourceClass.getMethod("getDamageType");
        getDirect = sourceClass.getMethod("getDirectEntity");
        getCausing = sourceClass.getMethod("getCausingEntity");
        getKey = typeClass.getMethod("getKey");
    }

    private static void both(String cause, String... types) {
        TYPES_WITHOUT_ENTITY.put(cause, types);
        TYPES_WITH_ENTITY.put(cause, types);
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public boolean damage(@NotNull Damageable target,
                          double amount,
                          @NotNull DamageCause cause,
                          @Nullable Entity source) {
        Object type   = damageTypeFor(cause, source);
        Method damage = damageMethod(target.getClass());
        if (type == null || damage == null) return false;

        Object damageSource;
        try {
            Object building = builder.invoke(null, type);
            if (source != null) {
                building = withCausingEntity.invoke(building, source);
                building = withDirectEntity.invoke(building, source);
            }
            damageSource = build.invoke(building);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }

        try {
            damage.invoke(target, amount, damageSource);
        } catch (IllegalAccessException e) {
            return false;
        } catch (InvocationTargetException e) {
            // The damage call itself ran and something in it threw, such as a listener of the damage event.
            // That is the caller's to see, exactly as with a direct call.
            Throwable thrown = e.getCause();
            if (thrown instanceof RuntimeException) throw (RuntimeException) thrown;
            if (thrown instanceof Error) throw (Error) thrown;
            throw new IllegalStateException(thrown);
        }
        return true;
    }

    @Override
    @Nullable
    public String getDamageType(@NotNull EntityDamageEvent event) {
        Object source = sourceOf(event);
        if (source == null) return null;
        try {
            Object type = getType.invoke(source);
            Object key  = type == null ? null : getKey.invoke(type);
            return key == null ? null : key.toString();
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    @Override
    @Nullable
    public Entity getDirectEntity(@NotNull EntityDamageEvent event) {
        Object source = sourceOf(event);
        if (source == null) return legacy.getDirectEntity(event);
        return entity(getDirect, source);
    }

    @Override
    @Nullable
    public Entity getCausingEntity(@NotNull EntityDamageEvent event) {
        Object source = sourceOf(event);
        if (source == null) return legacy.getCausingEntity(event);
        return entity(getCausing, source);
    }

    @Nullable
    private Entity entity(Method getter, Object source) {
        try {
            Object entity = getter.invoke(source);
            return entity instanceof Entity ? (Entity) entity : null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    /**
     * @return the event's damage source, or null if this event has none
     */
    @Nullable
    private Object sourceOf(EntityDamageEvent event) {
        Method getter = sourceGetters.computeIfAbsent(event.getClass(), type -> {
            try {
                return type.getMethod("getDamageSource");
            } catch (NoSuchMethodException e) {
                return NONE;
            }
        });
        if (getter == NONE) return null;
        try {
            return getter.invoke(event);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    @Nullable
    private Method damageMethod(Class<?> targetClass) {
        Method method = damageMethods.computeIfAbsent(targetClass, type -> {
            try {
                return type.getMethod("damage", double.class, sourceClass);
            } catch (NoSuchMethodException e) {
                return NONE;
            }
        });
        return method == NONE ? null : method;
    }

    @Nullable
    private Object damageTypeFor(DamageCause cause, @Nullable Entity source) {
        String name = cause.name();
        if (name.equals("ENTITY_ATTACK") || name.equals("ENTITY_SWEEP_ATTACK")) {
            if (source == null) return null;
            return type(source instanceof Player ? "PLAYER_ATTACK" : "MOB_ATTACK");
        }
        String[] candidates = (source == null ? TYPES_WITHOUT_ENTITY : TYPES_WITH_ENTITY).get(name);
        if (candidates == null) return null;
        for (String candidate : candidates) {
            Object type = type(candidate);
            if (type != null) return type;
        }
        return null;
    }

    /**
     * @return the DamageType constant of that name, or null if this server version does not have it
     */
    @Nullable
    private Object type(String constant) {
        Object type = types.computeIfAbsent(constant, name -> {
            try {
                Object value = typeClass.getField(name).get(null);
                return value == null ? NONE : value;
            } catch (ReflectiveOperationException | RuntimeException e) {
                return NONE;
            }
        });
        return type == NONE ? null : type;
    }

    /** Stands for "looked up and not there" in the caches, which cannot hold null. */
    private static final Method NONE;

    static {
        try {
            NONE = Object.class.getMethod("hashCode");
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
