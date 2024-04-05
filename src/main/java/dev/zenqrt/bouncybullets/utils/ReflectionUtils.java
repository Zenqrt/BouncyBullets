package dev.zenqrt.bouncybullets.utils;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public final class ReflectionUtils {

    public static void setFieldUnsafe(Field field, Object object, Object value) {
        Unsafe unsafe = getUnsafe();
        long offset = unsafe.objectFieldOffset(field);
        unsafe.putObject(object, offset, value);
    }

    public static <T> void setFieldUnsafe(Class<T> clazz, String fieldName, Object object, Object value) {
        Field field = getAccessibleField(clazz, fieldName);
        setFieldUnsafe(field, object, value);
    }

    private static Unsafe getUnsafe() {
        Field unsafeField;
        try {
            unsafeField = getAccessibleField(Unsafe.class, "theUnsafe");
            return (Unsafe) unsafeField.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to get Unsafe instance", e);
        }
    }

    public static <T> void setDeclaredField(Class<T> clazz, Object object, String fieldName, Object value) {
        try {
            Field field = getAccessibleField(clazz, fieldName);
            field.set(object, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> T getDeclaredField(Class<?> clazz, Object object, String fieldName) {
        try {
            Field field = getAccessibleField(clazz, fieldName);
            return (T) field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> T getStaticDeclaredField(Class<?> clazz, String fieldName) {
        try {
            Field field = getAccessibleField(clazz, fieldName);
            return (T) field.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static <T> Field getAccessibleField(Class<T> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
