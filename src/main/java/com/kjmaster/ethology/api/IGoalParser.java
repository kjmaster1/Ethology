package com.kjmaster.ethology.api;

import net.minecraft.world.entity.ai.goal.Goal;
import java.util.function.Consumer;

@FunctionalInterface
public interface IGoalParser<T extends Goal> {
    void parse(T goal, Consumer<MobTrait> traitConsumer);
}