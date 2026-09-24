package mekanism.common.tile.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public interface IBlockEntityRenderBounds {

    @NotNull
    BlockPos getBlockPos();

    @NotNull
    default AABB getRenderBoundingBox() {
        return new AABB(getBlockPos());
    }
}
