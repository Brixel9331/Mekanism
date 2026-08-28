package mekanism.common.world;

import org.jetbrains.annotations.Nullable;

public interface ChunkWorldgenState {

    @Nullable
    Integer mekanism$getGenerationVersion();

    void mekanism$setGenerationVersion(@Nullable Integer version);
}
