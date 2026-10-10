package mekanism.common.recipe.compat;

import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public class AE2RecipeProvider extends CompatRecipeProvider {

    public AE2RecipeProvider(String modid) {
        super(modid);
    }

    @Override
    protected void registerRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Certus Crystal -> Certus Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(TagKey.create(Registries.ITEM, new ResourceLocation("forge", "gems/certus_quartz"))),
                    new ItemStack(item("certus_quartz_dust"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "certus_crystal_to_dust"));

        //Fluix Crystal -> Fluix Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(item("fluix_crystal")),
                    new ItemStack(item("fluix_dust"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "fluix_crystal_to_dust"));

        //Sky Stone -> Sky Stone Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("sky_stone_block")),
                    new ItemStack(item("sky_dust"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "sky_stone_to_dust"));

        //Sky Stone Dust -> Sky Stone
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(item("sky_dust")),
                    new ItemStack(block("sky_stone_block"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "sky_stone_dust_to_sky_stone"));

        addDecorativeRecipes(consumer, basePath + "decorative/");
    }

    private void addDecorativeRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        addDecorativeQuartzRecipes(consumer, basePath + "certus_quartz/");
        addDecorativeSkyStoneRecipes(consumer, basePath + "sky_stone/");
    }

    private void addDecorativeSkyStoneRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        addDecorativeCrushingSkyStoneRecipes(consumer, basePath + "crushing/");
        addDecorativeEnrichingSkyStoneRecipes(consumer, basePath + "enriching/");
    }

    private void addDecorativeCrushingSkyStoneRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Smooth Sky Stone Chest -> Sky Stone Chest
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("smooth_sky_stone_chest")),
                    new ItemStack(block("sky_stone_chest"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_chest_to_chest"));

        //Smooth Sky Stone -> Sky Stone
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("smooth_sky_stone_block")),
                    new ItemStack(block("sky_stone_block"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_to_stone"));
        //Smooth Sky Stone Slab -> Sky Stone Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("smooth_sky_stone_slab")),
                    new ItemStack(block("sky_stone_slab"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_slab_to_slab"));
        //Smooth Sky Stone Stairs -> Sky Stone Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("smooth_sky_stone_stairs")),
                    new ItemStack(block("sky_stone_stairs"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_stairs_to_stairs"));
        //Smooth Sky Stone Wall -> Sky Stone Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("smooth_sky_stone_wall")),
                    new ItemStack(block("sky_stone_wall"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_wall_to_wall"));

        //Sky Stone Brick -> Smooth Sky Stone
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("sky_stone_brick")),
                    new ItemStack(block("smooth_sky_stone_block"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_to_smooth"));
        //Sky Stone Brick Slab -> Smooth Sky Stone Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("sky_stone_brick_slab")),
                    new ItemStack(block("smooth_sky_stone_slab"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_slab_to_smooth_slab"));
        //Sky Stone Brick Stairs -> Smooth Sky Stone Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("sky_stone_brick_stairs")),
                    new ItemStack(block("smooth_sky_stone_stairs"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_stairs_to_smooth_stairs"));
        //Sky Stone Brick Wall -> Smooth Sky Stone Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("sky_stone_brick_wall")),
                    new ItemStack(block("smooth_sky_stone_wall"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_wall_to_smooth_wall"));

        //Sky Stone Small Brick -> Sky Stone Brick
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("sky_stone_small_brick")),
                    new ItemStack(block("sky_stone_brick"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "small_brick_to_brick"));
        //Sky Stone Small Brick Slab -> Sky Stone Brick Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("sky_stone_small_brick_slab")),
                    new ItemStack(block("sky_stone_brick_slab"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "small_brick_slab_to_brick_slab"));
        //Sky Stone Small Brick Stairs -> Sky Stone Brick Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("sky_stone_small_brick_stairs")),
                    new ItemStack(block("sky_stone_brick_stairs"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "small_brick_stairs_to_brick_stairs"));
        //Sky Stone Small Brick Wall -> Sky Stone Brick Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("sky_stone_small_brick_wall")),
                    new ItemStack(block("sky_stone_brick_wall"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "small_brick_wall_to_brick_wall"));
    }

    private void addDecorativeEnrichingSkyStoneRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Sky Stone Chest -> Smooth Sky Stone Chest
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("sky_stone_chest")),
                    new ItemStack(block("smooth_sky_stone_chest"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chest_to_smooth_chest"));

        //Sky Stone Slab -> Smooth Sky Stone Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("sky_stone_slab")),
                    new ItemStack(block("smooth_sky_stone_slab"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "slab_to_smooth_slab"));
        //Sky Stone Stairs -> Smooth Sky Stone Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("sky_stone_stairs")),
                    new ItemStack(block("smooth_sky_stone_stairs"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "stairs_to_smooth_stairs"));
        //Sky Stone Wall -> Smooth Sky Stone Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("sky_stone_wall")),
                    new ItemStack(block("smooth_sky_stone_wall"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "wall_to_smooth_wall"));

        //Smooth Sky Stone -> Sky Stone Brick
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("smooth_sky_stone_block")),
                    new ItemStack(block("sky_stone_brick"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_to_brick"));
        //Smooth Sky Stone Slab -> Sky Stone Brick Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("smooth_sky_stone_slab")),
                    new ItemStack(block("sky_stone_brick_slab"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_slab_to_brick_slab"));
        //Smooth Sky Stone Stairs -> Sky Stone Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("smooth_sky_stone_stairs")),
                    new ItemStack(block("sky_stone_brick_stairs"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_stairs_to_brick_stairs"));
        //Smooth Sky Stone Wall -> Sky Stone Brick Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("smooth_sky_stone_wall")),
                    new ItemStack(block("sky_stone_brick_wall"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_wall_to_brick_wall"));

        //Sky Stone Brick -> Sky Stone Small Brick
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("sky_stone_brick")),
                    new ItemStack(block("sky_stone_small_brick"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_to_small_brick"));
        //Sky Stone Brick Slab -> Sky Stone Small Brick Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("sky_stone_brick_slab")),
                    new ItemStack(block("sky_stone_small_brick_slab"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_slab_to_small_brick_slab"));
        //Sky Stone Brick Stairs -> Sky Stone Small Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("sky_stone_brick_stairs")),
                    new ItemStack(block("sky_stone_small_brick_stairs"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_stairs_to_small_brick_stairs"));
        //Sky Stone Brick Wall -> Sky Stone Small Brick Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("sky_stone_brick_wall")),
                    new ItemStack(block("sky_stone_small_brick_wall"), 1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_wall_to_small_brick_wall"));
    }

    private void addDecorativeQuartzRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        addDecorativeQuartzCrushingRecipes(consumer, basePath + "crushing/");
        addDecorativeQuartzEnrichingRecipes(consumer, basePath + "enriching/");
    }

    private void addDecorativeQuartzCrushingRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Certus Quartz Block -> Chiseled Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("quartz_block")),
                    new ItemStack(block("chiseled_quartz_block"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "block_to_chiseled_block"));
        //Certus Quartz Slab -> Chiseled Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("quartz_slab")),
                    new ItemStack(block("chiseled_quartz_slab"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "slab_to_chiseled_slab"));
        //Certus Quartz Stairs -> Chiseled Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("quartz_stairs")),
                    new ItemStack(block("chiseled_quartz_stairs"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "stairs_to_chiseled_stairs"));
        //Certus Quartz Wall -> Chiseled Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("quartz_wall")),
                    new ItemStack(block("chiseled_quartz_wall"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "wall_to_chiseled_wall"));

        //Chiseled Certus Quartz Block -> Certus Quartz Pillar Block
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("chiseled_quartz_block")),
                    new ItemStack(block("quartz_pillar"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_block_to_pillar"));
        //Chiseled Certus Quartz Slab -> Certus Quartz Pillar Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("chiseled_quartz_slab")),
                    new ItemStack(block("quartz_pillar_slab"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_slab_to_pillar_slab"));
        //Chiseled Certus Quartz Stairs -> Certus Quartz Pillar Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("chiseled_quartz_stairs")),
                    new ItemStack(block("quartz_pillar_stairs"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_stairs_to_pillar_stairs"));
        //Chiseled Certus Quartz Wall -> Certus Quartz Pillar Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("chiseled_quartz_wall")),
                    new ItemStack(block("quartz_pillar_wall"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_wall_to_pillar_wall"));

        //Certus Quartz Pillar Block -> Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("quartz_pillar")),
                    new ItemStack(block("quartz_block"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_to_block"));
        //Certus Quartz Pillar Slab -> Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("quartz_pillar_slab")),
                    new ItemStack(block("quartz_slab"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_slab_to_slab"));
        //Certus Quartz Pillar Stairs -> Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("quartz_pillar_stairs")),
                    new ItemStack(block("quartz_stairs"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_stairs_to_stairs"));
        //Certus Quartz Pillar Wall -> Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(block("quartz_pillar_wall")),
                    new ItemStack(block("quartz_wall"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_wall_to_wall"));
    }

    private void addDecorativeQuartzEnrichingRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Chiseled Certus Quartz Block -> Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("chiseled_quartz_block")),
                    new ItemStack(block("quartz_block"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_block_to_block"));
        //Chiseled Certus Quartz Slab -> Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("chiseled_quartz_slab")),
                    new ItemStack(block("quartz_slab"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_slab_to_slab"));
        //Chiseled Certus Quartz Stairs -> Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("chiseled_quartz_stairs")),
                    new ItemStack(block("quartz_stairs"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_stairs_to_stairs"));
        //Chiseled Certus Quartz Wall -> Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("chiseled_quartz_wall")),
                    new ItemStack(block("quartz_wall"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_wall_to_wall"));

        //Certus Quartz Pillar Block -> Chiseled Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("quartz_pillar")),
                    new ItemStack(block("chiseled_quartz_block"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_to_chiseled_block"));
        //Certus Quartz Pillar Slab -> Chiseled Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("quartz_pillar_slab")),
                    new ItemStack(block("chiseled_quartz_slab"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_slab_to_chiseled_slab"));
        //Certus Quartz Pillar Stairs -> Chiseled Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("quartz_pillar_stairs")),
                    new ItemStack(block("chiseled_quartz_stairs"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_stairs_to_chiseled_stairs"));
        //Certus Quartz Pillar Wall -> Chiseled Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("quartz_pillar_wall")),
                    new ItemStack(block("chiseled_quartz_wall"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_wall_to_chiseled_wall"));

        //Certus Quartz Block -> Certus Quartz Pillar Block
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("quartz_block")),
                    new ItemStack(block("quartz_pillar"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "block_to_pillar"));
        //Certus Quartz Slab -> Certus Quartz Pillar Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("quartz_slab")),
                    new ItemStack(block("quartz_pillar_slab"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "slab_to_pillar_slab"));
        //Certus Quartz Stairs -> Certus Quartz Pillar Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("quartz_stairs")),
                    new ItemStack(block("quartz_pillar_stairs"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "stairs_to_pillar_stairs"));
        //Certus Quartz Wall -> Certus Quartz Pillar Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(block("quartz_wall")),
                    new ItemStack(block("quartz_pillar_wall"))
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "wall_to_pillar_wall"));
    }
}