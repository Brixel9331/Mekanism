package mekanism.common.util;

import mekanism.api.Action;
import mekanism.api.capabilities.CapabilityReference;
import mekanism.api.fluid.FluidStack;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.api.fluid.IExtendedFluidHandlerItem;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.world.item.ItemStack;

public final class FluidContainerUtils {

    private FluidContainerUtils() {
    }

    public static CapabilityReference<IExtendedFluidHandlerItem> getFluidHandler(ItemStack stack) {
        return Capabilities.FLUID_HANDLER_ITEM.find(stack, null);
    }

    public static int fill(IExtendedFluidHandler handler, FluidStack stack, Action action) {
        return stack.getAmount() - handler.insertFluid(stack, action).getAmount();
    }
}
