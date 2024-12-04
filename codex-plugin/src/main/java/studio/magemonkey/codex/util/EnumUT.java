package studio.magemonkey.codex.util;

import lombok.SneakyThrows;

import java.lang.reflect.Method;

public class EnumUT {
    @SneakyThrows
    public static String getName(Object biome) {
        Method name = null;
        try {
            name = biome.getClass().getDeclaredMethod("name");
        } catch (NoSuchMethodException ignored) {
        }
        // Attempt super class if not found
        if (name == null) {
            try {
                name = biome.getClass().getSuperclass().getDeclaredMethod("name");
            } catch (NoSuchMethodException ignored) {
            }
        }

        if (name == null) {
            return null;
        }
        return name.invoke(biome).toString();
    }
}
