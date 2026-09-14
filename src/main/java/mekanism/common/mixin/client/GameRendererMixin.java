package mekanism.common.mixin.client;

import mekanism.client.render.RenderFrameCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "render", at = @At("RETURN"))
    private void mekanism$endRenderFrame(float partialTick, long finishTime, boolean renderLevel, CallbackInfo callback) {
        RenderFrameCallback.END.invoker().onRenderFrame(minecraft, partialTick);
    }
}
