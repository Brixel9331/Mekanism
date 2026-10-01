package mekanism.client.render;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public interface BlockParticleEffects {

    default boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine particles) {
        return false;
    }

    default boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine particles) {
        return false;
    }
}
