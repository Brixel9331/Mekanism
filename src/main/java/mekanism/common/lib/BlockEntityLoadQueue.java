package mekanism.common.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import mekanism.common.tile.interfaces.IBlockEntityLifecycle;
import mekanism.common.util.NeighborUpdates;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class BlockEntityLoadQueue {

    private static boolean registered;
    private final Set<BlockEntity> queued = Collections.newSetFromMap(new IdentityHashMap<>());
    private List<BlockEntity> pending = new ArrayList<>();
    private Set<BlockPos> neighborUpdates = new HashSet<>();

    public static void register() {
        if (!registered) {
            registered = true;
            ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register(BlockEntityLoadQueue::enqueue);
            ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register(BlockEntityLoadQueue::enqueueNeighbors);
        }
    }

    public static void enqueue(BlockEntity blockEntity, Level level) {
        enqueueNeighbors(blockEntity, level);
        if (blockEntity instanceof IBlockEntityLifecycle) {
            ((LevelAccess) level).mekanism$getBlockEntityLoadQueue().add(blockEntity);
        }
    }

    public static void enqueueNeighbors(BlockEntity blockEntity, Level level) {
        ((LevelAccess) level).mekanism$getBlockEntityLoadQueue().addNeighbors(blockEntity.getBlockPos());
    }

    private synchronized void addNeighbors(BlockPos pos) {
        neighborUpdates.add(pos.immutable());
    }

    private synchronized void add(BlockEntity blockEntity) {
        if (queued.add(blockEntity)) {
            pending.add(blockEntity);
        }
    }

    public void runPending(Level level) {
        List<BlockEntity> loading;
        Set<BlockPos> updates;
        synchronized (this) {
            if (pending.isEmpty() && neighborUpdates.isEmpty()) {
                return;
            }
            loading = pending;
            pending = new ArrayList<>();
            queued.clear();
            updates = neighborUpdates;
            neighborUpdates = new HashSet<>();
        }
        for (BlockEntity blockEntity : loading) {
            if (!blockEntity.isRemoved() && blockEntity.getLevel() == level) {
                ((IBlockEntityLifecycle) blockEntity).onLoad();
            }
        }
        for (BlockPos neighbor : updates) {
            NeighborUpdates.notifyBlockEntityNeighbors(level, neighbor);
        }
    }

    public interface LevelAccess {

        BlockEntityLoadQueue mekanism$getBlockEntityLoadQueue();
    }
}
