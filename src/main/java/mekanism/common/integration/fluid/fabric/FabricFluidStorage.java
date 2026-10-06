package mekanism.common.integration.fluid.fabric;

import java.util.Objects;
import mekanism.api.AutomationType;
import mekanism.api.fluid.FluidAmounts;
import mekanism.api.fluid.FluidStack;
import mekanism.api.fluid.ITransactionalFluidTank;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class FabricFluidStorage implements SingleSlotStorage<FluidVariant> {

    private final ITransactionalFluidTank tank;

    public FabricFluidStorage(ITransactionalFluidTank tank) {
        this.tank = Objects.requireNonNull(tank);
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        int requested = FluidAmounts.toMilliBuckets(maxAmount);
        if (requested == 0) {
            return 0;
        }
        try (Transaction nested = transaction.openNested()) {
            FluidStack remainder = tank.insert(FluidStack.fromVariant(resource, requested), nested, AutomationType.EXTERNAL);
            if (remainder.getAmount() > requested || !remainder.isEmpty() && !resource.equals(remainder.getVariant())) {
                throw new IllegalStateException("Fluid tank returned an invalid insertion remainder");
            }
            long inserted = FluidAmounts.toFabric(requested - remainder.getAmount());
            if (inserted > 0) {
                nested.commit();
            }
            return inserted;
        }
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        int requested = FluidAmounts.toMilliBuckets(maxAmount);
        if (requested == 0 || !resource.equals(getResource())) {
            return 0;
        }
        try (Transaction nested = transaction.openNested()) {
            FluidStack extracted = tank.extract(requested, nested, AutomationType.EXTERNAL);
            if (extracted.isEmpty()) {
                return 0;
            }
            if (extracted.getAmount() > requested || !resource.equals(extracted.getVariant())) {
                throw new IllegalStateException("Fluid tank returned an invalid extraction");
            }
            nested.commit();
            return FluidAmounts.toFabric(extracted.getAmount());
        }
    }

    @Override
    public boolean isResourceBlank() {
        return tank.isEmpty();
    }

    @Override
    public FluidVariant getResource() {
        return tank.getFluid().getVariant();
    }

    @Override
    public long getAmount() {
        return FluidAmounts.toFabric(tank.getFluidAmount());
    }

    @Override
    public long getCapacity() {
        return FluidAmounts.toFabric(tank.getCapacity());
    }
}
