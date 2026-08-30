package mekanism.common.mixin;

import mekanism.common.lib.LivingEntityTickCallback;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void mekanism$livingTick(CallbackInfo ci) {
        LivingEntityTickCallback.EVENT.invoker().onTick((LivingEntity) (Object) this);
    }
}
