package mekanism.common.mixin;

import mekanism.common.world.ChunkWorldgenState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChunkAccess.class)
public abstract class ChunkWorldgenStateMixin implements ChunkWorldgenState {

    @Unique
    @Nullable
    private volatile Integer mekanism$generationVersion;

    @Override
    @Nullable
    public Integer mekanism$getGenerationVersion() {
        return mekanism$generationVersion;
    }

    @Override
    public void mekanism$setGenerationVersion(@Nullable Integer version) {
        mekanism$generationVersion = version;
    }
}
