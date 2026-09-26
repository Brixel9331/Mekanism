package mekanism.common.tile.interfaces;

import mekanism.common.util.BoundingBlockInteraction.RemovedMainBlock;
import org.jetbrains.annotations.Nullable;

public interface IBoundingBlockRemoval {

    void setRemovedMainBlock(@Nullable RemovedMainBlock mainBlock);

    @Nullable
    RemovedMainBlock takeRemovedMainBlock();
}
