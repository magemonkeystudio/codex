package studio.magemonkey.codex.util;

import lombok.SneakyThrows;
import org.bukkit.Keyed;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class AttributeUT {
    private static Method valueOf;

    /**
     * Resolve an attribute by name. This method is deprecated because
     * the underlying method is also deprecated. This is a temporary fix
     * until we determine where we want to go.
     *
     * @param name The name of the attribute
     * @return The attribute
     * @deprecated This method is a hack that is necessary for backward compatibility with &lt;1.21.3 versions. It's
     * annoying and should be removed when possible, though this will probably require a hard break at 1.21.3+.
     */
    @SneakyThrows
    @Deprecated
    public static Keyed resolve(String name) {
        if (valueOf == null) {
            valueOf = Objects.requireNonNull(Reflex.getClass("org.bukkit.attribute.Attribute")).getDeclaredMethod("valueOf", String.class);
        }

        Keyed attribute = null;
        try {
            attribute = (Keyed) valueOf.invoke(null, name);
        } catch (InvocationTargetException ignored) {
        }

        if (attribute == null) {
            try {
                attribute = (Keyed) valueOf.invoke(null, "GENERIC_" + name);
            } catch (InvocationTargetException ignored) {
            }
        }
        if (attribute == null) {
            try {
                attribute = (Keyed) valueOf.invoke(null, "PLAYER_" + name);
            } catch (InvocationTargetException ignored) {
            }
        }
        return attribute;
    }

}
