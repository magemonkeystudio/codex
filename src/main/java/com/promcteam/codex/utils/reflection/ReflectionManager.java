package com.promcteam.codex.utils.reflection;

import com.promcteam.codex.core.Version;
import org.bukkit.Bukkit;

public class ReflectionManager {

    public static final String         VERSION       = Bukkit.getServer().getClass().getPackage().getName().contains("mockbukkit")
            ? "testing_19"
            : Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    public static final int            MINOR_VERSION = Integer.parseInt(VERSION.split("_")[1]);
    private static      ReflectionUtil reflection;

    public static ReflectionUtil getReflectionUtil() {
        if (reflection != null) return reflection;

        switch (Version.CURRENT) {
            case V1_17_R1, V1_18_R1, V1_18_R2, V1_19_R1, V1_19_R2, V1_19_R3 -> reflection = new Reflection_1_17();
            case V1_20_R1, V1_20_R2, V1_20_R3 -> reflection = new Reflection_1_20();
            default -> reflection = new DefaultReflectionUtil();
        }

        return reflection;
    }

}
