package studio.magemonkey.codex.api;

import lombok.Setter;
import org.bukkit.Bukkit;
import studio.magemonkey.codex.api.exception.UnsupportedVersionException;
import studio.magemonkey.codex.core.Version;

public class VersionManager {
    @Setter
    protected static NMS       nms;
    @Setter
    private static   ArmorUtil armorUtil;

    public static void setup() {
        if (Version.CURRENT == Version.TEST) return;
        String version = Bukkit.getServer().getBukkitVersion().split("-")[0];

        try {
            String packageName = getPackageFromVersion(version);
            VersionManager.setNms((NMS) Class.forName("studio.magemonkey.codex.nms." + packageName + ".NMSImpl").getConstructor().newInstance());
            try {
                VersionManager.setArmorUtil((ArmorUtil) Class.forName("studio.magemonkey.codex.nms." + packageName + ".ArmorUtilImpl").getConstructor().newInstance());
            } catch (ClassNotFoundException ignored) {
                // ArmorUtil is not implemented for this version -- (pre 1.19.4)
                VersionManager.setArmorUtil(new ArmorUtil() {
                });
            }
        } catch (Exception e) {
            throw new UnsupportedVersionException("Could not find NMS implementation for version " + version, e);
        }
    }

    private static String getPackageFromVersion(String version) {
        return switch (version) {
            case "1.18", "1.18.1", "1.19", "1.19.1", "1.19.2", "1.19.3", "1.20", "1.20.1" ->
                    throw new UnsupportedVersionException("Version " + version + " is not supported. Please upgrade to the latest minor version of your current major version.");
            case "1.16.5" -> "v1_16_5";
            case "1.17", "1.17.1" -> "v1_17";
            case "1.18.2" -> "v1_18_2";
            case "1.19.4" -> "v1_19_4";
            case "1.20.2" -> "v1_20_2";
            case "1.20.3", "1.20.4" -> "v1_20_4";
            case "1.20.5", "1.20.6" -> "v1_20_6";
            case "1.21", "1.21.1" -> "v1_21_1";
            case "1.21.2", "1.21.3" -> "v1_21_2";
            case "1.21.4" -> "v1_21_4";
            default -> throw new UnsupportedVersionException("Unknown version " + version);
        };
    }

    public static NMS getNms() {
        if (nms == null) {
            throw new RuntimeException("NMS has not been set yet! Something is wrong.");
        }

        return nms;
    }

    public static ArmorUtil getArmorUtil() {
        if (armorUtil == null) {
            throw new RuntimeException("ArmorUtil has not been set yet! Something is wrong.");
        }

        return armorUtil;
    }
}
