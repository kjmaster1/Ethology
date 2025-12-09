package com.kjmaster.ethology.api;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Optional;

/**
 * Interface for analyzing AI Memories.
 * Can handle both Archetype analysis (memory presence) and Instance analysis (current memory value).
 */
@FunctionalInterface
public interface MemoryInspector {
    /**
     * Inspects a Memory Module.
     *
     * @param type  The MemoryModuleType being inspected.
     * @param value An Optional containing the current value of the memory (if present/known).
     * For Archetype scans, this may be empty even if the mob *can* have this memory.
     * @return An Optional containing the MobTrait if recognized.
     */
    Optional<MobTrait> inspect(MemoryModuleType<?> type, Optional<?> value);
}