package mekanism.common.world;

import java.util.Objects;
import java.util.function.IntSupplier;
import mekanism.api.NBTConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import org.jetbrains.annotations.Nullable;

public final class ChunkGenerationData {

    private static IntSupplier currentVersion = () -> 0;

    private ChunkGenerationData() {
    }

    public static void setVersionSource(IntSupplier versionSource) {
        currentVersion = Objects.requireNonNull(versionSource);
    }

    private static ChunkWorldgenState state(ChunkAccess chunk) {
        return (ChunkWorldgenState) (chunk instanceof ImposterProtoChunk imposter ? imposter.getWrapped() : chunk);
    }

    @Nullable
    public static Integer getVersion(ChunkAccess chunk) {
        return state(chunk).mekanism$getGenerationVersion();
    }

    public static void read(ChunkAccess chunk, CompoundTag data) {
        state(chunk).mekanism$setGenerationVersion(data.getInt(NBTConstants.WORLD_GEN_VERSION));
    }

    public static void write(ChunkAccess chunk, CompoundTag data) {
        Integer version = getVersion(chunk);
        data.putInt(NBTConstants.WORLD_GEN_VERSION, version == null ? currentVersion.getAsInt() : version);
    }

    public static void copy(ChunkAccess source, ChunkAccess target) {
        state(target).mekanism$setGenerationVersion(getVersion(source));
    }

    public static void markGenerated(ChunkAccess chunk, int version) {
        state(chunk).mekanism$setGenerationVersion(version);
        chunk.setUnsaved(true);
    }
}
