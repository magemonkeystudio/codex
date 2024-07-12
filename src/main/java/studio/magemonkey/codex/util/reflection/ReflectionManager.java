package studio.magemonkey.codex.util.reflection;

import org.bukkit.Bukkit;
import studio.magemonkey.codex.core.Version;

public class ReflectionManager {

    public static final String         VERSION       =
            Bukkit.getServer().getClass().getPackage().getName().contains("mockbukkit") ? "testing_19"
                    : (Integer.parseInt(Bukkit.getServer().getBukkitVersion().split("\\.")[1]) < 20 ? Bukkit.getServer()
                            .getClass()
                            .getPackage()
                            .getName()
                            .replace(".", ",")
                            .split(",")[3] : Bukkit.getServer().getBukkitVersion().split("-")[0].replace(".", "_"));
    public static final int            MINOR_VERSION = Integer.parseInt(VERSION.split("_")[1]);
    private static      ReflectionUtil reflection;

    public static ReflectionUtil getReflectionUtil() {
        if (reflection != null) return reflection;

        switch (Version.CURRENT) {
            case V1_17_R1, V1_18_R1, V1_18_R2, V1_19_R1, V1_19_R2, V1_19_R3 -> reflection = new Reflection_1_17();
            case V1_20_R1, V1_20_R2, V1_20_R3, V1_20_R4, V1_21_R1 -> reflection = new Reflection_1_20();
            default -> reflection = new DefaultReflectionUtil();
        }

        return reflection;
    }

}
