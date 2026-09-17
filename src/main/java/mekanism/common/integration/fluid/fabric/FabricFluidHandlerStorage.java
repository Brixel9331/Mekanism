package mekanism.common.integration.fluid.fabric;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import mekanism.api.capabilities.CapabilityProvider;
import mekanism.api.fluid.FluidAmounts;
import mekanism.api.fluid.FluidStack;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.ITransactionalFluidHandler;
import mekanism.common.capabilities.Capabilities;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class FabricFluidHandlerStorage implements SlottedStorage<FluidVariant> {

    private final Supplier<? extends IExtendedFluidHandler> handlers;

    public FabricFluidHandlerStorage(Supplier<? extends IExtendedFluidHandler> handlers) {
        this.handlers = Objects.requireNonNull(handlers);
    }

    @Nullable
    public static FabricFluidHandlerStorage create(@Nullable BlockEntity tile, @Nullable Direction side) {
        if (!(tile instanceof CapabilityProvider provider) || tile.isRemoved()) {
            return null;
        }
        FabricFluidHandlerStorage storage = new FabricFluidHandlerStorage(() -> tile.isRemoved() ? null
              : provider.getCapability(Capabilities.FLUID_HANDLER, side).orElse(null));
        return storage.resolveHandler() == null ? null : storage;
    }

    @Nullable
    private ITransactionalFluidHandler resolveHandler() {
        return handlers.get() instanceof ITransactionalFluidHandler handler ? handler : null;
    }

    @Nullable
    private static IExtendedFluidTank fluidTank(ITransactionalFluidHandler handler, int index) {
        return index < 0 || index >= handler.getTanks() ? null : handler.getFluidTank(index);
    }

    @Override
    public int getSlotCount() {
        ITransactionalFluidHandler handler = resolveHandler();
        return handler == null ? 0 : handler.getTanks();
    }

    @Override
    public SingleSlotStorage<FluidVariant> getSlot(int slot) {
        ITransactionalFluidHandler handler = resolveHandler();
        Objects.checkIndex(slot, handler == null ? 0 : handler.getTanks());
        return new TankView(slot, fluidTank(handler, slot));
    }

    private List<TankView> tankViews() {
        ITransactionalFluidHandler handler = resolveHandler();
        int count = handler == null ? 0 : handler.getTanks();
        List<TankView> tanks = new ArrayList<>(count);
        for (int tank = 0; tank < count; tank++) {
            tanks.add(new TankView(tank, fluidTank(handler, tank)));
        }
        return tanks;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return new ArrayList<StorageView<FluidVariant>>(tankViews()).iterator();
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (FluidAmounts.toMilliBuckets(maxAmount) == 0) {
            return 0;
        }
        List<TankView> tanks = tankViews();
        if (tanks.size() == 1) {
            return tanks.get(0).insert(resource, maxAmount, transaction);
        }
        long remaining = maxAmount;
        try (Transaction nested = transaction.openNested()) {
            for (int pass = 0; pass < 2 && remaining > 0; pass++) {
                for (TankView tank : tanks) {
                    if (pass == 0 ? resource.equals(tank.getResource()) : tank.isResourceBlank()) {
                        remaining -= tank.insert(resource, remaining, nested);
                        if (remaining == 0) {
                            break;
                        }
                    }
                }
            }
            if (remaining < maxAmount) {
                nested.commit();
            }
        }
        return maxAmount - remaining;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (FluidAmounts.toMilliBuckets(maxAmount) == 0) {
            return 0;
        }
        long remaining = maxAmount;
        try (Transaction nested = transaction.openNested()) {
            for (TankView tank : tankViews()) {
                remaining -= tank.extract(resource, remaining, nested);
                if (remaining == 0) {
                    break;
                }
            }
            if (remaining < maxAmount) {
                nested.commit();
            }
        }
        return maxAmount - remaining;
    }

    private static int checkedAmount(FluidStack stack, FluidVariant resource, int requested) {
        if (stack.getAmount() < 0 || stack.getAmount() > requested || !stack.isEmpty() && !resource.equals(stack.getVariant())) {
            throw new IllegalStateException("Fluid handler returned an invalid transfer stack");
        }
        return stack.getAmount();
    }

    private final class TankView implements SingleSlotStorage<FluidVariant> {

        private final int index;
        @Nullable
        private final IExtendedFluidTank tank;

        private TankView(int index, @Nullable IExtendedFluidTank tank) {
            this.index = index;
            this.tank = tank;
        }

        @Nullable
        private ITransactionalFluidHandler resolve() {
            ITransactionalFluidHandler handler = resolveHandler();
            return handler != null && tank != null && fluidTank(handler, index) == tank ? handler : null;
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            int requested = FluidAmounts.toMilliBuckets(maxAmount);
            ITransactionalFluidHandler handler = resolve();
            if (requested == 0 || handler == null) {
                return 0;
            }
            try (Transaction nested = transaction.openNested()) {
                int inserted = requested - checkedAmount(handler.insertFluid(index, FluidStack.fromVariant(resource, requested), nested), resource, requested);
                if (inserted > 0) {
                    nested.commit();
                }
                return FluidAmounts.toFabric(inserted);
            }
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            int requested = FluidAmounts.toMilliBuckets(maxAmount);
            ITransactionalFluidHandler handler = resolve();
            if (requested == 0 || handler == null || !resource.equals(handler.getFluidInTank(index).getVariant())) {
                return 0;
            }
            try (Transaction nested = transaction.openNested()) {
                int extracted = checkedAmount(handler.extractFluid(index, requested, nested), resource, requested);
                if (extracted > 0) {
                    nested.commit();
                }
                return FluidAmounts.toFabric(extracted);
            }
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public FluidVariant getResource() {
            ITransactionalFluidHandler handler = resolve();
            return handler == null ? FluidVariant.blank() : handler.getFluidInTank(index).getVariant();
        }

        @Override
        public long getAmount() {
            ITransactionalFluidHandler handler = resolve();
            return handler == null ? 0 : FluidAmounts.toFabric(handler.getFluidInTank(index).getAmount());
        }

        @Override
        public long getCapacity() {
            ITransactionalFluidHandler handler = resolve();
            return handler == null ? 0 : FluidAmounts.toFabric(Math.min(handler.getTankCapacity(index), tank.getCapacity()));
        }
    }
}
