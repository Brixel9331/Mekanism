package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import mekanism.api.fluid.FluidStack;
import net.minecraft.nbt.CompoundTag;

public final class CrTFluidUtils {

    private CrTFluidUtils() {
    }

    public static FluidStack fromCraftTweaker(IFluidStack stack) {
        if (stack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        long amount = stack.getAmount();
        if (amount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Mekanism fluid amounts cannot exceed " + Integer.MAX_VALUE + " mB: " + amount);
        }
        return new FluidStack(stack.getFluid(), (int) amount, stack.getInternalTag());
    }

    public static IFluidStack toCraftTweaker(FluidStack stack) {
        if (stack.isEmpty()) {
            return IFluidStack.empty();
        }
        CompoundTag tag = stack.getTag();
        return IFluidStack.of(stack.getFluid(), stack.getAmount(), tag == null ? null : tag.copy());
    }
}
