package mekanism.common.util;

import it.unimi.dsi.fastutil.longs.Long2DoubleArrayMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

public final class FluidIntersection {

    private FluidIntersection() {
    }

    public static Map<Fluid, FluidInDetails> find(BlockGetter world, AABB bounds) {
        int xMin = Mth.floor(bounds.minX);
        int yMin = Mth.floor(bounds.minY);
        int zMin = Mth.floor(bounds.minZ);
        int xMax = Math.max(xMin + 1, Mth.ceil(bounds.maxX));
        int yMax = Mth.ceil(bounds.maxY);
        int zMax = Math.max(zMin + 1, Mth.ceil(bounds.maxZ));
        Map<Fluid, FluidInDetails> result = new IdentityHashMap<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                for (int z = zMin; z < zMax; z++) {
                    pos.set(x, y, z);
                    FluidState state = world.getFluidState(pos);
                    if (!state.isEmpty()) {
                        double top = Math.min(bounds.maxY, y + state.getHeight(world, pos));
                        double height = top - Math.max(bounds.minY, y);
                        if (height > 0) {
                            Fluid fluid = state.getType() instanceof FlowingFluid flowing ? flowing.getSource() : state.getType();
                            FluidInDetails details = result.computeIfAbsent(fluid, ignored -> new FluidInDetails());
                            details.positions.put(pos.immutable(), state);
                            details.heights.merge(ChunkPos.asLong(x, z), height, Double::sum);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static class FluidInDetails {

        private final Map<BlockPos, FluidState> positions = new HashMap<>();
        private final Long2DoubleMap heights = new Long2DoubleArrayMap();

        public Map<BlockPos, FluidState> getPositions() {
            return positions;
        }

        public double getMaxHeight() {
            return heights.values().doubleStream().max().orElse(0);
        }
    }
}
