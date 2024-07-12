package studio.magemonkey.codex.core;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.util.CollectionsUT;

public enum Version {

    // !!! KEEP THE VERSIONS LIST IN A ORDER FROM LOWER TO HIGHER !!!
    TEST,
    V1_16_R3,
    V1_17_R1,
    V1_18_R1,
    V1_18_R2,
    V1_19_R1,
    V1_19_R2,
    V1_19_R3,
    V1_20_R1,
    V1_20_R2,
    V1_20_R3,
    V1_20_R4,
    V1_21_R1;

    public static final Version CURRENT;

    static {
        String[] split      = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        String   versionRaw = split[split.length - 1];
        if (versionRaw.equals("mockbukkit"))
            CURRENT = Version.TEST;
        else if (versionRaw.equals("craftbukkit")) {
            // New as of Paper 1.20.6, no more version specific package
            // see: https://forums.papermc.io/threads/important-dev-psa-future-removal-of-cb-package-relocation.1106/

            // This get version method has been around since 2011
            String version = Bukkit.getServer().getBukkitVersion();
            CURRENT = switch (version) {
                case "1.20.6-R0.1-SNAPSHOT" -> Version.V1_20_R4;
                case "1.21-R0.1-SNAPSHOT" -> Version.V1_21_R1;
                default -> throw new IllegalStateException("Unexpected version: " + version);
            };
        } else
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
