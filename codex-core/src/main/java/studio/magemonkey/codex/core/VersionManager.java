package studio.magemonkey.codex.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import studio.magemonkey.codex.api.NMS;
import studio.magemonkey.codex.api.NMSProvider;

public class VersionManager {
    public VersionManager(JavaPlugin plugin) {
        String version = Bukkit.getServer().getBukkitVersion().split("-")[0];

        try {
            String packageName = getPackageFromVersion(version);
            NMSProvider.setNms((NMS) Class.forName("studio.magemonkey.codex.nms." + packageName + ".NMSImpl").getConstructor().newInstance());
        } catch (Exception e) {
            throw new RuntimeException("Could not find NMS implementation for version " + version, e);
        }

        plugin.getLogger().info("Using NMS implementation for version " + NMSProvider.getNms().getVersion());
    }

    private static String getPackageFromVersion(String version) {
        return switch (version) {
            case "1.18", "1.18.1", "1.19", "1.19.1", "1.19.2", "1.19.3", "1.20", "1.20.1" ->
                    throw new RuntimeException("Version " + version + " is not supported. Please upgrade to the latest minor version of your current major version.");
            case "1.16.5" -> "v1_16_5";
            case "1.17", "1.17.1" -> "v1_17";
            case "1.18.2" -> "v1_18_2";
            case "1.19.4" -> "v1_19_4";
            case "1.20.2" -> "v1_20_2";
            case "1.20.3", "1.20.4" -> "v1_20_4";
            case "1.20.5", "1.20.6" -> "v1_20_6";
            case "1.21", "1.21.1" -> "v1_21_1";
            case "1.21.2", "1.21.3", "1.21.4" -> "v1_21_2";
            default -> throw new RuntimeException("Unknown version " + version);
        };
    }
}
