package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;

@FunctionalInterface
public interface RenderArmCallback {

    Event<RenderArmCallback> EVENT = EventFactory.createArrayBacked(RenderArmCallback.class,
          callbacks -> (renderer, player, arm, pose, buffers, light) -> {
              for (RenderArmCallback callback : callbacks) {
                  if (callback.renderArm(renderer, player, arm, pose, buffers, light)) {
                      return true;
                  }
              }
              return false;
          });

    boolean renderArm(PlayerRenderer renderer, AbstractClientPlayer player, HumanoidArm arm, PoseStack pose, MultiBufferSource buffers, int light);
}
