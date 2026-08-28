package mekanism.common.mixin;

import mekanism.common.world.ChunkGenerationData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public abstract class LevelChunkGenerationMixin {

    @Inject(method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V",
          at = @At("RETURN"))
    private void mekanism$copyGenerationVersion(ServerLevel world, ProtoChunk proto, LevelChunk.PostLoadProcessor processor, CallbackInfo ci) {
        ChunkGenerationData.copy(proto, (LevelChunk) (Object) this);
    }
}
