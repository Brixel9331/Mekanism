package mekanism.common.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import mekanism.common.tile.interfaces.IBlockEntityLifecycle;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class BlockEntityLoadQueue {

    private static boolean registered;
    private final Set<BlockEntity> queued = Collections.newSetFromMap(new IdentityHashMap<>());
    private List<BlockEntity> pending = new ArrayList<>();

    public static void register() {
        if (!registered) {
            registered = true;
            ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register(BlockEntityLoadQueue::enqueue);
        }
    }

    public static void enqueue(BlockEntity blockEntity, Level level) {
        if (blockEntity instanceof IBlockEntityLifecycle) {
            ((LevelAccess) level).mekanism$getBlockEntityLoadQueue().add(blockEntity);
        }
    }

    private synchronized void add(BlockEntity blockEntity) {
        if (queued.add(blockEntity)) {
            pending.add(blockEntity);
        }
    }

    public void runPending(Level level) {
        List<BlockEntity> loading;
        synchronized (this) {
            if (pending.isEmpty()) {
                return;
            }
            loading = pending;
            pending = new ArrayList<>();
            queued.clear();
        }
        for (BlockEntity blockEntity : loading) {
            if (!blockEntity.isRemoved() && blockEntity.getLevel() == level) {
                ((IBlockEntityLifecycle) blockEntity).onLoad();
            }
        }
    }

    public interface LevelAccess {

        BlockEntityLoadQueue mekanism$getBlockEntityLoadQueue();
    }
}
