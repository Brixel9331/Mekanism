package mekanism.common.world;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class RetrogenQueue {

    private final Map<ResourceKey<Level>, LinkedHashSet<ChunkPos>> dimensions = new HashMap<>();

    public synchronized void add(ResourceKey<Level> dimension, ChunkPos pos) {
        dimensions.computeIfAbsent(dimension, key -> new LinkedHashSet<>()).add(pos);
    }

    public synchronized void remove(ResourceKey<Level> dimension, ChunkPos pos) {
        LinkedHashSet<ChunkPos> queue = dimensions.get(dimension);
        if (queue != null) {
            queue.remove(pos);
            if (queue.isEmpty()) {
                dimensions.remove(dimension);
            }
        }
    }

    public synchronized void remove(ResourceKey<Level> dimension) {
        dimensions.remove(dimension);
    }

    public synchronized void clear() {
        dimensions.clear();
    }

    private synchronized ChunkPos poll(ResourceKey<Level> dimension) {
        LinkedHashSet<ChunkPos> queue = dimensions.get(dimension);
        if (queue == null) {
            return null;
        }
        var iterator = queue.iterator();
        ChunkPos next = iterator.next();
        iterator.remove();
        if (queue.isEmpty()) {
            dimensions.remove(dimension);
        }
        return next;
    }

    public void process(ResourceKey<Level> dimension, long budget, LongSupplier clock, Consumer<ChunkPos> generate) {
        long startTime = clock.getAsLong();
        while (clock.getAsLong() - startTime < budget) {
            ChunkPos next = poll(dimension);
            if (next == null) {
                return;
            }
            try {
                generate.accept(next);
            } catch (RuntimeException | Error failure) {
                add(dimension, next);
                throw failure;
            }
        }
    }
}
