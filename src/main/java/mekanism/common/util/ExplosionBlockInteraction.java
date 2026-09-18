package mekanism.common.util;

import mekanism.common.block.interfaces.IExplosionBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class ExplosionBlockInteraction {

    private ExplosionBlockInteraction() {
    }

    public static float getResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        Block block = state.getBlock();
        return block instanceof IExplosionBlock handler ? handler.getExplosionResistance(state, world, pos, explosion) : block.getExplosionResistance();
    }

    public static void beforeBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        if (state.getBlock() instanceof IExplosionBlock handler) {
            handler.beforeBlockExploded(state, world, pos, explosion);
        }
    }

    public static void explodeBlock(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        beforeBlockExploded(state, world, pos, explosion);
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        state.getBlock().wasExploded(world, pos, explosion);
    }
}
