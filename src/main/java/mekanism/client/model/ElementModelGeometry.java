package mekanism.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import mekanism.client.model.baked.MaterialBakedModel;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class ElementModelGeometry implements ModelGeometry {

    private final List<BlockElement> elements;
    private final JsonArray definitions;

    public ElementModelGeometry(JsonObject json) {
        JsonObject model = json.deepCopy();
        model.remove("transform");
        elements = BlockModel.fromString(model.toString()).getElements();
        definitions = GsonHelper.getAsJsonArray(json, "elements").deepCopy();
    }

    public static boolean hasFaceData(JsonObject json) {
        if (json.has("elements")) {
            for (JsonElement entry : json.getAsJsonArray("elements")) {
                JsonObject element = entry.getAsJsonObject();
                if (element.has("forge_data")) {
                    return true;
                }
                if (element.has("faces")) {
                    for (JsonElement face : element.getAsJsonObject("faces").asMap().values()) {
                        if (face.getAsJsonObject().has("forge_data")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public BakedModel bake(BlockModel source, BakedModel original, ModelBaker baker, Function<Material, TextureAtlasSprite> sprites, ModelState state,
          ResourceLocation id, BlendMode blendMode) {
        List<BakedQuad> unculled = new ArrayList<>();
        Map<Direction, List<BakedQuad>> culled = new EnumMap<>(Direction.class);
        for (Direction side : Direction.values()) {
            culled.put(side, new ArrayList<>());
        }
        FaceBakery bakery = new FaceBakery();
        for (int index = 0; index < elements.size(); index++) {
            BlockElement element = elements.get(index);
            JsonObject definition = definitions.get(index).getAsJsonObject();
            FaceData elementData = FaceData.read(definition, FaceData.DEFAULT);
            JsonObject faces = definition.getAsJsonObject("faces");
            for (Map.Entry<Direction, BlockElementFace> faceEntry : element.faces.entrySet()) {
                BlockElementFace face = faceEntry.getValue();
                BakedQuad baked = bakery.bakeQuad(element.from, element.to, face, sprites.apply(source.getMaterial(face.texture)), faceEntry.getKey(),
                      state, element.rotation, element.shade, id);
                FaceData data = FaceData.read(faces.getAsJsonObject(faceEntry.getKey().getName()), elementData);
                baked = data.apply(baked);
                if (face.cullForDirection == null) {
                    unculled.add(baked);
                } else {
                    culled.get(Direction.rotate(state.getRotation().getMatrix(), face.cullForDirection)).add(baked);
                }
            }
        }
        QuadTransformation rootTransform = ModelGeometry.rootTransform(source, state);
        if (rootTransform != QuadTransformation.identity) {
            unculled = QuadUtils.transformBakedQuads(unculled, rootTransform);
            culled.replaceAll((side, quads) -> QuadUtils.transformBakedQuads(quads, rootTransform));
        }
        culled.replaceAll((side, quads) -> List.copyOf(quads));
        BakedModel model = new SimpleBakedModel(List.copyOf(unculled), Map.copyOf(culled), original.useAmbientOcclusion(), original.usesBlockLight(),
              original.isGui3d(), original.getParticleIcon(), original.getTransforms(), original.getOverrides());
        return wrap(model, blendMode);
    }

    protected BakedModel wrap(BakedModel original, BlendMode blendMode) {
        return new MaterialBakedModel(original, blendMode);
    }

    record FaceData(int color, int blockLight, int skyLight, boolean ambientOcclusion) {

        static final FaceData DEFAULT = new FaceData(-1, 0, 0, true);

        BakedQuad apply(BakedQuad baked) {
            if (equals(DEFAULT)) {
                return baked;
            }
            Quad quad = new Quad(baked);
            quad.vertexTransform(vertex -> vertex.color(color >> 16 & 255, color >> 8 & 255, color & 255, color >>> 24).light(blockLight, skyLight));
            quad.setHasAmbientOcclusion(ambientOcclusion);
            return quad.bake();
        }

        private static FaceData read(JsonObject owner, FaceData fallback) {
            if (!owner.has("forge_data")) {
                return fallback;
            }
            return readData(owner.getAsJsonObject("forge_data"), fallback);
        }

        static FaceData readData(JsonObject data, FaceData fallback) {
            return new FaceData(GsonHelper.getAsInt(data, "color", fallback.color()), light(data, "block_light", fallback.blockLight()),
                  light(data, "sky_light", fallback.skyLight()), GsonHelper.getAsBoolean(data, "ambient_occlusion", fallback.ambientOcclusion()));
        }

        private static int light(JsonObject data, String key, int fallback) {
            int value = GsonHelper.getAsInt(data, key, fallback);
            if (value < 0 || value > 15) {
                throw new IllegalArgumentException(key + " must be between 0 and 15");
            }
            return value;
        }
    }
}
