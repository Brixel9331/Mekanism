package mekanism.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.client.model.robit.RobitModels;
import mekanism.client.render.lib.ModelQuadRenderer;
import mekanism.client.render.entity.RenderRobit.RobitModelWrapper;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.registries.MekanismRobitSkins.SkinLookup;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RenderRobit extends MobRenderer<EntityRobit, RobitModelWrapper> {

    public RenderRobit(EntityRendererProvider.Context context) {
        super(context, new RobitModelWrapper(), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull EntityRobit robit) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    public static class RobitModelWrapper extends EntityModel<EntityRobit> {

        RobitModelWrapper() {
        }

        @Nullable
        private EntityRobit robit;

        @Override
        public void setupAnim(@NotNull EntityRobit robit, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            this.robit = robit;
        }

        @Override
        public void renderToBuffer(@NotNull PoseStack matrix, @NotNull VertexConsumer builder, int light, int overlayLight, float red, float green, float blue,
              float alpha) {
            if (robit == null) {
                //Setup didn't happen right
                return;
            }
            matrix.pushPose();
            try {
                ResourceLocation texture = robit.getModelTexture();
                SkinLookup skinLookup = MekanismRobitSkins.lookup(robit.level().registryAccess(), robit.getSkin());
                BakedModel model = RobitModels.getModel(skinLookup.skin().customModel());
                matrix.mulPose(Axis.XP.rotationDegrees(180));
                matrix.translate(-0.5, -1.5, -0.5);
                PoseStack.Pose last = matrix.last();
                for (BakedQuad quad : RobitModels.getQuads(model, texture, robit.level().random)) {
                    ModelQuadRenderer.render(last, builder, quad, red, green, blue, alpha, light, overlayLight);
                }
            } finally {
                matrix.popPose();
                //Clear current robit after rendering it
                robit = null;
            }
        }
    }
}
