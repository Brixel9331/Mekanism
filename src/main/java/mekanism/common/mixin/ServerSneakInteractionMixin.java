package mekanism.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mekanism.common.item.interfaces.ISneakBypassUseItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerSneakInteractionMixin {

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSecondaryUseActive()Z"))
    private boolean mekanism$bypassSneaking(ServerPlayer player, Operation<Boolean> original, ServerPlayer user, Level level, ItemStack stack,
          InteractionHand hand, BlockHitResult hit) {
        return original.call(player) && !(ISneakBypassUseItem.bypasses(player.getMainHandItem(), level, hit.getBlockPos(), player) &&
              ISneakBypassUseItem.bypasses(player.getOffhandItem(), level, hit.getBlockPos(), player));
    }
}
