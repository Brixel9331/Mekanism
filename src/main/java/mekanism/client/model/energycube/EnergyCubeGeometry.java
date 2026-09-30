package mekanism.client.model.energycube;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import mekanism.api.RelativeSide;
import mekanism.client.model.ModelGeometry;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class EnergyCubeGeometry implements ModelGeometry {

    private final List<BlockElement> frame;
    private final Map<RelativeSide, List<BlockElement>> leds;
    private final Map<RelativeSide, List<BlockElement>> ports;

    EnergyCubeGeometry(List<BlockElement> frame, Map<RelativeSide, List<BlockElement>> leds, Map<RelativeSide, List<BlockElement>> ports) {
        this.frame = frame;
        this.leds = leds;
        this.ports = ports;
    }

    @Override
    public BakedModel bake(BlockModel source, BakedModel original, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
          ResourceLocation modelLocation, BlendMode blendMode) {
        Function<String, TextureAtlasSprite> rawSpriteGetter = spriteGetter.compose(source::getMaterial);
        FaceData frame = bakeElement(rawSpriteGetter, modelState, modelLocation, this.frame);
        Map<RelativeSide, FaceData> leds = bakeElements(rawSpriteGetter, modelState, modelLocation, this.leds);
        Map<RelativeSide, FaceData> ports = bakeElements(rawSpriteGetter, modelState, modelLocation, this.ports);
        QuadTransformation rootTransform = ModelGeometry.rootTransform(source, modelState);
        if (rootTransform != QuadTransformation.identity) {
            frame = frame.transform(rootTransform);
            leds.replaceAll((side, faces) -> faces.transform(rootTransform));
            ports.replaceAll((side, faces) -> faces.transform(rootTransform));
        }
        return new EnergyCubeBakedModel(original, frame, leds, ports, blendMode);
    }

    private Map<RelativeSide, FaceData> bakeElements(Function<String, TextureAtlasSprite> spriteGetter, ModelState modelState,
          ResourceLocation modelLocation, Map<RelativeSide, List<BlockElement>> sideBasedElements) {
        Map<RelativeSide, FaceData> sideBasedFaceData = new EnumMap<>(RelativeSide.class);
        for (Map.Entry<RelativeSide, List<BlockElement>> entry : sideBasedElements.entrySet()) {
            FaceData faceData = bakeElement(spriteGetter, modelState, modelLocation, entry.getValue());
            sideBasedFaceData.put(entry.getKey(), faceData);
        }
        return sideBasedFaceData;
    }

    private FaceData bakeElement(Function<String, TextureAtlasSprite> spriteGetter, ModelState modelState, ResourceLocation modelLocation, List<BlockElement> elements) {
        FaceData data = new FaceData();
        for (BlockElement element : elements) {
            for (Entry<Direction, BlockElementFace> faceEntry : element.faces.entrySet()) {
                BlockElementFace face = faceEntry.getValue();
                TextureAtlasSprite sprite = spriteGetter.apply(face.texture);
                //noinspection ConstantConditions (can be null)
                Direction direction = face.cullForDirection == null ? null : Direction.rotate(modelState.getRotation().getMatrix(), face.cullForDirection);
                data.addFace(direction, new FaceBakery().bakeQuad(element.from, element.to, face, sprite, faceEntry.getKey(), modelState,
                      element.rotation, element.shade, modelLocation));
            }
        }
        return data;
    }

    static class FaceData {

        private List<BakedQuad> unculledFaces;
        private Map<Direction, List<BakedQuad>> culledFaces;

        public List<BakedQuad> getFaces(@Nullable Direction side) {
            if (side == null) {
                return unculledFaces == null ? Collections.emptyList() : unculledFaces;
            }
            return culledFaces == null ? Collections.emptyList() : culledFaces.getOrDefault(side, Collections.emptyList());
        }

        public void addFace(@Nullable Direction direction, BakedQuad quad) {
            List<BakedQuad> quads;
            if (direction == null) {
                if (unculledFaces == null) {
                    unculledFaces = new ArrayList<>();
                }
                quads = unculledFaces;
            } else {
                if (culledFaces == null) {
                    culledFaces = new EnumMap<>(Direction.class);
                }
                quads = culledFaces.computeIfAbsent(direction, dir -> new ArrayList<>());
            }
            quads.add(quad);
        }

        public FaceData transform(QuadTransformation transformation) {
            if (unculledFaces == null && culledFaces == null) {
                return this;
            }
            FaceData transformed = new FaceData();
            if (unculledFaces != null) {
                transformed.unculledFaces = QuadUtils.transformBakedQuads(unculledFaces, transformation);
            }
            if (culledFaces != null) {
                transformed.culledFaces = new EnumMap<>(Direction.class);
                for (Map.Entry<Direction, List<BakedQuad>> entry : culledFaces.entrySet()) {
                    transformed.culledFaces.put(entry.getKey(), QuadUtils.transformBakedQuads(entry.getValue(), transformation));
                }
            }
            return transformed;
        }
    }
}
