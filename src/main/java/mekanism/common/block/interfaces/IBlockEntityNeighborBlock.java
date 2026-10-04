package mekanism.common.block.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface IBlockEntityNeighborBlock {

    void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor);
}
