package studio.magemonkey.codex.api.damage;

import org.jetbrains.annotations.NotNull;

/**
 * Gives the {@link DamageSourceAdapter} that fits the running server: one that uses Bukkit's damage sources
 * on Minecraft 1.20.4 and newer, and one that does without them on older versions.
 */
public final class DamageSources {
    private static DamageSourceAdapter adapter;

    private DamageSources() {}

    @NotNull
    public static DamageSourceAdapter get() {
        if (adapter == null) adapter = create(DamageSources.class.getClassLoader());
        return adapter;
    }

    @NotNull
    static DamageSourceAdapter create(@NotNull ClassLoader loader) {
        try {
            return new ModernDamageSourceAdapter(loader);
        } catch (ReflectiveOperationException | LinkageError e) {
            return new LegacyDamageSourceAdapter();
        }
    }
}
