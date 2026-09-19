package mekanism.common.mixin.client;

import mekanism.common.tile.interfaces.IBlockEntityUpdateReceiver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelChunk.class)
public abstract class ClientChunkBlockEntityMixin {

    @Redirect(method = "method_31716", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;load(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void mekanism$receiveUpdateTag(BlockEntity blockEntity, CompoundTag tag) {
        if (blockEntity instanceof IBlockEntityUpdateReceiver receiver) {
            receiver.handleUpdateTag(tag);
        } else {
            blockEntity.load(tag);
        }
    }
}
