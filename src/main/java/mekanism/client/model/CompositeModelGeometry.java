package mekanism.client.model;

import com.google.gson.JsonArray;
import io.github.fabricators_of_create.porting_lib.models.UnbakedGeometryHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class CompositeModelGeometry implements ModelGeometry {

    private final Map<String, ResourceLocation> parts;
    private final List<String> itemOrder;

    public CompositeModelGeometry(Map<String, ResourceLocation> parts, @Nullable JsonArray order) {
        if (parts.isEmpty()) {
            throw new IllegalArgumentException("A composite model requires at least one child");
        }
        this.parts = new LinkedHashMap<>(parts);
        List<String> itemOrder = new ArrayList<>();
        if (order == null) {
            itemOrder.addAll(parts.keySet());
        } else {
            order.forEach(value -> {
                String name = value.getAsString();
                if (!parts.containsKey(name)) {
                    throw new IllegalArgumentException("Unknown model child: " + name);
                }
                itemOrder.add(name);
            });
        }
        this.itemOrder = List.copyOf(itemOrder);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return parts.values();
    }

    @Override
    public BakedModel bake(BlockModel source, BakedModel original, ModelBaker baker, Function<Material, TextureAtlasSprite> sprites,
          ModelState state, ResourceLocation id, BlendMode blendMode) {
        ModelState transformed = UnbakedGeometryHelper.composeRootTransformIntoModelState(state, source.getRootTransform());
        Map<String, BakedModel> children = new LinkedHashMap<>();
        parts.forEach((name, location) -> {
            if (source.isComponentVisible(name, true)) {
                children.put(name, baker.bake(location, transformed));
            }
        });
        List<BakedModel> items = itemOrder.stream().filter(children::containsKey).map(children::get).toList();
        return new Baked(original, List.copyOf(children.values()), items);
    }

    private static final class Baked extends ForwardingBakedModel {

        private final List<BakedModel> children;
        private final List<BakedModel> items;

        private Baked(BakedModel original, List<BakedModel> children, List<BakedModel> items) {
            wrapped = original;
            this.children = children;
            this.items = items;
        }

        @Override
        public boolean isVanillaAdapter() {
            return false;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
            List<BakedQuad> quads = new ArrayList<>();
            for (BakedModel child : children) {
                quads.addAll(child.getQuads(state, side, random));
            }
            return quads;
        }

        @Override
        public void emitBlockQuads(BlockAndTintGetter view, BlockState state, BlockPos pos, Supplier<RandomSource> random, RenderContext context) {
            children.forEach(child -> child.emitBlockQuads(view, state, pos, random, context));
        }

        @Override
        public void emitItemQuads(ItemStack stack, Supplier<RandomSource> random, RenderContext context) {
            items.forEach(child -> child.emitItemQuads(stack, random, context));
        }
    }
}
