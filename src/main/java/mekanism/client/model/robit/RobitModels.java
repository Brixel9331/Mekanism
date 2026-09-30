package mekanism.client.model.robit;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.model.BaseModelCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public final class RobitModels {

    private static final ModelResourceLocation BASE_MODEL = new ModelResourceLocation(new ResourceLocation("mekanism", "robit"), "inventory");

    private RobitModels() {
    }

    public static BakedModel getModel(@Nullable ResourceLocation customModel) {
        ModelManager manager = Minecraft.getInstance().getModelManager();
        if (customModel != null) {
            BakedModel model = manager.getModel(customModel);
            if (model == null || model == manager.getMissingModel()) {
                model = BaseModelCache.getBaker().bake(customModel, BlockModelRotation.X0_Y0);
            }
            if (model != null && model != manager.getMissingModel()) {
                return model;
            }
        }
        return manager.getModel(BASE_MODEL);
    }

    public static BakedModel bind(BakedModel model, ResourceLocation skin) {
        return model instanceof RobitBakedModel robit ? robit.bind(skin) : model;
    }

    public static List<BakedQuad> getQuads(BakedModel model, ResourceLocation skin, RandomSource random) {
        BakedModel bound = bind(model, skin);
        List<BakedQuad> quads = new ArrayList<>();
        long seed = random.nextLong();
        RandomSource modelRandom = RandomSource.create(seed);
        for (Direction side : Direction.values()) {
            modelRandom.setSeed(seed);
            quads.addAll(bound.getQuads(null, side, modelRandom));
        }
        modelRandom.setSeed(seed);
        quads.addAll(bound.getQuads(null, null, modelRandom));
        return quads;
    }
}
