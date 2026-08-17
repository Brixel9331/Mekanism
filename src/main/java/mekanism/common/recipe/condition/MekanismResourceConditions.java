package mekanism.common.recipe.condition;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

public final class MekanismResourceConditions {

    private static boolean registered;

    private MekanismResourceConditions() {
    }

    public static synchronized void register() {
        if (!registered) {
            ResourceConditions.register(ConditionExistsCondition.NAME, ConditionExistsCondition::test);
            ResourceConditions.register(ModVersionLoadedCondition.NAME, ModVersionLoadedCondition::test);
            registered = true;
        }
    }
}
