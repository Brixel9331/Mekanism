package mekanism.common.recipe.compat;

import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@NothingNullByDefault
public class FarmersDelightRecipeProvider extends CompatRecipeProvider {

    public FarmersDelightRecipeProvider(String modid) {
        super(modid);
    }

    @Override
    protected void registerRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        addPrecisionSawmillRecipes(consumer, basePath + "sawing/");
    }

    private void addPrecisionSawmillRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Beef
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.BEEF),
                    new ItemStack(item("minced_beef"), 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "minced_beef"));
        //Pork
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.PORKCHOP),
                    new ItemStack(item("bacon"), 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "raw_bacon"));
        //Mutton
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.MUTTON),
                    new ItemStack(item("mutton_chops"), 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "raw_mutton_chops"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.COOKED_MUTTON),
                    new ItemStack(item("cooked_mutton_chops"), 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "mutton_chops"));
        //Ham
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(item("ham")),
                    new ItemStack(Items.PORKCHOP, 2),
                    new ItemStack(Items.BONE),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "ham_processing"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(item("smoked_ham")),
                    new ItemStack(Items.COOKED_PORKCHOP, 2),
                    new ItemStack(Items.BONE),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smoked_ham_processing"));
        //Chicken
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.CHICKEN),
                    new ItemStack(item("chicken_cuts"), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "raw_chicken_cuts"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.COOKED_CHICKEN),
                    new ItemStack(item("cooked_chicken_cuts"), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chicken_cuts"));
        //Salmon
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.SALMON),
                    new ItemStack(item("salmon_slice"), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "raw_salmon_slice"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.COOKED_SALMON),
                    new ItemStack(item("cooked_salmon_slice"), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "salmon_slice"));
        //Cod
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.COD),
                    new ItemStack(item("cod_slice"), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "raw_cod_slice"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.COOKED_COD),
                    new ItemStack(item("cooked_cod_slice"), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "cod_slice"));
    }
}