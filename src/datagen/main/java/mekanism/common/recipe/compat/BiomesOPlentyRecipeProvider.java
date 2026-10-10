package mekanism.common.recipe.compat;

import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.registries.MekanismPigments;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

@NothingNullByDefault
public class BiomesOPlentyRecipeProvider extends CompatRecipeProvider {

    public BiomesOPlentyRecipeProvider(String modid) {
        super(modid);
    }

    @Override
    protected void registerRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        addDyeRecipes(consumer, basePath);
        addPrecisionSawmillRecipes(consumer, basePath + "sawing/");
        addSandRecipes(consumer, basePath + "sandstone_to_sand/");
        //TODO: Bio-fuel recipes?
    }

    private void addPrecisionSawmillRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, block("dead_planks"), item("dead_boat"), item("dead_chest_boat"), block("dead_door"),
              block("dead_fence_gate"), block("dead_pressure_plate"), block("dead_trapdoor"), block("dead_hanging_sign"), "dead");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, block("fir_planks"), item("fir_boat"), item("fir_chest_boat"), block("fir_door"),
              block("fir_fence_gate"), block("fir_pressure_plate"), block("fir_trapdoor"), block("fir_hanging_sign"), "fir");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, block("hellbark_planks"), item("hellbark_boat"), item("hellbark_chest_boat"), block("hellbark_door"),
              block("hellbark_fence_gate"), block("hellbark_pressure_plate"), block("hellbark_trapdoor"), block("hellbark_hanging_sign"), "hellbark");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, block("jacaranda_planks"), item("jacaranda_boat"), item("jacaranda_chest_boat"), block("jacaranda_door"),
              block("jacaranda_fence_gate"), block("jacaranda_pressure_plate"), block("jacaranda_trapdoor"), block("jacaranda_hanging_sign"), "jacaranda");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, block("magic_planks"), item("magic_boat"), item("magic_chest_boat"), block("magic_door"),
              block("magic_fence_gate"), block("magic_pressure_plate"), block("magic_trapdoor"), block("magic_hanging_sign"), "magic");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, block("mahogany_planks"), item("mahogany_boat"), item("mahogany_chest_boat"), block("mahogany_door"),
              block("mahogany_fence_gate"), block("mahogany_pressure_plate"), block("mahogany_trapdoor"), block("mahogany_hanging_sign"), "mahogany");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, block("palm_planks"), item("palm_boat"), item("palm_chest_boat"), block("palm_door"),
              block("palm_fence_gate"), block("palm_pressure_plate"), block("palm_trapdoor"), block("palm_hanging_sign"), "palm");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, block("redwood_planks"), item("redwood_boat"), item("redwood_chest_boat"), block("redwood_door"),
              block("redwood_fence_gate"), block("redwood_pressure_plate"), block("redwood_trapdoor"), block("redwood_hanging_sign"), "redwood");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, block("umbran_planks"), item("umbran_boat"), item("umbran_chest_boat"), block("umbran_door"),
              block("umbran_fence_gate"), block("umbran_pressure_plate"), block("umbran_trapdoor"), block("umbran_hanging_sign"), "umbran");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, block("willow_planks"), item("willow_boat"), item("willow_chest_boat"), block("willow_door"),
              block("willow_fence_gate"), block("willow_pressure_plate"), block("willow_trapdoor"), block("willow_hanging_sign"), "willow");
    }

    private void addPrecisionSawmillWoodTypeRecipes(Consumer<FinishedRecipe> consumer, String basePath, Block planks, Item boat,
          Item chestBoat, Block door, Block fenceGate, Block pressurePlate, Block trapdoor,
          Block hangingSign, String name) {
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, planks, boat, chestBoat, door, fenceGate,
              tag(name + "_logs"), pressurePlate, trapdoor, hangingSign, name, modLoaded);
    }

    private void addSandRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Black Sandstone -> Sand
        addSandStoneToSandRecipe(consumer, basePath + "black", block("black_sand"), block("black_sandstone"), block("chiseled_black_sandstone"),
              block("cut_black_sandstone"), block("smooth_black_sandstone"));
        //Orange Sandstone -> Sand
        addSandStoneToSandRecipe(consumer, basePath + "orange", block("orange_sand"), block("orange_sandstone"), block("chiseled_orange_sandstone"),
              block("cut_orange_sandstone"), block("smooth_orange_sandstone"));
        //White Sandstone -> Sand
        addSandStoneToSandRecipe(consumer, basePath + "white", block("white_sand"), block("white_sandstone"), block("chiseled_white_sandstone"),
              block("cut_white_sandstone"), block("smooth_white_sandstone"));
    }

    private void addSandStoneToSandRecipe(Consumer<FinishedRecipe> consumer, String path, Block sand, Block... sandstones) {
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, path, modLoaded, sand, sandstones);
    }

    private void addDyeRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Red
        dye(consumer, basePath, Items.RED_DYE, EnumColor.RED, block("rose"));
        //Purple
        dye(consumer, basePath, Items.PURPLE_DYE, EnumColor.PURPLE, block("violet"), block("lavender"));
        //Magenta
        dye(consumer, basePath, Items.MAGENTA_DYE, EnumColor.PINK, block("wildflower"));
        //Orange
        dye(consumer, basePath, Items.ORANGE_DYE, EnumColor.ORANGE, block("orange_cosmos"), block("burning_blossom"));
        //Pink
        dye(consumer, basePath, Items.PINK_DYE, EnumColor.BRIGHT_PINK, block("pink_daffodil"), block("pink_hibiscus"));
        //Cyan
        dye(consumer, basePath, Items.CYAN_DYE, EnumColor.DARK_AQUA, block("glowflower"));
        //Gray
        dye(consumer, basePath, Items.GRAY_DYE, EnumColor.DARK_GRAY, block("wilted_lily"));
        //Light Blue
        dye(consumer, basePath, Items.LIGHT_BLUE_DYE, EnumColor.INDIGO, block("blue_hydrangea"));
        //Yellow
        dye(consumer, basePath, Items.YELLOW_DYE, EnumColor.YELLOW, block("goldenrod"));
    }

    private void dye(Consumer<FinishedRecipe> consumer, String basePath, ItemLike output, EnumColor color, Block... inputs) {
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().from(Ingredient.of(inputs));
        ItemStackToItemStackRecipeBuilder.enriching(
                    inputIngredient,
                    new ItemStack(output, 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "dye/" + color.getRegistryPrefix()));
        //Flowers -> 4x dye output (See PigmentExtractingRecipeProvider#addFlowerExtractionRecipes for note)
        long flowerRate = 3 * PigmentExtractingRecipeProvider.DYE_RATE;
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                    inputIngredient,
                    MekanismPigments.PIGMENT_COLOR_LOOKUP.get(color).getStack(flowerRate)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pigment_extracting/" + color.getRegistryPrefix()));
    }
}