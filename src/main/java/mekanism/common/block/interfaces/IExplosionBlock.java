package mekanism.common.block.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IExplosionBlock {

    default float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        return state.getBlock().getExplosionResistance();
    }

    default void beforeBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
    }
}
