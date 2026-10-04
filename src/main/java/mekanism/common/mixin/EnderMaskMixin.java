package mekanism.common.mixin;

import mekanism.common.item.interfaces.IEquipmentProtection;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public abstract class EnderMaskMixin {

    @Inject(method = "isLookingAtMe", at = @At("HEAD"), cancellable = true)
    private void mekanism$helmetProtection(Player player, CallbackInfoReturnable<Boolean> callback) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof IEquipmentProtection protection && protection.isEnderMask(helmet, player, (EnderMan) (Object) this)) {
            callback.setReturnValue(false);
        }
    }
}
