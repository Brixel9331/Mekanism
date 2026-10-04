package mekanism.common.mixin;

import mekanism.common.item.interfaces.IPiglinCurrency;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinAi.class)
public abstract class PiglinCurrencyMixin {

    @Inject(method = "isBarterCurrency", at = @At("RETURN"), cancellable = true)
    private static void mekanism$barterCurrency(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValueZ() && stack.getItem() instanceof IPiglinCurrency item && item.isPiglinCurrency(stack)) {
            callback.setReturnValue(true);
        }
    }
}
