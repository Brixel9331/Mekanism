package mekanism.common.util;

import java.util.Set;
import mekanism.common.block.interfaces.IBlockEntityNeighborBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public final class NeighborUpdates {

    private static final Direction[] DIRECTIONS = Direction.values();

    private NeighborUpdates() {
    }

    public static void notifyBlockEntityNeighbors(Level world, BlockPos fromPos) {
        if (!world.isInWorldBounds(fromPos)) {
            return;
        }
        for (Direction side : DIRECTIONS) {
            BlockPos pos = fromPos.relative(side);
            BlockState state = getLoadedState(world, pos);
            if (state != null && state.getBlock() instanceof IBlockEntityNeighborBlock block) {
                block.onNeighborChange(state, world, pos, fromPos);
            }
        }
    }

    public static boolean isGettingPowered(Level world, BlockPos pos) {
        if (getLoadedState(world, pos) == null) {
            return false;
        }
        for (Direction side : DIRECTIONS) {
            BlockPos neighborPos = pos.relative(side);
            BlockState neighbor = getLoadedState(world, neighborPos);
            if (neighbor != null && (neighbor.getSignal(world, neighborPos, side) > 0
                  || neighbor.isRedstoneConductor(world, neighborPos) && isDirectlyGettingPowered(world, neighborPos))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDirectlyGettingPowered(Level world, BlockPos pos) {
        if (getLoadedState(world, pos) == null) {
            return false;
        }
        for (Direction side : DIRECTIONS) {
            BlockPos neighborPos = pos.relative(side);
            BlockState neighbor = getLoadedState(world, neighborPos);
            if (neighbor != null && neighbor.getDirectSignal(world, neighborPos, side) > 0) {
                return true;
            }
        }
        return false;
    }

    public static void notifyLoadedNeighborsOfTileChange(Level world, BlockPos pos) {
        BlockState source = getLoadedState(world, pos);
        if (source == null) {
            return;
        }
        for (Direction side : DIRECTIONS) {
            BlockPos neighborPos = pos.relative(side);
            notifyNeighbor(world, neighborPos, pos, source.getBlock());
            if (side.getAxis().isHorizontal()) {
                BlockState neighbor = getLoadedState(world, neighborPos);
                if (neighbor != null && neighbor.isRedstoneConductor(world, neighborPos)) {
                    BlockPos comparatorPos = neighborPos.relative(side);
                    BlockState comparator = getLoadedState(world, comparatorPos);
                    if (comparator != null && comparator.is(Blocks.COMPARATOR)) {
                        comparator.neighborChanged(world, comparatorPos, source.getBlock(), pos, false);
                    }
                }
            }
        }
    }

    public static void notifyNeighborsOfChange(@Nullable Level world, BlockPos fromPos, Set<Direction> neighbors) {
        if (!neighbors.isEmpty()) {
            BlockState source = getLoadedState(world, fromPos);
            if (source != null) {
                for (Direction side : neighbors) {
                    notifyNeighbor(world, fromPos.relative(side), fromPos, source.getBlock());
                }
            }
        }
    }

    public static void notifyNeighborOfChange(@Nullable Level world, BlockPos pos, BlockPos fromPos) {
        BlockState source = getLoadedState(world, fromPos);
        if (source != null) {
            notifyNeighbor(world, pos, fromPos, source.getBlock());
        }
    }

    private static void notifyNeighbor(Level world, BlockPos pos, BlockPos fromPos, Block source) {
        BlockState state = getLoadedState(world, pos);
        if (state != null) {
            state.neighborChanged(world, pos, source, fromPos, false);
        }
    }

    @Nullable
    private static BlockState getLoadedState(@Nullable Level world, BlockPos pos) {
        if (world == null || !world.isInWorldBounds(pos)) {
            return null;
        }
        ChunkAccess chunk = world.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
        return chunk == null ? null : chunk.getBlockState(pos);
    }
}
