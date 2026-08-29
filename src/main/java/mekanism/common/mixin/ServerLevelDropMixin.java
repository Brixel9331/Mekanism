package mekanism.common.mixin;

import mekanism.common.lib.DropCapture;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelDropMixin {

    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void mekanism$captureDrops(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (DropCapture.capture(entity)) {
            cir.setReturnValue(false);
        }
    }
}
