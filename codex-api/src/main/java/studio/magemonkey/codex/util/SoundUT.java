package studio.magemonkey.codex.util;

import org.bukkit.Keyed;
import studio.magemonkey.codex.Codex;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class SoundUT {
    @SuppressWarnings("unchecked")
    public static Keyed getSound(String name) {
        try {
            Method valueOf = Reflex.getMethod(Objects.requireNonNull(Reflex.getClass("org.bukkit.Sound")),
                    "valueOf",
                    String.class);
            return (Keyed) valueOf.invoke(null, name);
        } catch (IncompatibleClassChangeError | IllegalAccessException | InvocationTargetException e) {
            try {
                return (Keyed) Enum.valueOf((Class<Enum>) Class.forName("org.bukkit.Sound"), name);
            } catch (ClassNotFoundException | ClassCastException e1) {
                Codex.error("Sound not found: " + name + " " + e1.getMessage());
            }
        }
        return null;
    }
}
