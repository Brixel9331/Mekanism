package mekanism.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import mekanism.client.model.baked.DynamicBakedModel;
import mekanism.client.render.lib.ModelQuadRenderer;
import mekanism.common.util.EnumUtils;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MekanismISTER extends BlockEntityWithoutLevelRenderer {

    protected MekanismISTER() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    protected EntityModelSet getEntityModels() {
        //Just have this method as a helper for what we pass as entity models rather than bothering to
        // use an AT to access it directly
        return Minecraft.getInstance().getEntityModels();
    }

    protected BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
        //Just have this method as a helper for what we pass as the block entity render dispatcher
        // rather than bothering to use an AT to access it directly
        return Minecraft.getInstance().getBlockEntityRenderDispatcher();
    }

    protected Camera getCamera() {
        return getBlockEntityRenderDispatcher().camera;
    }

    @Override
    public abstract void onResourceManagerReload(@NotNull ResourceManager resourceManager);

    @Override
    public abstract void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
          int light, int overlayLight);

    /**
     * @implNote Heavily based on/from vanilla's ItemRenderer#render code that calls the renderByItem method on the ISBER
     */
    protected void renderBlockItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
          int light, int overlayLight, @Nullable Object modelData) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        Block block = blockItem.getBlock();
        boolean fabulous;
        if (displayContext != ItemDisplayContext.GUI && !displayContext.firstPerson()) {
            fabulous = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
        } else {
            fabulous = true;
        }
        Minecraft minecraft = Minecraft.getInstance();
        BlockState defaultState = block.defaultBlockState();
        //TODO: See if we can come up with a better way to handle getting the model, maybe even one that supports non block items??
        BakedModel baseModel = minecraft.getModelManager().getBlockModelShaper().getBlockModel(defaultState);
        if (baseModel instanceof DynamicBakedModel<?> dynamicModel) {
            baseModel = dynamicModel.bind(modelData);
        }
        long seed = 42;
        RandomSource random = RandomSource.create();
        boolean hasEffect = stack.hasFoil();
        RenderType renderType = ItemBlockRenderTypes.getRenderType(defaultState, fabulous);
        VertexConsumer buffer = fabulous ? ItemRenderer.getFoilBufferDirect(renderer, renderType, true, hasEffect) :
              ItemRenderer.getFoilBuffer(renderer, renderType, true, hasEffect);
        //Note: Manually call the render quads lists rather than using renderModelLists so that we can pass the proper render type and model data
        for (Direction direction : EnumUtils.DIRECTIONS) {
            random.setSeed(seed);
            renderQuadList(matrix, buffer, baseModel.getQuads(defaultState, direction, random), stack, light, overlayLight);
        }
        random.setSeed(seed);
        renderQuadList(matrix, buffer, baseModel.getQuads(defaultState, null, random), stack, light, overlayLight);
    }

    private static void renderQuadList(PoseStack matrix, VertexConsumer buffer, List<BakedQuad> quads, ItemStack stack,
          int light, int overlay) {
        ItemColor colors = ColorProviderRegistry.ITEM.get(stack.getItem());
        for (BakedQuad quad : quads) {
            int color = quad.isTinted() && colors != null ? colors.getColor(stack, quad.getTintIndex()) : -1;
            ModelQuadRenderer.render(matrix.last(), buffer, quad, (color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F,
                  (color & 255) / 255F, 1, light, overlay);
        }
    }
}
