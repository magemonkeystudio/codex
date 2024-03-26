package com.promcteam.codex.util;

import com.promcteam.codex.CodexEngine;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Reflex {

    public static final String VERSION =
            !Bukkit.getServer().getClass().getPackage().getName().contains("mockbukkit")
                    ? Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]
                    : "";

    @Setter
    private static CodexEngine engine;


    @Nullable
    public static Class<?> getClass(@NotNull String path, @NotNull String name) {
        return getClass(path + "." + name);
    }

    @Nullable
    public static Class<?> getInnerClass(@NotNull String path, @NotNull String name) {
        return getClass(path + "$" + name);
    }

    @Nullable
    public static Class<?> getClass(@NotNull String path) {
        try {
            return Class.forName(path);
        } catch (ClassNotFoundException e) {
            engine.error("[Reflex] Class not found: " + path);
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Constructor<?> getConstructor(@NotNull Class<?> clazz, Class<?>... types) {
        try {
            Constructor<?> con = clazz.getDeclaredConstructor(types);
            con.setAccessible(true);
            return con;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static Object invokeConstructor(@NotNull Constructor<?> con, Object... obj) {
        try {
            return con.newInstance(obj);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return obj;
    }


    public static String getNMSPackage() {
        return "net.minecraft.server." + VERSION;
    }

    @Nullable
    public static Class<?> getNMSClass(@NotNull String name) {
        return getClass("net.minecraft.server." + VERSION,
                name);
    }

    public static String getCraftPackage() {
        return "org.bukkit.craftbukkit." + VERSION;
    }

    public static Class<?> getCraftClass(String craftClassString) {
        return getClass("org.bukkit.craftbukkit." + VERSION, craftClassString);
    }

    @NotNull
    public static List<Field> getFields(@NotNull Class<?> type) {
        List<Field> result = new ArrayList<>();

        Class<?> clazz = type;
        while (clazz != null && clazz != Object.class) {
            if (!result.isEmpty()) {
                result.addAll(0, Arrays.asList(clazz.getDeclaredFields()));
            } else {
                Collections.addAll(result, clazz.getDeclaredFields());
            }
            clazz = clazz.getSuperclass();
        }

        return result;
    }

    @Nullable
    public static Field getField(@NotNull Class<?> clazz, @NotNull String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null) {
                return null;
            }
            return getField(superClass, fieldName);
        }
    }

    @Nullable
    public static Object getFieldValue(@NotNull Object from, @NotNull String fieldName) {
        try {
            Class<?> clazz = from instanceof Class<?> ? (Class<?>) from : from.getClass();
            Field    field = getField(clazz, fieldName);
            if (field == null) return null;

            field.setAccessible(true);
            return field.get(from);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean setFieldValue(@NotNull Object of, @NotNull String fieldName, @Nullable Object value) {
        try {
            boolean  isStatic = of instanceof Class;
            Class<?> clazz    = isStatic ? (Class<?>) of : of.getClass();

            Field field = getField(clazz, fieldName);
            if (field == null) return false;

            field.setAccessible(true);
            field.set(isStatic ? null : of, value);
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Tries to get a method from the object
     *
     * @param o          object reference
     * @param methodName name of the field to retrieve the value from
     * @return the value of the field or null if not found
     */
    public static Method getMethod(Object o, String methodName, Class<?>... params) {
        try {
            Method method;
            try {
                method = o.getClass().getMethod(methodName, params);
            } catch (NoSuchMethodException | SecurityException e) {
                method = o.getClass().getDeclaredMethod(methodName, params);
            }
            if (!method.isAccessible()) method.setAccessible(true);
            return method;
        } catch (Exception ex) { /* Do nothing */ }
        return null;
    }

    @Nullable
    public static Method getMethod(@NotNull Class<?> clazz, @NotNull String fieldName, @NotNull Class<?>... o) {
        try {
            return clazz.getDeclaredMethod(fieldName, o);
        } catch (NoSuchMethodException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null) {
                return null;
            } else {
                return getMethod(superClass, fieldName);
            }
        }
    }

    @Nullable
    public static Object invokeMethod(@NotNull Method m, @Nullable Object by, @Nullable Object... param) {
        m.setAccessible(true);
        try {
            return m.invoke(by, param);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T extends Enum> T getEnum(Class<?> clazz, String enumName) {
        return (T) Enum.valueOf((Class<T>) clazz, enumName);
    }

    /**
     * Tries to set a value for the object
     *
     * @param o         object reference
     * @param fieldName name of the field to set
     * @param value     value to set
     */
    public static void setValue(Object o, String fieldName, Object value) {
        try {
            Field field = o.getClass().getDeclaredField(fieldName);
            if (!field.isAccessible()) field.setAccessible(true);
            field.set(o, value);
        } catch (Exception ex) { /* Do Nothing */ }
    }

    /**
     * Tries to get a value from the object
     *
     * @param o         object reference
     * @param fieldName name of the field to retrieve the value from
     * @return the value of the field or null if not found
     */
    public static Object getValue(Object o, String fieldName) {
        try {
            Field field = o.getClass().getDeclaredField(fieldName);
            if (!field.isAccessible()) field.setAccessible(true);
            return field.get(o);
        } catch (Exception ex) { /* Do nothing */ }
        return null;
    }

    /**
     * Gets an instance of the class
     *
     * @param c    class to get an instance of
     * @param args constructor arguments
     * @return instance of the class or null if unable to create the object
     */
    public static Object getInstance(Class<?> c, Object... args) {
        if (c == null) return null;
        try {
            for (Constructor<?> constructor : c.getDeclaredConstructors())
                if (constructor.getGenericParameterTypes().length == args.length)
                    return constructor.newInstance(args);
        } catch (Exception ex) { /* */ }
        return null;
    }
}
