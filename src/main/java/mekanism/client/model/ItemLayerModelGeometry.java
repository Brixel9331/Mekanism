package mekanism.client.model;

import com.google.gson.JsonObject;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import mekanism.client.model.ElementModelGeometry.FaceData;
import mekanism.client.model.baked.MaterialBakedModel;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;

public final class ItemLayerModelGeometry implements ModelGeometry {

    private final Map<Integer, FaceData> layers;
    private final FaceData defaults;

    public ItemLayerModelGeometry(JsonObject json) {
        JsonObject data = GsonHelper.getAsJsonObject(json, "forge_data", new JsonObject());
        defaults = FaceData.readData(data, FaceData.DEFAULT);
        Map<Integer, FaceData> layers = new HashMap<>();
        GsonHelper.getAsJsonObject(data, "layers", new JsonObject()).entrySet().forEach(entry -> {
            int index = Integer.parseInt(entry.getKey());
            if (index < 0 || index >= ItemModelGenerator.LAYERS.size()) {
                throw new IllegalArgumentException("Invalid generated item layer: " + index);
            }
            layers.put(index, FaceData.readData(entry.getValue().getAsJsonObject(), defaults));
        });
        this.layers = Map.copyOf(layers);
    }

    @Override
    public BakedModel bake(BlockModel source, BakedModel original, ModelBaker baker, Function<Material, TextureAtlasSprite> sprites,
          ModelState state, ResourceLocation id, BlendMode blendMode) {
        BlockModel generated = new ItemModelGenerator().generateBlockModel(sprites, source);
        BakedModel model = generated.bake(baker, sprites, state, id);
        QuadTransformation rootTransform = ModelGeometry.rootTransform(source, state);
        Map<Direction, List<BakedQuad>> faces = new EnumMap<>(Direction.class);
        for (Direction side : Direction.values()) {
            faces.put(side, transform(model.getQuads(null, side, RandomSource.create(0)), rootTransform));
        }
        BakedModel result = new SimpleBakedModel(transform(model.getQuads(null, null, RandomSource.create(0)), rootTransform), Map.copyOf(faces),
              model.useAmbientOcclusion(), model.usesBlockLight(), model.isGui3d(), model.getParticleIcon(), model.getTransforms(), model.getOverrides());
        return new MaterialBakedModel(result, blendMode);
    }

    private List<BakedQuad> transform(List<BakedQuad> quads, QuadTransformation rootTransform) {
        List<BakedQuad> lit = quads.stream().map(quad -> layers.getOrDefault(quad.getTintIndex(), defaults).apply(quad)).toList();
        return rootTransform == QuadTransformation.identity ? lit : QuadUtils.transformBakedQuads(lit, rootTransform);
    }
}
