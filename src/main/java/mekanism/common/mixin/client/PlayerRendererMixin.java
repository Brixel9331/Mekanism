package mekanism.common.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.render.RenderArmCallback;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(method = "renderRightHand", at = @At("HEAD"), cancellable = true)
    private void mekanism$renderRightArm(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, CallbackInfo callback) {
        if (RenderArmCallback.EVENT.invoker().renderArm((PlayerRenderer) (Object) this, player, HumanoidArm.RIGHT, pose, buffers, light)) {
            callback.cancel();
        }
    }

    @Inject(method = "renderLeftHand", at = @At("HEAD"), cancellable = true)
    private void mekanism$renderLeftArm(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, CallbackInfo callback) {
        if (RenderArmCallback.EVENT.invoker().renderArm((PlayerRenderer) (Object) this, player, HumanoidArm.LEFT, pose, buffers, light)) {
            callback.cancel();
        }
    }
}
