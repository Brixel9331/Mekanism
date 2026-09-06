package mekanism.common.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.client.render.FogRenderCallback;
import mekanism.client.render.FogRenderCallback.FogDistance;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    @ModifyArgs(method = "setupColor", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V", ordinal = 1, remap = false))
    private static void mekanism$fogColor(Args args, Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount) {
        Vector3f color = new Vector3f(fogRed, fogGreen, fogBlue);
        FogRenderCallback.COLOR.invoker().render(camera, partialTick, color);
        args.set(0, fogRed = color.x());
        args.set(1, fogGreen = color.y());
        args.set(2, fogBlue = color.z());
    }

    @Inject(method = "setupFog", at = @At("RETURN"))
    private static void mekanism$fogDistance(Camera camera, FogMode mode, float renderDistance, boolean thickFog, float partialTick, CallbackInfo callback) {
        FogDistance original = new FogDistance(RenderSystem.getShaderFogStart(), RenderSystem.getShaderFogEnd());
        FogDistance distance = FogRenderCallback.DISTANCE.invoker().render(camera, mode, partialTick, original);
        if (!distance.equals(original)) {
            RenderSystem.setShaderFogStart(distance.near());
            RenderSystem.setShaderFogEnd(distance.far());
        }
    }
}
