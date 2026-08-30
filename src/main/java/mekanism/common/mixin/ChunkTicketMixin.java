package mekanism.common.mixin;

import mekanism.common.lib.ChunkTicketCallback;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkMap.class)
public abstract class ChunkTicketMixin {

    @Shadow @Final
    private ServerLevel level;

    @Inject(method = "updateChunkScheduling", at = @At("RETURN"))
    private void mekanism$ticketChanged(long chunk, int newLevel, ChunkHolder holder, int oldLevel, CallbackInfoReturnable<ChunkHolder> cir) {
        if (oldLevel != newLevel) {
            ChunkTicketCallback.EVENT.invoker().changed(level, chunk, oldLevel, newLevel);
        }
    }
}
