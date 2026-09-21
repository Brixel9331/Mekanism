package mekanism.common.mixin;

import mekanism.common.registries.MekanismAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class StepHeightMixin {

    @Inject(method = "maxUpStep", at = @At("RETURN"), cancellable = true)
    private void mekanism$stepHeight(CallbackInfoReturnable<Float> callback) {
        if ((Object) this instanceof Player player) {
            callback.setReturnValue(Math.max(0, callback.getReturnValueF() + (float) player.getAttributeValue(MekanismAttributes.STEP_HEIGHT)));
        }
    }
}
