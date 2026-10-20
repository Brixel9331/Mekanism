package mekanism.tools.common.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.tools.common.item.ItemRefinedGlowstoneArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HumanoidArmorLayer.class)
public abstract class GlowArmorMixin {

    @ModifyVariable(method = "renderArmorPiece", at = @At("HEAD"), argsOnly = true)
    private int mekanismtools$fullbright(int light, PoseStack matrix, MultiBufferSource buffers, LivingEntity entity,
          EquipmentSlot slot, int packedLight, HumanoidModel<?> model) {
        return entity.getItemBySlot(slot).getItem() instanceof ItemRefinedGlowstoneArmor ? LightTexture.FULL_BRIGHT : light;
    }
}
