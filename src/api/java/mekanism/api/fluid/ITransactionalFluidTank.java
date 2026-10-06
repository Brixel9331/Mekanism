package mekanism.api.fluid;

import mekanism.api.AutomationType;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public interface ITransactionalFluidTank extends IExtendedFluidTank {

    FluidStack insert(FluidStack stack, TransactionContext transaction, AutomationType automationType);

    FluidStack extract(int amount, TransactionContext transaction, AutomationType automationType);
}
