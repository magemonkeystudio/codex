package studio.magemonkey.codex.legacy.utils;

import lombok.Getter;
import org.bukkit.Bukkit;

public class ReflectionUtil {
    private static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();
    private static final String VERSION;
    static {
        String[] pack = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",");
        if (pack.length == 4) {
            VERSION = pack[3];
        } else {
            String version = Bukkit.getVersion();
            version = version.substring(0, version.indexOf('-')).replace('.', '_');
            int i = version.lastIndexOf('_');
            VERSION = 'v'+version.substring(0, i)+"_R"+(Integer.parseInt(version.substring(i+1))-1);
        }
    }
    public static final  int     MINOR_VERSION = Integer.parseInt(VERSION.split("_")[1]);
    @Getter
    private static final boolean newVersion    = Integer.parseInt(VERSION.split("_")[1]) >= 17;

    public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        try {
            return Class.forName("net.minecraft.server." + name);
        } catch (ClassNotFoundException e) {
            return Class.forName("net.minecraft.server." + VERSION + "." + name);
        }
    }

    public static Class<?> getBukkitClass(String name) throws ClassNotFoundException {
        return Class.forName(CRAFTBUKKIT_PACKAGE + "." + name);
    }

    public static boolean isVersionGreater(int version) {
        return MINOR_VERSION >= version;
    }

}
