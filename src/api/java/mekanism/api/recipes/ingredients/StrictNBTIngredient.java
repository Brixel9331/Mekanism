package mekanism.api.recipes.ingredients;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public final class StrictNBTIngredient implements CustomIngredient {

    private static final Serializer SERIALIZER = new Serializer();
    private final ItemStack stack;

    private StrictNBTIngredient(ItemStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Strict NBT ingredients cannot be empty");
        }
        this.stack = stack.copyWithCount(1);
    }

    public static void register() {
        CustomIngredientSerializer<?> existing = CustomIngredientSerializer.get(SERIALIZER.getIdentifier());
        if (existing == null) {
            CustomIngredientSerializer.register(SERIALIZER);
        } else if (existing != SERIALIZER) {
            throw new IllegalStateException("Strict NBT ingredient serializer is already registered");
        }
    }

    public static Ingredient of(ItemStack stack) {
        register();
        return new StrictNBTIngredient(Objects.requireNonNull(stack)).toVanilla();
    }

    @Override
    public boolean test(ItemStack candidate) {
        return ItemStack.isSameItemSameTags(stack, candidate);
    }

    @Override
    public List<ItemStack> getMatchingStacks() {
        return List.of(stack.copy());
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private static final class Serializer implements CustomIngredientSerializer<StrictNBTIngredient> {

        private final ResourceLocation id = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "strict_nbt");

        @Override
        public ResourceLocation getIdentifier() {
            return id;
        }

        @Override
        public StrictNBTIngredient read(JsonObject json) {
            ResourceLocation itemId = ResourceLocation.tryParse(GsonHelper.getAsString(json, "item"));
            if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
                throw new JsonSyntaxException("Unknown item in strict NBT ingredient: " + itemId);
            }
            Item item = BuiltInRegistries.ITEM.get(itemId);
            if (item == Items.AIR) {
                throw new JsonSyntaxException("Strict NBT ingredients cannot use air");
            }
            ItemStack stack = new ItemStack(item);
            if (json.has("nbt")) {
                try {
                    stack.setTag(TagParser.parseTag(GsonHelper.getAsString(json, "nbt")));
                } catch (CommandSyntaxException e) {
                    throw new JsonSyntaxException("Invalid strict ingredient NBT", e);
                }
            }
            return new StrictNBTIngredient(stack);
        }

        @Override
        public void write(JsonObject json, StrictNBTIngredient ingredient) {
            json.addProperty("item", BuiltInRegistries.ITEM.getKey(ingredient.stack.getItem()).toString());
            if (ingredient.stack.getTag() != null) {
                json.addProperty("nbt", ingredient.stack.getTag().toString());
            }
        }

        @Override
        public StrictNBTIngredient read(FriendlyByteBuf buffer) {
            return new StrictNBTIngredient(buffer.readItem());
        }

        @Override
        public void write(FriendlyByteBuf buffer, StrictNBTIngredient ingredient) {
            buffer.writeItem(ingredient.stack);
        }
    }
}
