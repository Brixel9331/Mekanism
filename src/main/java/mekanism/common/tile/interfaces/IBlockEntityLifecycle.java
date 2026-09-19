package mekanism.common.tile.interfaces;

public interface IBlockEntityLifecycle {

    default void onLoad() {
    }

    default void onChunkUnloaded() {
    }
}
