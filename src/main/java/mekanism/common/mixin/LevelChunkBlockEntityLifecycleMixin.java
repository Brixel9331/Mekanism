package mekanism.common.mixin;

import java.util.List;
import mekanism.common.tile.interfaces.IBlockEntityLifecycle;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public abstract class LevelChunkBlockEntityLifecycleMixin {

    @Inject(method = "clearAllBlockEntities", at = @At("HEAD"))
    private void mekanism$unloadBlockEntities(CallbackInfo ci) {
        for (BlockEntity blockEntity : List.copyOf(((LevelChunk) (Object) this).getBlockEntities().values())) {
            if (!blockEntity.isRemoved() && blockEntity instanceof IBlockEntityLifecycle lifecycle) {
                lifecycle.onChunkUnloaded();
            }
        }
    }
}
