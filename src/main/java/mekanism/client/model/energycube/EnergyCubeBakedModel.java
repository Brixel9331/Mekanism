package mekanism.client.model.energycube;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.api.RelativeSide;
import mekanism.client.model.baked.DynamicBakedModel;
import mekanism.client.model.energycube.EnergyCubeGeometry.FaceData;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.common.lib.render.EnergyCubeModelData;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EnergyCubeBakedModel extends DynamicBakedModel<EnergyCubeModelData> {

    private static final QuadTransformation LED_TRANSFORMS = QuadTransformation.list(QuadTransformation.fullbright, QuadTransformation.uvShift(-2, 0));
    private final Map<Integer, List<BakedQuad>> cache = new ConcurrentHashMap<>();
    private final FaceData frame;
    private final Map<RelativeSide, FaceData> leds;
    private final Map<RelativeSide, FaceData> activeLEDs;
    private final Map<RelativeSide, FaceData> ports;
    private final Map<RelativeSide, FaceData> activePorts;

    EnergyCubeBakedModel(BakedModel original, FaceData frame, Map<RelativeSide, FaceData> leds, Map<RelativeSide, FaceData> ports, BlendMode blendMode) {
        super(original, EnergyCubeModelData.class, blendMode);
        this.frame = frame;
        this.leds = leds;
        this.ports = ports;
        this.activeLEDs = new EnumMap<>(RelativeSide.class);
        this.activePorts = new EnumMap<>(RelativeSide.class);
        //Note: We don't bother having any form of lazy transformations take place here as this should only have a memory
        // impact equivalent to having two models: one with the leds and ports off, and one with all of them active
        for (Map.Entry<RelativeSide, FaceData> entry : this.leds.entrySet()) {
            activeLEDs.put(entry.getKey(), entry.getValue().transform(LED_TRANSFORMS));
        }
        for (Map.Entry<RelativeSide, FaceData> entry : this.ports.entrySet()) {
            activePorts.put(entry.getKey(), entry.getValue().transform(QuadTransformation.filtered_fullbright));
        }
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, @Nullable EnergyCubeModelData data) {
        EnergyCubeModelData snapshot = data == null ? EnergyCubeModelData.INACTIVE : data;
        int key = snapshot.cacheKey() * 7 + (side == null ? 6 : side.ordinal());
        return cache.computeIfAbsent(key, ignored -> createQuads(side, snapshot));
    }

    private List<BakedQuad> createQuads(@Nullable Direction side, EnergyCubeModelData data) {
        List<BakedQuad> quads = new ArrayList<>(frame.getFaces(side));
        for (RelativeSide dir : RelativeSide.values()) {
            if (data.isLit(dir.ordinal())) {
                quads.addAll(activeLEDs.get(dir).getFaces(side));
                quads.addAll(activePorts.get(dir).getFaces(side));
            } else {
                quads.addAll(leds.get(dir).getFaces(side));
                if (data.isActive(dir.ordinal())) {
                    quads.addAll(ports.get(dir).getFaces(side));
                }
            }
        }
        return List.copyOf(quads);
    }
}
