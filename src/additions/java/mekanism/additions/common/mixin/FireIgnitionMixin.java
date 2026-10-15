package mekanism.additions.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mekanism.additions.common.block.BlockObsidianTNT;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireBlock.class)
public abstract class FireIgnitionMixin {

    @WrapOperation(method = "checkBurnOut", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void mekanism$igniteObsidian(Level level, BlockPos pos, Operation<Void> original, @Local BlockState state) {
        if (state.getBlock() instanceof BlockObsidianTNT block) {
            block.onCaughtFire(state, level, pos, null, null);
        } else {
            original.call(level, pos);
        }
    }
}
