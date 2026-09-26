package mekanism.common.lib.chunkloading;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public final class MekanismChunkManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DATA_NAME = "mekanism_chunk_loading";
    private static final TicketType<TicketOwner> TICKET = TicketType.create("mekanism:chunk_loader",
          Comparator.comparing(TicketOwner::modId).thenComparing(TicketOwner::pos).thenComparing(TicketOwner::ticking));
    private static final Map<String, LoadingValidationCallback> CALLBACKS = new LinkedHashMap<>();
    private static final Map<ServerLevel, ChunkLoadingData> ACTIVE = new IdentityHashMap<>();
    private static boolean registered;

    private MekanismChunkManager() {
    }

    public static void setForcedChunkLoadingCallback(String modId, LoadingValidationCallback callback) {
        if (!FabricLoader.getInstance().isModLoaded(modId)) {
            throw new IllegalArgumentException("Cannot register chunk loading for an unloaded mod: " + modId);
        }
        CALLBACKS.put(modId, Objects.requireNonNull(callback));
        if (!registered) {
            registered = true;
            ServerLifecycleEvents.SERVER_STARTED.register(server -> server.getAllLevels().forEach(MekanismChunkManager::restore));
            ServerTickEvents.START_WORLD_TICK.register(MekanismChunkManager::restore);
            ServerWorldEvents.UNLOAD.register((server, world) -> ACTIVE.remove(world));
            ServerLifecycleEvents.SERVER_STOPPED.register(server -> ACTIVE.clear());
        }
    }

    public static boolean forceChunk(ServerLevel world, String modId, BlockPos owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        if (!CALLBACKS.containsKey(modId)) {
            throw new IllegalArgumentException("No chunk loading callback registered for: " + modId);
        }
        ChunkLoadingData data = data(world);
        TicketOwner ticket = new TicketOwner(modId, owner.immutable(), ticking);
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        if (!add) {
            return remove(world, data, ticket, pos.toLong());
        }
        if (!data.add(ticket, pos.toLong())) {
            return false;
        }
        data.setDirty();
        world.getChunkSource().addRegionTicket(TICKET, pos, 2, ticket);
        try {
            world.getChunk(chunkX, chunkZ);
        } catch (RuntimeException | Error failure) {
            remove(world, data, ticket, pos.toLong());
            throw failure;
        }
        return true;
    }

    private static boolean remove(ServerLevel world, ChunkLoadingData data, TicketOwner owner, long chunk) {
        if (!data.remove(owner, chunk)) {
            return false;
        }
        data.setDirty();
        world.getChunkSource().removeRegionTicket(TICKET, new ChunkPos(chunk), 2, owner);
        return true;
    }

    public static boolean hasForcedChunks(ServerLevel world) {
        ChunkLoadingData data = ACTIVE.get(world);
        return data != null && !data.tickets.isEmpty();
    }

    public static boolean shouldForceTicks(ServerLevel world, long chunk) {
        ChunkLoadingData data = ACTIVE.get(world);
        return data != null && data.isTicking(chunk);
    }

    private static ChunkLoadingData data(ServerLevel world) {
        return ACTIVE.computeIfAbsent(world, level -> level.getDataStorage().computeIfAbsent(
              tag -> ChunkLoadingData.load(tag, CALLBACKS::containsKey), () -> importLegacy(level), DATA_NAME));
    }

    private static ChunkLoadingData importLegacy(ServerLevel world) {
        CompoundTag tag;
        try {
            tag = world.getDataStorage().readTagFromDisk("chunks", SharedConstants.getCurrentVersion().getDataVersion().getVersion()).getCompound("data");
        } catch (FileNotFoundException missing) {
            tag = new CompoundTag();
        } catch (IOException failure) {
            throw new UncheckedIOException("Unable to import legacy chunk tickets in " + world.dimension().location(), failure);
        }
        ChunkLoadingData data = ChunkLoadingData.loadLegacy(tag, CALLBACKS::containsKey);
        if (!data.tickets.isEmpty()) {
            LOGGER.info("Imported {} chunk loader owners in {}", data.tickets.size(), world.dimension().location());
        }
        return data;
    }

    private static void restore(ServerLevel world) {
        ChunkLoadingData data = data(world);
        if (data.restored) {
            return;
        }
        data.restored = true;
        for (Map.Entry<String, LoadingValidationCallback> entry : CALLBACKS.entrySet()) {
            Map<BlockPos, Pair<LongSet, LongSet>> tickets = data.snapshot(entry.getKey());
            if (!tickets.isEmpty()) {
                entry.getValue().validateTickets(world, new TicketHelper(world, data, entry.getKey(), tickets));
            }
        }
        for (Map.Entry<TicketOwner, LongSet> entry : data.tickets.entrySet()) {
            for (long chunk : entry.getValue()) {
                world.getChunkSource().addRegionTicket(TICKET, new ChunkPos(chunk), 2, entry.getKey());
            }
        }
    }

    record TicketOwner(String modId, BlockPos pos, boolean ticking) {

        TicketOwner {
            Objects.requireNonNull(modId);
            pos = pos.immutable();
        }
    }

    @FunctionalInterface
    public interface LoadingValidationCallback {

        void validateTickets(ServerLevel world, TicketHelper ticketHelper);
    }

    public static final class TicketHelper {

        private final ServerLevel world;
        private final ChunkLoadingData data;
        private final String modId;
        private final Map<BlockPos, Pair<LongSet, LongSet>> blockTickets;

        private TicketHelper(ServerLevel world, ChunkLoadingData data, String modId, Map<BlockPos, Pair<LongSet, LongSet>> blockTickets) {
            this.world = world;
            this.data = data;
            this.modId = modId;
            this.blockTickets = blockTickets;
        }

        public Map<BlockPos, Pair<LongSet, LongSet>> getBlockTickets() {
            return blockTickets;
        }

        public void removeTicket(BlockPos owner, long chunk, boolean ticking) {
            remove(world, data, new TicketOwner(modId, owner, ticking), chunk);
        }

        public void removeAllTickets(BlockPos owner) {
            for (TicketOwner ticket : new ArrayList<>(data.tickets.keySet())) {
                if (ticket.modId().equals(modId) && ticket.pos().equals(owner)) {
                    for (long chunk : data.tickets.get(ticket).toLongArray()) {
                        remove(world, data, ticket, chunk);
                    }
                }
            }
        }
    }
}
