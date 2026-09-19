package mekanism.common.tile.interfaces;

import net.minecraft.nbt.CompoundTag;

public interface IBlockEntityUpdateReceiver {

    void handleUpdateTag(CompoundTag tag);

    default void handleUpdatePacket(CompoundTag tag) {
        handleUpdateTag(tag);
    }
}
