package mekanism.common.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import mekanism.common.lib.chunkloading.MekanismChunkManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheChunkLoadingMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @ModifyExpressionValue(method = "tickChunks", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/server/level/ChunkMap;anyPlayerCloseEnoughForSpawning(Lnet/minecraft/world/level/ChunkPos;)Z"))
    private boolean mekanism$tickForcedChunks(boolean original, @Local LevelChunk chunk) {
        return original || MekanismChunkManager.shouldForceTicks(level, chunk.getPos().toLong());
    }

    @ModifyExpressionValue(method = "tickChunks", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/server/level/ServerLevel;isNaturalSpawningAllowed(Lnet/minecraft/world/level/ChunkPos;)Z"))
    private boolean mekanism$allowForcedChunkSpawning(boolean original, @Local LevelChunk chunk) {
        return original || MekanismChunkManager.shouldForceTicks(level, chunk.getPos().toLong());
    }
}
