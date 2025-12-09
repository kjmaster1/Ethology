package com.kjmaster.ethology.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for safe, deep reflection.
 * Used to heuristically inspect unknown AI Goal objects to determine their targets or triggers.
 */
public class EthologyReflection {

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

        Class<?> currentClass = target.getClass();

        // Traverse up the class hierarchy
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                // Check if the field's type is compatible with the type we are looking for
                if (typeToFind.isAssignableFrom(field.getType())) {
                    try {
                        // Attempt to break encapsulation
                        field.setAccessible(true);
                        Object value = field.get(target);

                        // Ensure the value is valid and castable
                        if (typeToFind.isInstance(value)) {
                            return Optional.of(typeToFind.cast(value));
                        }
                    } catch (Throwable ignored) {
                        // Swallow all exceptions (IllegalAccessException, InaccessibleObjectException, etc.)
                        // to prevent mod crashes during analysis.
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
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
        List<T> results = new ArrayList<>();
        if (target == null) return results;

        Class<?> currentClass = target.getClass();

        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (typeToFind.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(target);
                        if (typeToFind.isInstance(value)) {
                            results.add(typeToFind.cast(value));
                        }
                    } catch (Throwable ignored) {
                        // Swallow exceptions
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return results;
    }
}