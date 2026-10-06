package mekanism.api.datagen;

import com.google.gson.JsonObject;
import java.util.concurrent.atomic.AtomicReference;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.SharedConstants;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecipeConditionTest {

    private static final ResourceLocation GATE = new ResourceLocation("test", "recipe_gate");

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        ResourceConditions.register(GATE, json -> json.get("enabled").getAsBoolean());
    }

    @Test
    void generatedConditionsControlFabricResourceLoading() {
        for (boolean enabled : new boolean[]{false, true}) {
            ItemStackToItemStackRecipeBuilder builder = recipe().addCondition(new ConditionJsonProvider() {
                @Override
                public ResourceLocation getConditionId() {
                    return GATE;
                }

                @Override
                public void writeParameters(JsonObject object) {
                    object.addProperty("enabled", enabled);
                }
            });
            AtomicReference<FinishedRecipe> result = new AtomicReference<>();
            builder.build(result::set, new ResourceLocation("test", "conditional"));
            JsonObject json = result.get().serializeRecipe();
            assertEquals(enabled, ResourceConditions.objectMatchesConditions(json));
            assertTrue(json.has(ResourceConditions.CONDITIONS_KEY));
            assertFalse(json.has("conditions"));
            assertEquals("mekanism:crushing", json.get("type").getAsString());
            assertEquals("minecraft:diamond", json.getAsJsonObject("output").get("item").getAsString());
        }
    }

    @Test
    void unconditionalRecipesRemainLoadable() {
        AtomicReference<FinishedRecipe> result = new AtomicReference<>();
        recipe().build(result::set);
        JsonObject json = result.get().serializeRecipe();
        assertTrue(ResourceConditions.objectMatchesConditions(json));
        assertFalse(json.has(ResourceConditions.CONDITIONS_KEY));
        assertEquals(new ResourceLocation("minecraft", "diamond"), result.get().getId());
    }

    private static ItemStackToItemStackRecipeBuilder recipe() {
        return ItemStackToItemStackRecipeBuilder.crushing(ItemStackIngredientCreator.INSTANCE.from(Items.DIAMOND_ORE), new ItemStack(Items.DIAMOND));
    }
}
