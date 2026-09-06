package mekanism.common.mixin.client;

import mekanism.client.key.MouseScrollCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyVariable(method = "onScroll", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private double mekanism$horizontalScroll(double vertical, long window, double horizontal, double originalVertical) {
        return Minecraft.ON_OSX && vertical == 0 ? horizontal : vertical;
    }

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"),
          cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void mekanism$scrollMode(long window, double horizontal, double vertical, CallbackInfo callback, double delta, int steps) {
        if (MouseScrollCallback.EVENT.invoker().onScroll(minecraft, delta)) {
            callback.cancel();
        }
    }
}
