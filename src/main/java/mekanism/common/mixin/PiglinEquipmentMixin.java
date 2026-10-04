package mekanism.common.mixin;

import mekanism.common.item.interfaces.IEquipmentProtection;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinAi.class)
public abstract class PiglinEquipmentMixin {

    @Inject(method = "isWearingGold", at = @At("RETURN"), cancellable = true)
    private static void mekanism$armorProtection(LivingEntity entity, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValueZ()) {
            for (ItemStack stack : entity.getArmorSlots()) {
                if (stack.getItem() instanceof IEquipmentProtection protection && protection.makesPiglinsNeutral(stack, entity)) {
                    callback.setReturnValue(true);
                    return;
                }
            }
        }
    }
}
