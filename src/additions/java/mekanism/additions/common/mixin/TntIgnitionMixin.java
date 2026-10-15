package mekanism.additions.common.mixin;

import mekanism.additions.common.block.BlockObsidianTNT;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntBlock.class)
public abstract class TntIgnitionMixin {

    @Inject(method = "explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)V",
          at = @At("HEAD"), cancellable = true)
    private static void mekanism$igniteObsidian(Level level, BlockPos pos, LivingEntity igniter, CallbackInfo callback) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BlockObsidianTNT block) {
            block.onCaughtFire(state, level, pos, null, igniter);
            callback.cancel();
        }
    }
}
