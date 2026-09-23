package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import mekanism.client.render.armor.ICustomArmor;
import mekanism.client.render.armor.JetpackArmor;
import mekanism.common.registries.MekanismItems;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record MekanismTrinketRenderer(ICustomArmor model) implements TrinketRenderer {

    public static void register() {
        TrinketRendererRegistry.registerRenderer(MekanismItems.JETPACK.get(), new MekanismTrinketRenderer(JetpackArmor.JETPACK));
        TrinketRendererRegistry.registerRenderer(MekanismItems.ARMORED_JETPACK.get(), new MekanismTrinketRenderer(JetpackArmor.ARMORED_JETPACK));
    }

    @Override
    public void render(ItemStack stack, SlotReference slot, EntityModel<? extends LivingEntity> contextModel, PoseStack poses,
          MultiBufferSource buffers, int light, LivingEntity entity, float limbAngle, float limbDistance, float partialTicks,
          float animationProgress, float headYaw, float headPitch) {
        if (contextModel instanceof HumanoidModel<?> humanoid) {
            poses.pushPose();
            try {
                model.render(humanoid, poses, buffers, light, OverlayTexture.NO_OVERLAY, partialTicks, stack.hasFoil(), entity, stack);
            } finally {
                poses.popPose();
            }
        }
    }
}
