package com.kjmaster.ethology.api;

import net.minecraft.world.entity.ai.goal.Goal;

@FunctionalInterface
public interface IGoalParser<T extends Goal> {
    void parse(T goal, MobScopedInfo info);
}