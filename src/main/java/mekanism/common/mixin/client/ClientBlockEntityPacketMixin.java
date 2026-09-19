package mekanism.common.mixin.client;

import mekanism.common.tile.interfaces.IBlockEntityUpdateReceiver;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public abstract class ClientBlockEntityPacketMixin {

    @Redirect(method = "method_38542", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;load(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void mekanism$receiveUpdatePacket(BlockEntity blockEntity, CompoundTag tag) {
        if (blockEntity instanceof IBlockEntityUpdateReceiver receiver) {
            receiver.handleUpdatePacket(tag);
        } else {
            blockEntity.load(tag);
        }
    }
}
