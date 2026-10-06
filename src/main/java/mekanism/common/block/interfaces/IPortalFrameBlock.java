package mekanism.common.block.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface IPortalFrameBlock {

    boolean isPortalFrame(BlockState state, BlockGetter world, BlockPos pos);
}
