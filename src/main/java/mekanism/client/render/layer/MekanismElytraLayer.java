package mekanism.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.common.registries.MekanismItems;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MekanismElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation HDPE_ELYTRA = new ResourceLocation("mekanism", "textures/entity/hdpe_elytra.png");
    private final ElytraModel<T> model;

    public MekanismElytraLayer(RenderLayerParent<T, M> entityRenderer, EntityModelSet modelSet) {
        super(entityRenderer);
        model = new ElytraModel<>(modelSet.bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float limbSwing, float limbSwingAmount, float partialTicks,
          float age, float yaw, float pitch) {
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (!shouldRender(stack, entity)) {
            return;
        }
        ResourceLocation texture = getElytraTexture(stack, entity);
        if (entity instanceof AbstractClientPlayer player) {
            if (player.isElytraLoaded() && player.getElytraTextureLocation() != null) {
                texture = player.getElytraTextureLocation();
            } else if (player.isCapeLoaded() && player.getCloakTextureLocation() != null && player.isModelPartShown(PlayerModelPart.CAPE)) {
                texture = player.getCloakTextureLocation();
            }
        }
        pose.pushPose();
        try {
            pose.translate(0, 0, 0.125F);
            getParentModel().copyPropertiesTo(model);
            model.setupAnim(entity, limbSwing, limbSwingAmount, age, yaw, pitch);
            model.renderToBuffer(pose, ItemRenderer.getArmorFoilBuffer(buffers, RenderType.armorCutoutNoCull(texture), false, stack.hasFoil()),
                  light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        } finally {
            pose.popPose();
        }
    }

    public boolean shouldRender(@NotNull ItemStack stack, @NotNull T entity) {
        return stack.getItem() == MekanismItems.HDPE_REINFORCED_ELYTRA.asItem();
    }

    @NotNull
    public ResourceLocation getElytraTexture(@NotNull ItemStack stack, @NotNull T entity) {
        return HDPE_ELYTRA;
    }
}
