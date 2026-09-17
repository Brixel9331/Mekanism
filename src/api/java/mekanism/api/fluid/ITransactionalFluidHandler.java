package mekanism.api.fluid;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public interface ITransactionalFluidHandler extends IExtendedFluidHandler {

    @Nullable
    IExtendedFluidTank getFluidTank(int tank);

    default FluidStack insertFluid(int tank, FluidStack stack, TransactionContext transaction) {
        if (stack.isEmpty() || !(getFluidTank(tank) instanceof ITransactionalFluidTank transactional)) {
            return stack;
        }
        int requested = stack.getAmount();
        int allowed = requested - checkedAmount(insertFluid(tank, stack, Action.SIMULATE), stack, requested);
        if (allowed == 0) {
            return stack;
        }
        try (Transaction nested = transaction.openNested()) {
            int inserted = allowed - checkedAmount(transactional.insert(new FluidStack(stack, allowed), nested, AutomationType.EXTERNAL), stack, allowed);
            if (inserted > 0) {
                FluidStack transferred = new FluidStack(stack, inserted);
                afterCommit(nested, () -> onFluidInserted(transferred));
                nested.commit();
            }
            return new FluidStack(stack, requested - inserted);
        }
    }

    default FluidStack extractFluid(int tank, int amount, TransactionContext transaction) {
        if (amount <= 0 || !(getFluidTank(tank) instanceof ITransactionalFluidTank transactional)) {
            return FluidStack.EMPTY;
        }
        FluidStack stored = getFluidInTank(tank);
        if (stored.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int allowed = checkedAmount(extractFluid(tank, amount, Action.SIMULATE), stored, amount);
        if (allowed == 0) {
            return FluidStack.EMPTY;
        }
        FluidStack expected = stored.copy();
        try (Transaction nested = transaction.openNested()) {
            FluidStack extracted = transactional.extract(allowed, nested, AutomationType.EXTERNAL);
            if (checkedAmount(extracted, expected, allowed) > 0) {
                nested.commit();
            }
            return extracted;
        }
    }

    default void onFluidInserted(FluidStack stack) {
    }

    private static int checkedAmount(FluidStack stack, FluidStack expected, int requested) {
        if (stack.getAmount() < 0 || stack.getAmount() > requested || !stack.isEmpty() && !stack.isFluidEqual(expected)) {
            throw new IllegalStateException("Fluid handler returned an invalid transfer stack");
        }
        return stack.getAmount();
    }

    private static void afterCommit(TransactionContext transaction, Runnable action) {
        transaction.addCloseCallback(new TransactionContext.CloseCallback() {
            @Override
            public void onClose(TransactionContext closed, TransactionContext.Result result) {
                if (result.wasCommitted()) {
                    if (closed.nestingDepth() == 0) {
                        closed.addOuterCloseCallback(outerResult -> action.run());
                    } else {
                        closed.getOpenTransaction(closed.nestingDepth() - 1).addCloseCallback(this);
                    }
                }
            }
        });
    }
}
