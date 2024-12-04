package studio.magemonkey.codex.util;

import lombok.SneakyThrows;
import org.bukkit.attribute.Attribute;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AttributeUT {
    private static Method valueOf;

    /**
     * Resolve an attribute by name. This method is deprecated because
     * the underlying method is also deprecated. This is a temporary fix
     * until we determine where we want to go.
     * @param name The name of the attribute
     * @return The attribute
     * @deprecated
     */
    @SneakyThrows
    @Deprecated
    public static Attribute resolve(String name) {
        if (valueOf == null) {
            valueOf = Attribute.class.getDeclaredMethod("valueOf", String.class);
        }

        Attribute attribute = null;
        try {
            attribute = (Attribute) valueOf.invoke(null, name);
        } catch (InvocationTargetException ignored) {
        }

        if (attribute == null) {
            try {
                attribute = (Attribute) valueOf.invoke(null, "GENERIC_" + name);
            } catch (InvocationTargetException ignored) {
            }
        }
        if (attribute == null) {
            try {
                attribute = (Attribute) valueOf.invoke(null, "PLAYER_" + name);
            } catch (InvocationTargetException ignored) {
            }
        }
        return attribute;
    }

}
