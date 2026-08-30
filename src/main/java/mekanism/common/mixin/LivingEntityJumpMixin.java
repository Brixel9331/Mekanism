package mekanism.common.mixin;

import mekanism.common.lib.LivingEntityJumpCallback;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityJumpMixin {

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void mekanism$livingJump(CallbackInfo ci) {
        LivingEntityJumpCallback.EVENT.invoker().onJump((LivingEntity) (Object) this);
    }
}
