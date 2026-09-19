package mekanism.common.mixin;

import mekanism.common.lib.BlockEntityLoadQueue;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class LevelBlockEntityLifecycleMixin implements BlockEntityLoadQueue.LevelAccess {

    @Unique
    private final BlockEntityLoadQueue mekanism$blockEntityLoadQueue = new BlockEntityLoadQueue();

    @Override
    public BlockEntityLoadQueue mekanism$getBlockEntityLoadQueue() {
        return mekanism$blockEntityLoadQueue;
    }

    @Inject(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private void mekanism$loadBlockEntities(CallbackInfo ci) {
        mekanism$blockEntityLoadQueue.runPending((Level) (Object) this);
    }
}
