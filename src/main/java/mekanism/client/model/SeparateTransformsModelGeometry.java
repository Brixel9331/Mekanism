package mekanism.client.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class SeparateTransformsModelGeometry implements ModelGeometry {

    private final ResourceLocation base;
    private final Map<ItemDisplayContext, ResourceLocation> perspectives;

    public SeparateTransformsModelGeometry(ResourceLocation base, Map<String, ResourceLocation> perspectives) {
        this.base = base;
        this.perspectives = new EnumMap<>(ItemDisplayContext.class);
        for (ItemDisplayContext context : ItemDisplayContext.values()) {
            ResourceLocation model = perspectives.getOrDefault(context.name().toLowerCase(Locale.ROOT), perspectives.get(context.getSerializedName()));
            if (model != null) {
                this.perspectives.put(context, model);
            }
        }
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        Collection<ResourceLocation> models = new ArrayList<>(perspectives.values());
        models.add(base);
        return models;
    }

    @Override
    public BakedModel bake(BlockModel source, BakedModel original, ModelBaker baker, Function<Material, TextureAtlasSprite> sprites,
          ModelState state, ResourceLocation id, BlendMode blendMode) {
        Map<ItemDisplayContext, BakedModel> models = new EnumMap<>(ItemDisplayContext.class);
        perspectives.forEach((context, location) -> models.put(context, baker.bake(location, state)));
        return new Baked(baker.bake(base, state), models);
    }

    private static final class Baked extends ForwardingBakedModel {

        private final Map<ItemDisplayContext, BakedModel> perspectives;
        private final ItemTransforms transforms;

        private Baked(BakedModel base, Map<ItemDisplayContext, BakedModel> perspectives) {
            wrapped = base;
            this.perspectives = Map.copyOf(perspectives);
            transforms = new ItemTransforms(transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND), transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND),
                  transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND), transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND),
                  transform(ItemDisplayContext.HEAD), transform(ItemDisplayContext.GUI), transform(ItemDisplayContext.GROUND), transform(ItemDisplayContext.FIXED));
        }

        private ItemTransform transform(ItemDisplayContext context) {
            return perspectives.getOrDefault(context, wrapped).getTransforms().getTransform(context);
        }

        @Override
        public ItemTransforms getTransforms() {
            return transforms;
        }

        @Override
        public boolean isVanillaAdapter() {
            return false;
        }

        @Override
        public void emitItemQuads(ItemStack stack, Supplier<RandomSource> random, RenderContext context) {
            perspectives.getOrDefault(context.itemTransformationMode(), wrapped).emitItemQuads(stack, random, context);
        }
    }
}
