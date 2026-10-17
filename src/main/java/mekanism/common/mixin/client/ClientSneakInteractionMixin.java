package mekanism.common.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mekanism.common.item.interfaces.ISneakBypassUseItem;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientSneakInteractionMixin {

    @WrapOperation(method = "performUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSecondaryUseActive()Z"))
    private boolean mekanism$bypassSneaking(LocalPlayer player, Operation<Boolean> original, LocalPlayer user, InteractionHand hand, BlockHitResult hit) {
        return original.call(player) && !(ISneakBypassUseItem.bypasses(player.getMainHandItem(), player.level(), hit.getBlockPos(), player) &&
              ISneakBypassUseItem.bypasses(player.getOffhandItem(), player.level(), hit.getBlockPos(), player));
    }
}
