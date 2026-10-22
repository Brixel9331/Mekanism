package mekanism.common.util.test;

import it.unimi.dsi.fastutil.longs.Long2BooleanArrayMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import mekanism.common.Mekanism;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.world.level.ChunkPos;

public class GameTestEventListeners implements ModInitializer {

    //TODO: Do we want to eventually try and make this work for multiple dimensions??
    //TODO: Do we want to make it so that we have some unique identifier for purposes of removing no longer watched chunks??
    // This would effectively make it a table, but currently we just make sure things that adjust chunks are in their own
    // game test batch
    //Note: We use an array map as we are unlikely to be watching a large number of chunks at any one time
    static final Long2BooleanMap watchedChunks = new Long2BooleanArrayMap();

    @Override
    public void onInitialize() {
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk) -> chunkLoadStateChange(chunk.getPos(), true));
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> chunkLoadStateChange(chunk.getPos(), false));
    }

    private static void chunkLoadStateChange(ChunkPos pos, boolean loaded) {
        long chunkPos = pos.toLong();
        //If we are watching the chunk and the loaded state isn't what we already had it as
        if (watchedChunks.getOrDefault(chunkPos, loaded) != loaded) {
            watchedChunks.put(chunkPos, loaded);
            if (GameTestUtils.DEBUG_CHUNK_LOADING) {
                Mekanism.logger.info("Chunk {}: {}, {}", loaded ? "loaded" : "unloaded", pos.x, pos.z);
            }
        } else if (GameTestUtils.DEBUG_CHUNK_LOADING && watchedChunks.containsKey(chunkPos)) {
            Mekanism.logger.info("Chunk was already {}: {}, {}", loaded ? "loaded" : "unloaded", pos.x, pos.z);
        }
    }
}
