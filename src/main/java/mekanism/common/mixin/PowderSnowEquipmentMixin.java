package mekanism.common.mixin;

import mekanism.common.item.interfaces.IEquipmentProtection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBlock.class)
public abstract class PowderSnowEquipmentMixin {

    @Inject(method = "canEntityWalkOnPowderSnow", at = @At("RETURN"), cancellable = true)
    private static void mekanism$snowProtection(Entity entity, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValueZ() && entity instanceof LivingEntity living) {
            ItemStack boots = living.getItemBySlot(EquipmentSlot.FEET);
            if (boots.getItem() instanceof IEquipmentProtection protection && protection.canWalkOnPowderedSnow(boots, living)) {
                callback.setReturnValue(true);
            }
        }
    }
}
