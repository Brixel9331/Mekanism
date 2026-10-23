package mekanism.common.tile.interfaces;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public interface IBlockEntityRenderBounds {

    @NotNull
    default AABB getRenderBoundingBox() {
        return new AABB(((BlockEntity) this).getBlockPos());
    }
}
