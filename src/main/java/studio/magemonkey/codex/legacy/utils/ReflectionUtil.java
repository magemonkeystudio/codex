package studio.magemonkey.codex.legacy.utils;

import lombok.Getter;
import org.bukkit.Bukkit;

public class ReflectionUtil {

    private static final String  VERSION       =
            Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    public static final  int     MINOR_VERSION = Integer.parseInt(VERSION.split("_")[1]);
    private static final String  version       = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    @Getter
    private static final boolean newVersion    = Integer.parseInt(version.split("_")[1]) >= 17;

    public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + version + "." + name);
    }

    public static Class<?> getBukkitClass(String name) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
    }

    public static boolean isVersionGreater(int version) {
        return MINOR_VERSION >= version;
    }

}
