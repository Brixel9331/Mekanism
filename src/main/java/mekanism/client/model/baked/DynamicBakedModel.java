package mekanism.client.model.baked;

import java.util.List;
import java.util.function.Supplier;
import mekanism.client.render.lib.ModelQuadMaterials;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class DynamicBakedModel<T> extends ForwardingBakedModel {

    private final Class<T> dataType;
    private final BlendMode blendMode;

    protected DynamicBakedModel(BakedModel original, Class<T> dataType, BlendMode blendMode) {
        wrapped = original;
        this.dataType = dataType;
        this.blendMode = blendMode;
    }

    public abstract List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, @Nullable T data);

    @Override
    public final List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return getQuads(state, side, random, null);
    }

    @Override
    public boolean isVanillaAdapter() {
        return RendererAccess.INSTANCE.getRenderer() == null;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter view, BlockState state, BlockPos pos, Supplier<RandomSource> random, RenderContext context) {
        emitQuads(state, random, context, getRenderData(view, pos));
    }

    @Nullable
    protected T getRenderData(BlockAndTintGetter view, BlockPos pos) {
        Object attachment = view instanceof RenderAttachedBlockView attached ? attached.getBlockEntityRenderAttachment(pos) : null;
        return dataType.isInstance(attachment) ? dataType.cast(attachment) : null;
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> random, RenderContext context) {
        emitItemQuads(stack, random, context, null);
    }

    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> random, RenderContext context, @Nullable T data) {
        emitQuads(null, random, context, data);
    }

    public void emitQuads(@Nullable BlockState state, Supplier<RandomSource> random, RenderContext context, @Nullable T data) {
        QuadEmitter emitter = context.getEmitter();
        for (Direction side : Direction.values()) {
            for (BakedQuad quad : getQuads(state, side, random.get(), data)) {
                ModelQuadMaterials.emit(quad, side, blendMode, emitter);
            }
        }
        for (BakedQuad quad : getQuads(state, null, random.get(), data)) {
            ModelQuadMaterials.emit(quad, null, blendMode, emitter);
        }
    }

    public BakedModel bind(@Nullable Object data) {
        return new ModelDataBakedModel<>(this, dataType.isInstance(data) ? dataType.cast(data) : null);
    }
}
