package mekanism.common.recipe;

import com.google.gson.JsonArray;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

@NothingNullByDefault
public abstract class BaseRecipeProvider extends RecipeProvider {

    private final ExistingFileHelper existingFileHelper;

    protected BaseRecipeProvider(PackOutput output, ExistingFileHelper existingFileHelper, String modid) {
        super(output);
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public final void buildRecipes(Consumer<FinishedRecipe> consumer) {
        Consumer<FinishedRecipe> trackingConsumer = consumer.andThen(recipe ->
              existingFileHelper.trackGenerated(recipe.getId(), PackType.SERVER_DATA, ".json", "recipes"));
        addRecipes(trackingConsumer);
        getSubRecipeProviders().forEach(subRecipeProvider -> subRecipeProvider.addRecipes(trackingConsumer));
    }

    protected abstract void addRecipes(Consumer<FinishedRecipe> consumer);

    /**
     * Gets all the sub/offloaded recipe providers that this recipe provider has.
     *
     * @implNote This is only called once per provider so there is no need to bother caching the list that this returns
     */
    protected List<ISubRecipeProvider> getSubRecipeProviders() {
        return Collections.emptyList();
    }

    public static Ingredient createIngredient(TagKey<Item> itemTag, ItemLike... items) {
        return createIngredient(Collections.singleton(itemTag), items);
    }

    public static Ingredient createIngredient(Collection<TagKey<Item>> itemTags, ItemLike... items) {
        JsonArray ingredients = new JsonArray();
        itemTags.forEach(tag -> ingredients.add(Ingredient.of(tag).toJson()));
        Arrays.stream(items).forEach(item -> ingredients.add(Ingredient.of(item).toJson()));
        return ingredients.isEmpty() ? Ingredient.EMPTY : Ingredient.fromJson(ingredients);
    }

    @SafeVarargs
    public static Ingredient createIngredient(TagKey<Item>... tags) {
        return createIngredient(Arrays.asList(tags));
    }

    public static Ingredient difference(TagKey<Item> base, ItemLike subtracted) {
        return DefaultCustomIngredients.difference(Ingredient.of(base), Ingredient.of(subtracted));
    }
}