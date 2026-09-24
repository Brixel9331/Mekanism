package mekanism.client.render.tileentity;

import mekanism.common.mixin.client.LevelRendererFrustumAccessor;
import mekanism.common.tile.interfaces.IBlockEntityRenderBounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public interface BoundingBoxBlockEntityRenderer<TILE extends BlockEntity> extends BlockEntityRenderer<TILE> {

    @Override
    default boolean shouldRender(TILE tile, Vec3 camera) {
        if (!BlockEntityRenderer.super.shouldRender(tile, camera)) {
            return false;
        }
        if (tile instanceof IBlockEntityRenderBounds bounds) {
            LevelRenderer renderer = Minecraft.getInstance().levelRenderer;
            if (renderer != null) {
                LevelRendererFrustumAccessor accessor = (LevelRendererFrustumAccessor) renderer;
                Frustum frustum = accessor.mekanism$getCapturedFrustum();
                if (frustum == null) {
                    frustum = accessor.mekanism$getCullingFrustum();
                }
                return frustum == null || frustum.isVisible(bounds.getRenderBoundingBox());
            }
        }
        return true;
    }
}
