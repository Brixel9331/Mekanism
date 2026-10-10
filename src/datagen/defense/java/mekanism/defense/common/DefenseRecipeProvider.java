package mekanism.defense.common;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.recipe.BaseRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;

@NothingNullByDefault
public class DefenseRecipeProvider extends BaseRecipeProvider {

    public DefenseRecipeProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, existingFileHelper, MekanismDefense.MODID);
    }

    @Override
    protected void addRecipes(Consumer<FinishedRecipe> consumer) {
    }
}