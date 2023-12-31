package mc.promcteam.engine.core;

import mc.promcteam.engine.utils.CollectionsUT;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public enum Version {

    // !!! KEEP THE VERSIONS LIST IN A ORDER FROM LOWER TO HIGHER !!!
    TEST,
    V1_13_R2,
    V1_14_R1,
    V1_15_R1,
    V1_16_R2,
    V1_16_R3,
    V1_17_R1,
    V1_18_R1,
    V1_18_R2,
    V1_19_R1,
    V1_19_R2,
    V1_19_R3,
    V1_20_R1,
    V1_20_R2,
    V1_20_R3;

    public static final Version CURRENT;

    static {
        String[] split      = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        String   versionRaw = split[split.length - 1];
        if (versionRaw.equals("mockbukkit"))
            CURRENT = Version.TEST;
        else
            CURRENT = CollectionsUT.getEnum(versionRaw, Version.class);
    }

    public boolean isLower(@NotNull Version version) {
        return this.ordinal() < version.ordinal();
    }

    public boolean isHigher(@NotNull Version version) {
        return this.ordinal() > version.ordinal();
    }

    public boolean isAtLeast(@NotNull Version version) {
        return this.ordinal() >= version.ordinal();
    }

    public boolean isCurrent() {
        return this == Version.CURRENT;
    }
}
