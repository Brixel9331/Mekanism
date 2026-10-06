package mekanism.common.mixin;

import mekanism.common.block.interfaces.IPistonReactionBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockStateBase.class)
public abstract class PistonReactionMixin {

    @Inject(method = "getPistonPushReaction", at = @At("RETURN"), cancellable = true)
    private void mekanism$pistonReaction(CallbackInfoReturnable<PushReaction> callback) {
        BlockState state = (BlockState) (Object) this;
        if (state.getBlock() instanceof IPistonReactionBlock block) {
            callback.setReturnValue(block.getPistonPushReaction(state, callback.getReturnValue()));
        }
    }
}
