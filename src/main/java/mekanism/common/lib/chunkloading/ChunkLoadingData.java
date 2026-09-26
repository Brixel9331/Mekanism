package mekanism.common.lib.chunkloading;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import mekanism.common.lib.chunkloading.MekanismChunkManager.TicketOwner;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

final class ChunkLoadingData extends SavedData {

    final Map<TicketOwner, LongSet> tickets = new LinkedHashMap<>();
    private final Long2IntOpenHashMap tickingChunks = new Long2IntOpenHashMap();
    boolean restored;

    static ChunkLoadingData load(CompoundTag tag, Predicate<String> isLoaded) {
        ChunkLoadingData data = new ChunkLoadingData();
        ListTag entries = tag.getList("Tickets", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            String modId = entry.getString("Mod");
            if (isLoaded.test(modId) && entry.contains("Owner", Tag.TAG_COMPOUND)) {
                TicketOwner owner = new TicketOwner(modId, NbtUtils.readBlockPos(entry.getCompound("Owner")), entry.getBoolean("Ticking"));
                for (long chunk : entry.getLongArray("Chunks")) {
                    data.add(owner, chunk);
                }
            } else {
                data.setDirty();
            }
        }
        return data;
    }

    static ChunkLoadingData loadLegacy(CompoundTag tag, Predicate<String> acceptsMod) {
        ChunkLoadingData data = new ChunkLoadingData();
        ListTag entries = tag.getList("ForgeForced", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            String modId = entry.getString("Mod");
            if (acceptsMod.test(modId)) {
                ListTag chunks = entry.getList("ModForced", Tag.TAG_COMPOUND);
                for (int j = 0; j < chunks.size(); j++) {
                    CompoundTag chunk = chunks.getCompound(j);
                    data.loadLegacyOwners(modId, chunk, "Blocks", false);
                    data.loadLegacyOwners(modId, chunk, "TickingBlocks", true);
                }
            }
        }
        data.setDirty();
        return data;
    }

    private void loadLegacyOwners(String modId, CompoundTag chunk, String key, boolean ticking) {
        ListTag owners = chunk.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < owners.size(); i++) {
            add(new TicketOwner(modId, NbtUtils.readBlockPos(owners.getCompound(i)), ticking), chunk.getLong("Chunk"));
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        for (Map.Entry<TicketOwner, LongSet> ticket : tickets.entrySet()) {
            TicketOwner owner = ticket.getKey();
            CompoundTag entry = new CompoundTag();
            entry.putString("Mod", owner.modId());
            entry.put("Owner", NbtUtils.writeBlockPos(owner.pos()));
            entry.putBoolean("Ticking", owner.ticking());
            entry.putLongArray("Chunks", ticket.getValue().toLongArray());
            entries.add(entry);
        }
        tag.put("Tickets", entries);
        return tag;
    }

    boolean add(TicketOwner owner, long chunk) {
        if (!tickets.computeIfAbsent(owner, key -> new LongOpenHashSet()).add(chunk)) {
            return false;
        }
        if (owner.ticking()) {
            tickingChunks.addTo(chunk, 1);
        }
        return true;
    }

    boolean remove(TicketOwner owner, long chunk) {
        LongSet chunks = tickets.get(owner);
        if (chunks == null || !chunks.remove(chunk)) {
            return false;
        }
        if (chunks.isEmpty()) {
            tickets.remove(owner);
        }
        if (owner.ticking() && tickingChunks.addTo(chunk, -1) == 1) {
            tickingChunks.remove(chunk);
        }
        return true;
    }

    boolean isTicking(long chunk) {
        return tickingChunks.containsKey(chunk);
    }

    Map<BlockPos, Pair<LongSet, LongSet>> snapshot(String modId) {
        Map<BlockPos, Pair<LongSet, LongSet>> snapshot = new LinkedHashMap<>();
        for (Map.Entry<TicketOwner, LongSet> ticket : tickets.entrySet()) {
            TicketOwner owner = ticket.getKey();
            if (modId.equals(owner.modId())) {
                Pair<LongSet, LongSet> chunks = snapshot.computeIfAbsent(owner.pos(), key -> Pair.of(new LongOpenHashSet(), new LongOpenHashSet()));
                (owner.ticking() ? chunks.getSecond() : chunks.getFirst()).addAll(ticket.getValue());
            }
        }
        return Map.copyOf(snapshot);
    }
}
