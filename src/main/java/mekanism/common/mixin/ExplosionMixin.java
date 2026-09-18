package mekanism.common.mixin;

import mekanism.common.util.ExplosionBlockInteraction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Redirect(method = "finalizeExplosion", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean mekanism$beforeBlockExploded(Level world, BlockPos pos, BlockState state, int flags) {
        ExplosionBlockInteraction.beforeBlockExploded(world.getBlockState(pos), world, pos, (Explosion) (Object) this);
        return world.setBlock(pos, state, flags);
    }
}
