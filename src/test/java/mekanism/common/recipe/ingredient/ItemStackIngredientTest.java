package mekanism.common.recipe.ingredient;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.Unpooled;
import java.util.List;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator;
import mekanism.common.recipe.lookup.cache.type.ItemInputCache;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ItemStackIngredientTest {

    private static final ItemStackIngredientCreator CREATOR = ItemStackIngredientCreator.INSTANCE;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void plainIngredientsIgnoreNbtButRespectAmounts() {
        ItemStackIngredient ingredient = CREATOR.from(Items.DIAMOND, 3);
        ItemStack candidate = new ItemStack(Items.DIAMOND, 3);
        candidate.getOrCreateTag().putString("label", "extra");
        assertTrue(ingredient.test(candidate));
        assertFalse(ingredient.test(candidate.copyWithCount(2)));
        assertTrue(ingredient.testType(candidate.copyWithCount(1)));
        assertFalse(ingredient.test(new ItemStack(Items.EMERALD, 3)));
        ItemStack matching = ingredient.getMatchingInstance(candidate.copyWithCount(20));
        assertEquals(3, matching.getCount());
        assertEquals(candidate.getTag(), matching.getTag());
    }

    @Test
    void strictIngredientsPreserveDamageAndRejectExtraTags() {
        ItemStack template = new ItemStack(Items.DIAMOND_PICKAXE);
        template.setDamageValue(25);
        ItemStackIngredient ingredient = CREATOR.from(template);
        ItemStack candidate = template.copy();
        assertTrue(ingredient.test(candidate));
        template.setDamageValue(30);
        assertTrue(ingredient.test(candidate));
        assertFalse(ingredient.test(template));
        candidate.getOrCreateTag().putBoolean("extra", true);
        assertFalse(ingredient.test(candidate));
        assertFalse(ingredient.test(new ItemStack(Items.DIAMOND_PICKAXE)));
    }

    @Test
    void strictNbtSurvivesJsonAndPacketRoundTrips() {
        ItemStack template = new ItemStack(Items.DIAMOND, 4);
        CompoundTag tag = template.getOrCreateTag();
        tag.putBoolean("enabled", true);
        tag.putShort("grade", (short) 7);
        tag.putLong("energy", 10L);
        tag.putFloat("purity", 0.5F);
        tag.putIntArray("samples", new int[]{1, 2, 3});
        ItemStackIngredient ingredient = CREATOR.from(template);
        ItemStackIngredient jsonCopy = CREATOR.deserialize(JsonParser.parseString(ingredient.serialize().toString()));
        assertTrue(jsonCopy.test(template));
        assertEquals(tag, jsonCopy.getRepresentations().get(0).getTag());
        ItemStackIngredient packetCopy = packetCopy(ingredient);
        assertTrue(packetCopy.test(template));
        assertEquals(tag, packetCopy.getRepresentations().get(0).getTag());
    }

    @Test
    void multiIngredientsRetainEachAmountAndFlattenNestedAlternatives() {
        ItemStackIngredient first = CREATOR.from(Items.DIAMOND, 2);
        ItemStackIngredient second = CREATOR.from(Items.EMERALD, 4);
        ItemStackIngredient ingredient = CREATOR.createMulti(first, CREATOR.createMulti(second, CREATOR.from(Items.IRON_INGOT, 8)));
        for (ItemStackIngredient copy : List.of(ingredient, CREATOR.deserialize(ingredient.serialize()), packetCopy(ingredient))) {
            assertTrue(copy.test(new ItemStack(Items.DIAMOND, 2)));
            assertFalse(copy.test(new ItemStack(Items.EMERALD, 3)));
            assertTrue(copy.test(new ItemStack(Items.EMERALD, 4)));
            assertEquals(8, copy.getNeededAmount(new ItemStack(Items.IRON_INGOT)));
            assertEquals(3, copy.getRepresentations().size());
        }
    }

    @Test
    void returnedRepresentationsDoNotMutateTheIngredient() {
        ItemStack template = new ItemStack(Items.DIAMOND, 2);
        template.getOrCreateTag().putString("grade", "pure");
        ItemStackIngredient ingredient = CREATOR.from(template);
        ItemStack representation = ingredient.getRepresentations().get(0);
        representation.setCount(99);
        representation.getOrCreateTag().putString("grade", "impure");
        assertTrue(ingredient.test(template));
        assertEquals(2, ingredient.getRepresentations().get(0).getCount());
        assertEquals(template.getTag(), ingredient.getRepresentations().get(0).getTag());
    }

    @Test
    void inputCacheKeepsStrictNbtSeparateFromItemOnlyRecipes() {
        ItemStack template = new ItemStack(Items.DIAMOND);
        template.getOrCreateTag().putBoolean("enabled", true);
        ItemInputCache<MekanismRecipe> cache = new ItemInputCache<>();
        MekanismRecipe strict = recipe("strict");
        assertFalse(cache.mapInputs(strict, CREATOR.from(template)));
        assertTrue(cache.contains(template));
        assertFalse(cache.contains(new ItemStack(Items.DIAMOND)));
        assertSame(strict, cache.findFirstRecipe(template, candidate -> true));
        MekanismRecipe plain = recipe("plain");
        assertFalse(cache.mapInputs(plain, CREATOR.from(Items.DIAMOND)));
        assertSame(plain, cache.findFirstRecipe(new ItemStack(Items.DIAMOND), candidate -> true));
        assertTrue(cache.contains(template, candidate -> candidate == plain));
        cache.clear();
        assertFalse(cache.contains(template));
    }

    @Test
    void complexFabricIngredientsUseFullPredicateMatching() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("enabled", true);
        Ingredient partial = DefaultCustomIngredients.nbt(Ingredient.of(Items.DIAMOND), nbt, false);
        ItemInputCache<MekanismRecipe> cache = new ItemInputCache<>();
        ItemStackIngredient ingredient = CREATOR.from(partial);
        assertTrue(cache.mapInputs(recipe("partial"), ingredient));
        assertFalse(cache.contains(new ItemStack(Items.DIAMOND)));
        ItemStack candidate = new ItemStack(Items.DIAMOND);
        candidate.getOrCreateTag().putBoolean("enabled", true);
        candidate.getOrCreateTag().putInt("extra", 123);
        assertTrue(ingredient.test(candidate));
        assertFalse(ingredient.test(new ItemStack(Items.DIAMOND)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "[]", "12", "{}", "{\"ingredient\":{\"item\":\"minecraft:diamond\"},\"amount\":0}",
          "{\"ingredient\":{\"item\":\"minecraft:diamond\"},\"amount\":-1}"})
    void invalidJsonIsRejected(String json) {
        assertThrows(JsonSyntaxException.class, () -> CREATOR.deserialize(JsonParser.parseString(json)));
    }

    @Test
    void invalidDirectIngredientsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> CREATOR.from(ItemStack.EMPTY));
        assertThrows(IllegalArgumentException.class, () -> CREATOR.from(Ingredient.EMPTY, 1));
        assertThrows(IllegalArgumentException.class, () -> CREATOR.from(Items.DIAMOND, 0));
        assertThrows(IllegalArgumentException.class, () -> CREATOR.createMulti());
    }

    private static ItemStackIngredient packetCopy(ItemStackIngredient ingredient) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            ingredient.write(buffer);
            ItemStackIngredient copy = CREATOR.read(buffer);
            assertFalse(buffer.isReadable());
            return copy;
        } finally {
            buffer.release();
        }
    }

    private static MekanismRecipe recipe(String name) {
        return new MekanismRecipe(new ResourceLocation("test", name)) {
            @Override
            public void write(FriendlyByteBuf buffer) {
                buffer.writeResourceLocation(getId());
            }

            @Override
            public boolean isIncomplete() {
                return false;
            }

            @Override
            public RecipeSerializer<?> getSerializer() {
                throw new UnsupportedOperationException();
            }

            @Override
            public RecipeType<?> getType() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
