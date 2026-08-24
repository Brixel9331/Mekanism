package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.Action;
import mekanism.api.fluid.FluidStack;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import org.jetbrains.annotations.NotNull;

public class FluidHandlerTarget extends Target<IExtendedFluidHandler, Integer, @NotNull FluidStack> {

    public FluidHandlerTarget(@NotNull FluidStack type) {
        this.extra = type;
    }

    public FluidHandlerTarget(@NotNull FluidStack type, Collection<IExtendedFluidHandler> allHandlers) {
        super(allHandlers);
        this.extra = type;
    }

    public FluidHandlerTarget(@NotNull FluidStack type, int expectedSize) {
        super(expectedSize);
        this.extra = type;
    }

    @Override
    protected void acceptAmount(IExtendedFluidHandler handler, SplitInfo<Integer> splitInfo, Integer amount) {
        splitInfo.send(amount - handler.insertFluid(new FluidStack(extra, amount), Action.EXECUTE).getAmount());
    }

    @Override
    protected Integer simulate(IExtendedFluidHandler handler, @NotNull FluidStack fluidStack) {
        return fluidStack.getAmount() - handler.insertFluid(fluidStack, Action.SIMULATE).getAmount();
    }
}