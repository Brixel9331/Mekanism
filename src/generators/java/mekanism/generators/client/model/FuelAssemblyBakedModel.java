package mekanism.generators.client.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.Quad;
import mekanism.common.lib.Color;
import mekanism.client.model.baked.DynamicBakedModel;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import mekanism.client.render.lib.ModelQuadMaterials;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import java.util.function.Supplier;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Eventually make use of this if we figure out a way to not have coolant rendering make this invisible
@NothingNullByDefault
public class FuelAssemblyBakedModel extends DynamicBakedModel<Boolean> {

    private static final Color GLOW_ARGB = Color.rgbad(0.466, 0.882, 0.929, 0.6);
    private static final Vec3 NORTH_EAST = new Vec3(0.95, 0.125, 0.05);
    private static final Vec3 SOUTH_WEST = new Vec3(0.05, 0.125, 0.95);

    private final Map<Direction, List<BakedQuad>> cachedGlows = new EnumMap<>(Direction.class);
    private final double height;

    public FuelAssemblyBakedModel(BakedModel original, double height) {
        super(original, Boolean.class, BlendMode.DEFAULT);
        this.height = height;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, @Nullable Boolean glowing) {
        List<BakedQuad> quads = wrapped.getQuads(state, side, rand);
        //Note: Technically we would want the null side rather than a cullable side, but as this only would end up being used in fission reactors where
        // it should be in a checkerboard pattern we may as well allow it to be culled
        //TODO: Re-evaluate the above thought if we ever end up being able to make use of this model
        if (side != null && side.getAxis().isHorizontal()) {
            if (Boolean.TRUE.equals(glowing)) {
                //TODO: Eventually we may want to make the glow component be part of the json so resource packs can customize it more
                List<BakedQuad> allQuads = cachedGlows.computeIfAbsent(side, s -> {
                    Vec3 startPos = switch (s) {
                        case NORTH, EAST -> NORTH_EAST;
                        case SOUTH, WEST -> SOUTH_WEST;
                        default -> throw new IllegalStateException("Unexpected face");
                    };
                    Quad.Builder quadBuilder = new Quad.Builder(MekanismRenderer.whiteIcon, s)
                          .light(LightTexture.FULL_BLOCK, LightTexture.FULL_SKY)
                          .uv(0, 0, 16, 16)
                          .color(GLOW_ARGB)
                          .rect(startPos, 0.9, height, 1);
                    return List.of(quadBuilder.build().bake());
                });
                if (quads.isEmpty()) {
                    return allQuads;
                }
                //Combine the quads if the base model has any translucent ones
                List<BakedQuad> mergedQuads = new ArrayList<>(quads);
                mergedQuads.addAll(allQuads);
                return mergedQuads;
            }
        }
        //Fallback to our "default" model arrangement. The item variant uses this
        return quads;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter view, BlockState state, BlockPos pos, Supplier<RandomSource> random, RenderContext context) {
        wrapped.emitBlockQuads(view, state, pos, random, context);
        if (Boolean.TRUE.equals(getRenderData(view, pos))) {
            for (Direction side : Direction.values()) {
                if (side.getAxis().isHorizontal()) {
                    getQuads(state, side, random.get(), true);
                    for (BakedQuad quad : cachedGlows.get(side)) {
                        ModelQuadMaterials.emit(quad, side, BlendMode.TRANSLUCENT, context.getEmitter());
                    }
                }
            }
        }
    }
}
