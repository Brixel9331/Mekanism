package mekanism.common.mixin;

import mekanism.common.world.ChunkGenerationData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializerMixin {

    @Inject(method = "read", at = @At("RETURN"))
    private static void mekanism$readGenerationVersion(ServerLevel world, PoiManager poi, ChunkPos pos, CompoundTag data,
          CallbackInfoReturnable<ProtoChunk> cir) {
        ChunkGenerationData.read(cir.getReturnValue(), data);
    }

    @Inject(method = "write", at = @At("RETURN"))
    private static void mekanism$writeGenerationVersion(ServerLevel world, ChunkAccess chunk, CallbackInfoReturnable<CompoundTag> cir) {
        ChunkGenerationData.write(chunk, cir.getReturnValue());
    }
}
