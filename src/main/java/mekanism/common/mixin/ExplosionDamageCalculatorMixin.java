package mekanism.common.mixin;

import java.util.Optional;
import mekanism.common.block.interfaces.IExplosionBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExplosionDamageCalculator.class)
public abstract class ExplosionDamageCalculatorMixin {

    @Inject(method = "getBlockExplosionResistance", at = @At("HEAD"), cancellable = true)
    private void mekanism$blockResistance(Explosion explosion, BlockGetter world, BlockPos pos, BlockState state, FluidState fluid,
          CallbackInfoReturnable<Optional<Float>> cir) {
        if (state.getBlock() instanceof IExplosionBlock block) {
            cir.setReturnValue(Optional.of(Math.max(block.getExplosionResistance(state, world, pos, explosion), fluid.getExplosionResistance())));
        }
    }
}
