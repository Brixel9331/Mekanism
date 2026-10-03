package mekanism.common.mixin;

import mekanism.common.item.interfaces.IItemStackProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackPropertiesMixin {

    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
    private void mekanism$stackLimit(CallbackInfoReturnable<Integer> callback) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof IItemStackProperties provider) {
            callback.setReturnValue(provider.getMaxStackSize(stack));
        }
    }

    @Inject(method = "getHideFlags", at = @At("RETURN"), cancellable = true)
    private void mekanism$tooltipFlags(CallbackInfoReturnable<Integer> callback) {
        ItemStack stack = (ItemStack) (Object) this;
        CompoundTag tag = stack.getTag();
        if ((tag == null || !tag.contains("HideFlags", Tag.TAG_ANY_NUMERIC)) && stack.getItem() instanceof IItemStackProperties provider) {
            callback.setReturnValue(provider.getDefaultTooltipHideFlags(stack));
        }
    }
}
