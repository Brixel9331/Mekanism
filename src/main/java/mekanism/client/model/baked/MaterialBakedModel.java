package mekanism.client.model.baked;

import java.util.List;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class MaterialBakedModel extends DynamicBakedModel<Void> {

    public MaterialBakedModel(BakedModel original, BlendMode blendMode) {
        super(original, Void.class, blendMode);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, @Nullable Void data) {
        return wrapped.getQuads(state, side, random);
    }
}
