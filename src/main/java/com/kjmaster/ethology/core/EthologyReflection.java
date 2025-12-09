package com.kjmaster.ethology.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for safe, deep reflection.
 * Used to heuristically inspect unknown AI Goal objects to determine their targets or triggers.
 */
public class EthologyReflection {

    // Cache: TargetClass -> (FieldType -> List<Field>)
    // We cache the actual Field objects so we only pay the lookup and setAccessible cost once.
    private static final Map<Class<?>, Map<Class<?>, List<Field>>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * Recursively searches the object's class hierarchy (up to Object.class)
     * for the *first* field that matches the specified type and returns its value.
     *
     * @param target      The object instance to inspect.
     * @param typeToFind  The class type of the field to look for (e.g., Ingredient.class).
     * @param <T>         The expected return type.
     * @return An Optional containing the field value, or Optional.empty() if not found/inaccessible.
     */
    public static <T> Optional<T> getFirstFieldOfType(Object target, Class<T> typeToFind) {
        if (target == null) return Optional.empty();

        List<Field> fields = getCachedFields(target.getClass(), typeToFind);

        for (Field field : fields) {
            try {
                // We assume setAccessible(true) was already done during caching
                Object value = field.get(target);
                if (typeToFind.isInstance(value)) {
                    return Optional.of(typeToFind.cast(value));
                }
            } catch (Throwable ignored) {
                // Swallow exceptions during field access
            }
        }
        return Optional.empty();
    }

    /**
     * Recursively searches the object's class hierarchy for *all* fields matching the specified type.
     *
     * @param target      The object instance to inspect.
     * @param typeToFind  The class type of the field to look for.
     * @param <T>         The expected return type.
     * @return A List of all found non-null values matching the type.
     */
    public static <T> List<T> getAllFieldsOfType(Object target, Class<T> typeToFind) {
        if (target == null) return Collections.emptyList();

        List<Field> fields = getCachedFields(target.getClass(), typeToFind);
        List<T> results = new ArrayList<>(fields.size());

        for (Field field : fields) {
            try {
                Object value = field.get(target);
                if (typeToFind.isInstance(value)) {
                    results.add(typeToFind.cast(value));
                }
            } catch (Throwable ignored) {
                // Swallow exceptions during field access
            }
        }
        return results;
    }

    /**
     * Retrieves the list of matching fields from the cache, or scans the class hierarchy to populate it.
     */
    private static List<Field> getCachedFields(Class<?> targetClass, Class<?> typeToFind) {
        return FIELD_CACHE
                .computeIfAbsent(targetClass, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(typeToFind, k -> scanForFields(targetClass, typeToFind));
    }

    /**
     * Performs the expensive reflection scan.
     */
    private static List<Field> scanForFields(Class<?> targetClass, Class<?> typeToFind) {
        List<Field> foundFields = new ArrayList<>();
        Class<?> currentClass = targetClass;

        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                // Check if the field's type is compatible with the type we are looking for
                if (typeToFind.isAssignableFrom(field.getType())) {
                    try {
                        // Attempt to break encapsulation ONCE and cache the result
                        field.setAccessible(true);
                        foundFields.add(field);
                    } catch (Throwable ignored) {
                        // Swallow all exceptions (IllegalAccessException, InaccessibleObjectException, etc.)
                        // Fields that cannot be accessed are simply excluded from the cache.
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return foundFields;
    }

    /**
     * Clears the reflection cache.
     * Useful if classes are reloaded (rare) or to free memory on logout.
     */
    public static void clearCache() {
        FIELD_CACHE.clear();
    }
}