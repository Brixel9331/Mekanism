package mekanism.common.recipe.compat;

import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.recipe.ISubRecipeProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

//TODO: Decide if we should have compat recipes go into their own data packs
@NothingNullByDefault
public abstract class CompatRecipeProvider implements ISubRecipeProvider {

    protected final String modid;
    protected final ConditionJsonProvider modLoaded;
    protected final ConditionJsonProvider allModsLoaded;

    protected CompatRecipeProvider(String modid, String... secondaryMods) {
        this.modid = modid;
        this.modLoaded = DefaultResourceConditions.allModsLoaded(modid);
        if (secondaryMods.length == 0) {
            allModsLoaded = modLoaded;
        } else {
            ConditionJsonProvider combined = modLoaded;
            for (String secondaryMod : secondaryMods) {
                combined = DefaultResourceConditions.and(combined, DefaultResourceConditions.allModsLoaded(secondaryMod));
            }
            allModsLoaded = combined;
        }
    }

    @Override
    public final void addRecipes(Consumer<FinishedRecipe> consumer) {
        registerRecipes(consumer, getBasePath());
    }

    protected abstract void registerRecipes(Consumer<FinishedRecipe> consumer, String basePath);

    protected String getBasePath() {
        return "compat/" + modid + "/";
    }

    protected ResourceLocation rl(String path) {
        return new ResourceLocation(modid, path);
    }

    protected TagKey<Item> tag(String path) {
        return ItemTags.create(rl(path));
    }
}
