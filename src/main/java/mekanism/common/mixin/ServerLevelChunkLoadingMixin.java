package mekanism.common.mixin;

import java.util.function.BooleanSupplier;
import mekanism.common.lib.chunkloading.MekanismChunkManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelChunkLoadingMixin {

    @Shadow
    private int emptyTime;

    @Inject(method = "tick", at = @At("HEAD"))
    private void mekanism$keepForcedDimensionsActive(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (MekanismChunkManager.hasForcedChunks((ServerLevel) (Object) this)) {
            emptyTime = 0;
        }
    }
}
