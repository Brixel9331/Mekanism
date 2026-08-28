package mekanism.common.world;

import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

public final class RetrogenHandler {

    private static final long maximumDeltaTimeNanoSecs = 16_000_000; // 16 milliseconds

    private final RetrogenQueue queue = new RetrogenQueue();
    private final IntSupplier generationVersion;
    private final BooleanSupplier enabled;
    private final BiPredicate<ServerLevel, ChunkPos> generator;
    private boolean registered;

    public RetrogenHandler(IntSupplier generationVersion, BooleanSupplier enabled, BiPredicate<ServerLevel, ChunkPos> generator) {
        this.generationVersion = generationVersion;
        this.enabled = enabled;
        this.generator = generator;
    }

    public void register() {
        if (!registered) {
            ChunkGenerationData.setVersionSource(generationVersion);
            ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> onChunkLoad(world.dimension(), chunk));
            ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> queue.remove(world.dimension(), chunk.getPos()));
            ServerWorldEvents.UNLOAD.register((server, world) -> queue.remove(world.dimension()));
            ServerLifecycleEvents.SERVER_STOPPED.register(server -> reset());
            ServerTickEvents.END_WORLD_TICK.register(this::tick);
            registered = true;
        }
    }

    public void addRegenChunk(ResourceKey<Level> dimension, ChunkPos chunk) {
        queue.add(dimension, chunk);
    }

    public void reset() {
        queue.clear();
    }

    public void onChunkLoad(ResourceKey<Level> dimension, ChunkAccess chunk) {
        Integer version = ChunkGenerationData.getVersion(chunk);
        int current = generationVersion.getAsInt();
        if (version == null) {
            ChunkGenerationData.markGenerated(chunk, current);
        } else if (version < current && enabled.getAsBoolean()) {
            queue.add(dimension, chunk.getPos());
        }
    }

    private void tick(ServerLevel world) {
        tick(world.dimension(), pos -> world.getChunkSource().getChunkNow(pos.x, pos.z), pos -> generator.test(world, pos));
    }

    void tick(ResourceKey<Level> dimension, Function<ChunkPos, ? extends ChunkAccess> chunks, Predicate<ChunkPos> generate) {
        if (!enabled.getAsBoolean()) {
            return;
        }
        //Credit to E. Beef
        queue.process(dimension, maximumDeltaTimeNanoSecs, System::nanoTime, pos -> {
            //Ensure the chunk actually exists and is still loaded before trying to retrogen it
            ChunkAccess chunk = chunks.apply(pos);
            if (chunk != null) {
                if (generate.test(pos)) {
                    MekanismAPI.logger.info("Regenerating ores and salt at chunk {}", pos);
                }
                ChunkGenerationData.markGenerated(chunk, generationVersion.getAsInt());
            }
        });
    }
}
