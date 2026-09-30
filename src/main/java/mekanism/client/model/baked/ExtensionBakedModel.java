package mekanism.client.model.baked;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.List;
import java.util.function.Supplier;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.ModelQuadMaterials;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ExtensionBakedModel extends DynamicBakedModel<Void> {

    private final Cache<Key, List<BakedQuad>> cache = CacheBuilder.newBuilder().maximumSize(256).build();
    private final Cache<BakedModel, BakedModel> overrideModels = CacheBuilder.newBuilder().maximumSize(256).build();
    private final ItemOverrides overrides = new ItemOverrides(null, null, List.of()) {
        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            BakedModel resolved = wrapped.getOverrides().resolve(model, stack, level, entity, seed);
            if (resolved == null || resolved == model || resolved == wrapped) {
                return resolved == null ? null : model;
            }
            return overrideModels.asMap().computeIfAbsent(resolved, original -> new ExtensionBakedModel(original) {
                @Override
                protected QuadTransformation getTransformation() {
                    return ExtensionBakedModel.this.getTransformation();
                }
            });
        }
    };

    public ExtensionBakedModel(BakedModel original) {
        super(original, Void.class, BlendMode.DEFAULT);
    }

    @Nullable
    protected QuadTransformation getTransformation() {
        return null;
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter view, BlockState state, BlockPos pos, Supplier<RandomSource> random, RenderContext context) {
        emitTransformed(context, () -> wrapped.emitBlockQuads(view, state, pos, random, context));
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> random, RenderContext context) {
        emitTransformed(context, () -> wrapped.emitItemQuads(stack, random, context));
    }

    private void emitTransformed(RenderContext context, Runnable emit) {
        QuadTransformation transformation = getTransformation();
        if (transformation == null) {
            emit.run();
            return;
        }
        context.pushTransform(quad -> transform(quad, transformation));
        try {
            emit.run();
        } finally {
            context.popTransform();
        }
    }

    protected TextureAtlasSprite getSprite(QuadView quad) {
        return SpriteFinder.get(Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS)).find(quad);
    }

    private boolean transform(MutableQuadView emitted, QuadTransformation transformation) {
        var material = emitted.material();
        Quad quad = new Quad(emitted.toBakedQuad(getSprite(emitted)));
        quad.setHasAmbientOcclusion(material.ambientOcclusion() != TriState.FALSE);
        if (transformation.transform(quad)) {
            BakedQuad baked = quad.bake();
            var transformedMaterial = RendererAccess.INSTANCE.getRenderer().materialFinder().copyFrom(material)
                  .emissive(material.emissive() || ModelQuadMaterials.isFullbright(baked))
                  .disableDiffuse(!quad.isShade())
                  .ambientOcclusion(quad.hasAmbientOcclusion() ? material.ambientOcclusion() : TriState.FALSE).find();
            emitted.fromVanilla(baked.getVertices(), 0).material(transformedMaterial).colorIndex(quad.getTint()).nominalFace(quad.getSide());
            for (int i = 0; i < 4; i++) {
                var normal = quad.getVertices()[i].getNormal();
                emitted.normal(i, normal.x, normal.y, normal.z);
            }
        }
        return true;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, @Nullable Void data) {
        List<BakedQuad> quads = wrapped.getQuads(state, side, random);
        QuadTransformation transformation = getTransformation();
        if (transformation == null || quads.isEmpty()) {
            return quads;
        }
        Key key = new Key(List.copyOf(quads), transformation);
        return cache.asMap().computeIfAbsent(key, value -> QuadUtils.transformBakedQuads(value.quads(), value.transformation()));
    }

    public static class LightedBakedModel extends ExtensionBakedModel {

        public LightedBakedModel(BakedModel original) {
            super(original);
        }

        @Override
        protected QuadTransformation getTransformation() {
            return QuadTransformation.filtered_fullbright;
        }
    }

    private record Key(List<BakedQuad> quads, QuadTransformation transformation) {
    }
}
