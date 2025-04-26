package studio.magemonkey.codex;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This will slowly become the central point of reference for all things Codex instead of CodexEngine
 */
public class Codex {
    @Setter
    public static JavaPlugin plugin;

    @Getter
    @Setter
    public static BukkitAudiences audience;

    public static JavaPlugin getPlugin() {
        if (plugin == null) {
            throw new IllegalStateException("Codex has not been initialized");
        }

        return plugin;
    }

    public static void info(String message) {
        getPlugin().getLogger().info(message);
    }

    public static void warn(String message) {
        getPlugin().getLogger().warning(message);
    }

    public static void error(String message) {
        getPlugin().getLogger().severe(message);
    }

    public static void trace(String message) {
        getPlugin().getLogger().fine(message);
    }
}