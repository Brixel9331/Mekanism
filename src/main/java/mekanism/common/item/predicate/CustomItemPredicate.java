package mekanism.common.item.predicate;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import mekanism.api.JsonConstants;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CustomItemPredicate extends ItemPredicate {

    private static final Map<ResourceLocation, Function<JsonObject, ? extends ItemPredicate>> TYPES = new HashMap<>();

    public static void register(ResourceLocation id, Function<JsonObject, ? extends ItemPredicate> factory) {
        if (TYPES.putIfAbsent(Objects.requireNonNull(id), Objects.requireNonNull(factory)) != null) {
            throw new IllegalArgumentException("Item predicate already registered: " + id);
        }
    }

    @Nullable
    public static ItemPredicate deserialize(JsonObject json) {
        if (json.has(JsonConstants.TYPE)) {
            ResourceLocation id = new ResourceLocation(json.get(JsonConstants.TYPE).getAsString());
            Function<JsonObject, ? extends ItemPredicate> factory = TYPES.get(id);
            if (factory != null) {
                return Objects.requireNonNull(factory.apply(json));
            }
        }
        return null;
    }

    protected CustomItemPredicate() {
    }

    protected abstract ResourceLocation getID();

    @Override
    public abstract boolean matches(@NotNull ItemStack stack);

    @NotNull
    @Override
    public JsonObject serializeToJson() {
        JsonObject object = new JsonObject();
        object.addProperty(JsonConstants.TYPE, getID().toString());
        return object;
    }
}
