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
    V1_21_R1,
    V1_21_R2,
    V1_21_R3,
    V1_21_R4,
    V1_21_R5,
    V1_21_R6,
    V1_21_R7,
    V26_R1;

    public static final Version CURRENT;

    static {
        String[] split      = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        String   versionRaw = split[split.length - 1];
        if (versionRaw.equals("mockbukkit")) CURRENT = Version.TEST;
        else if (versionRaw.equals("craftbukkit")) {
            // New as of Paper 1.20.6, no more version specific package
            // see: https://forums.papermc.io/threads/important-dev-psa-future-removal-of-cb-package-relocation.1106/

            // This get version method has been around since 2011
            String version = Bukkit.getServer().getBukkitVersion();

            // Starting in 26.x, versions look like this: 26.2.build.42-alpha
            // So we need to clean up the version just to everything prior to `build` to determine the version
            if (version.contains("build")) {
                version = version.substring(0, version.indexOf("build") - 1);
            }

            CURRENT = switch (version) {
                case "1.20.6-R0.1-SNAPSHOT" -> Version.V1_20_R4;
                case "1.21-R0.1-SNAPSHOT", "1.21.1-R0.1-SNAPSHOT" -> Version.V1_21_R1;
                case "1.21.2-R0.1-SNAPSHOT", "1.21.3-R0.1-SNAPSHOT" -> Version.V1_21_R2;
                case "1.21.4-R0.1-SNAPSHOT" -> Version.V1_21_R3;
                case "1.21.5-R0.1-SNAPSHOT" -> Version.V1_21_R4;
                case "1.21.6-R0.1-SNAPSHOT", "1.21.7-R0.1-SNAPSHOT", "1.21.8-R0.1-SNAPSHOT" -> Version.V1_21_R5;
                case "1.21.9-R0.1-SNAPSHOT", "1.21.10-R0.1-SNAPSHOT" -> Version.V1_21_R6;
                case "1.21.11-R0.1-SNAPSHOT" -> Version.V1_21_R7;
                case "26.1.2-R0.1-SNAPSHOT", "26.1.2", "26.2-R0.1-SNAPSHOT", "26.2" -> Version.V26_R1;
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
