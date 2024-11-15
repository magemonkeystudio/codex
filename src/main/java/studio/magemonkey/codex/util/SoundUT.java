package studio.magemonkey.codex.util;

import org.bukkit.Keyed;
import org.bukkit.Sound;
import studio.magemonkey.codex.CodexEngine;

public class SoundUT {
    public static Keyed getSound(String name) {
        try {
            return Sound.valueOf(name);
        } catch (IncompatibleClassChangeError e) {
            try {
                return (Keyed) Enum.valueOf((Class<Enum>) Class.forName("org.bukkit.Sound"), name);
            } catch (ClassNotFoundException | ClassCastException e1) {
                CodexEngine.get().error("Sound not found: " + name + " " + e1.getMessage());
            }
        }
        return null;
    }
}
