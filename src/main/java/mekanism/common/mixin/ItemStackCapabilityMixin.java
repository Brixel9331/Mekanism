package mekanism.common.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mekanism.api.capabilities.Capability;
import mekanism.api.capabilities.CapabilityProvider;
import mekanism.api.capabilities.CapabilityReference;
import mekanism.api.capabilities.ItemCapabilityProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackCapabilityMixin implements CapabilityProvider {

    @Unique
    @Nullable
    private CapabilityProvider mekanism$provider;

    @Inject(method = "setCount", at = @At("RETURN"))
    private void mekanism$invalidateEmptyStack(int count, CallbackInfo callback) {
        if (((ItemStack) (Object) this).isEmpty()) {
            invalidateCapabilities();
        }
    }

    @WrapMethod(method = "setTag")
    private void mekanism$invalidateReplacedTag(@Nullable CompoundTag tag, Operation<Void> original) {
        CompoundTag previous = ((ItemStack) (Object) this).getTag();
        boolean replaced = previous != tag && (previous != null || tag != null && !tag.isEmpty());
        original.call(tag);
        if (replaced) {
            invalidateCapabilities();
        }
    }

    @Override
    public void invalidateCapabilities() {
        CapabilityProvider provider = mekanism$provider;
        mekanism$provider = null;
        if (provider != null) {
            provider.invalidateCapabilities();
        }
    }

    @Override
    public <T> CapabilityReference<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.isEmpty()) {
            return CapabilityReference.empty();
        }
        if (mekanism$provider == null && stack.getItem() instanceof ItemCapabilityProvider item) {
            mekanism$provider = item.initCapabilities(stack, stack.getTag());
        }
        return mekanism$provider == null ? CapabilityReference.empty() : mekanism$provider.getCapability(capability, side);
    }
}
