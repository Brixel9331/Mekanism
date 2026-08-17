package mekanism.common.recipe.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import mekanism.api.MekanismAPI;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public record ConditionExistsCondition(@Nullable ConditionJsonProvider condition) implements ConditionJsonProvider {

    static final ResourceLocation NAME = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "condition_exists");

    @Override
    public ResourceLocation getConditionId() {
        return NAME;
    }

    @Override
    public void writeParameters(JsonObject json) {
        if (condition != null) {
            json.add("value", condition.toJson());
        }
    }

    static boolean test(JsonObject json) {
        JsonObject condition = GsonHelper.getAsJsonObject(json, "value", null);
        if (condition == null) {
            return false;
        }
        try {
            return ResourceConditions.conditionMatches(condition);
        } catch (JsonParseException e) {
            //If we can't deserialize the internal condition it probably is from a mod that doesn't exist
            return false;
        }
    }

    @Override
    public String toString() {
        return "condition_exists(" + condition + ")";
    }
}
