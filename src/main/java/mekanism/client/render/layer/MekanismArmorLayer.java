package mekanism.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.render.armor.ISpecialGear;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public final class MekanismArmorLayer implements ArmorRenderer {

    private final ISpecialGear gear;
    private final HumanoidModel<LivingEntity> armorModel = new HumanoidModel<>(HumanoidModel.createMesh(CubeDeformation.NONE, 0).getRoot().bake(64, 32));

    public MekanismArmorLayer(ISpecialGear gear) {
        this.gear = gear;
    }

    @Override
    public void render(PoseStack matrix, MultiBufferSource renderer, ItemStack stack, LivingEntity entity, EquipmentSlot slot, int light,
          HumanoidModel<LivingEntity> contextModel) {
        if (stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == slot) {
            contextModel.copyPropertiesTo(armorModel);
            armorModel.setAllVisible(false);
            switch (slot) {
                case HEAD -> armorModel.head.visible = armorModel.hat.visible = true;
                case CHEST -> armorModel.body.visible = armorModel.rightArm.visible = armorModel.leftArm.visible = true;
                case LEGS -> armorModel.body.visible = armorModel.rightLeg.visible = armorModel.leftLeg.visible = true;
                case FEET -> armorModel.rightLeg.visible = armorModel.leftLeg.visible = true;
            }
            matrix.pushPose();
            try {
                gear.getGearModel(armor.getType()).render(armorModel, matrix, renderer, light, OverlayTexture.NO_OVERLAY,
                      Minecraft.getInstance().getFrameTime(), stack.hasFoil(), entity, stack);
            } finally {
                matrix.popPose();
            }
        }
    }
}
