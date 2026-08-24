package mekanism.common.integration.fluid.fabric;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import mekanism.api.Action;
import mekanism.api.fluid.FluidAmounts;
import mekanism.api.fluid.FluidStack;
import mekanism.api.fluid.IExtendedFluidHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public final class FabricFluidHandler implements IExtendedFluidHandler {

    private static final long MAX_FABRIC_AMOUNT = FluidAmounts.toFabric(Integer.MAX_VALUE);

    private final Storage<FluidVariant> storage;
    @Nullable
    private final SlottedStorage<FluidVariant> slotted;
    private List<FluidVariant> variants = List.of();

    public FabricFluidHandler(Storage<FluidVariant> storage) {
        this.storage = Objects.requireNonNull(storage);
        slotted = storage instanceof SlottedStorage<FluidVariant> tanks ? tanks : null;
        getTanks();
    }

    private List<FluidVariant> storedVariants() {
        LinkedHashSet<FluidVariant> snapshot = new LinkedHashSet<>();
        storage.nonEmptyViews().forEach(view -> snapshot.add(view.getResource()));
        return List.copyOf(snapshot);
    }

    @Override
    public int getTanks() {
        if (slotted != null) {
            return slotted.getSlotCount();
        }
        variants = storedVariants();
        return variants.size() + 1;
    }

    @Nullable
    private StorageView<FluidVariant> getView(int tank) {
        return slotted != null && tank >= 0 && tank < slotted.getSlotCount() ? slotted.getSlot(tank) : null;
    }

    @Nullable
    private FluidVariant getVariant(int tank) {
        return tank >= 0 && tank < variants.size() ? variants.get(tank) : null;
    }

    private int getGenericAmount(int tank, boolean capacity) {
        FluidVariant variant = getVariant(tank);
        if (variant == null) {
            return 0;
        }
        long amount = 0;
        for (StorageView<FluidVariant> view : storage) {
            if (variant.equals(view.getResource())) {
                amount += Math.min(MAX_FABRIC_AMOUNT, capacity ? view.getCapacity() : view.getAmount());
                if (amount >= MAX_FABRIC_AMOUNT) {
                    return Integer.MAX_VALUE;
                }
            }
        }
        return FluidAmounts.toMilliBuckets(amount);
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (slotted == null) {
            FluidVariant variant = getVariant(tank);
            return variant == null ? FluidStack.EMPTY : FluidStack.fromVariant(variant, getGenericAmount(tank, false));
        }
        StorageView<FluidVariant> view = getView(tank);
        return view == null ? FluidStack.EMPTY : FluidStack.fromVariant(view.getResource(), FluidAmounts.toMilliBuckets(view.getAmount()));
    }

    @Override
    public int getTankCapacity(int tank) {
        if (slotted == null) {
            return tank == variants.size() ? Integer.MAX_VALUE : getGenericAmount(tank, true);
        }
        StorageView<FluidVariant> view = getView(tank);
        return view == null ? 0 : FluidAmounts.toMilliBuckets(view.getCapacity());
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return !stack.isEmpty() && insertFluid(tank, new FluidStack(stack, 1), Action.SIMULATE).isEmpty();
    }

    @Override
    public void setFluidInTank(int tank, FluidStack stack) {
        throw new UnsupportedOperationException("Fabric storage contents must be changed through insertion or extraction");
    }

    @Override
    public FluidStack insertFluid(int tank, FluidStack stack, Action action) {
        Storage<FluidVariant> target = slotted == null ? tank == variants.size() ? storage : null :
              tank >= 0 && tank < slotted.getSlotCount() ? slotted.getSlot(tank) : null;
        return target == null ? stack : insert(target, stack, action);
    }

    @Override
    public FluidStack insertFluid(FluidStack stack, Action action) {
        return insert(storage, stack, action);
    }

    private FluidStack insert(Storage<FluidVariant> target, FluidStack stack, Action action) {
        if (stack.isEmpty() || !storage.supportsInsertion()) {
            return stack;
        }
        int inserted = transfer(stack.getAmount(), action, (amount, transaction) -> target.insert(stack.getVariant(), amount, transaction));
        return inserted == 0 ? stack : inserted == stack.getAmount() ? FluidStack.EMPTY : new FluidStack(stack, stack.getAmount() - inserted);
    }

    @Override
    public FluidStack extractFluid(int tank, int amount, Action action) {
        StorageView<FluidVariant> view = getView(tank);
        FluidVariant variant = slotted == null ? getVariant(tank) : view == null ? null : view.getResource();
        if (variant == null || variant.isBlank() || amount <= 0 || !storage.supportsExtraction()) {
            return FluidStack.EMPTY;
        }
        int extracted = transfer(amount, action, (requested, transaction) -> slotted == null ?
              storage.extract(variant, requested, transaction) : view.extract(variant, requested, transaction));
        return FluidStack.fromVariant(variant, extracted);
    }

    @Override
    public FluidStack extractFluid(FluidStack stack, Action action) {
        return stack.isEmpty() ? FluidStack.EMPTY : extract(stack.getVariant(), stack.getAmount(), action);
    }

    @Override
    public FluidStack extractFluid(int amount, Action action) {
        if (amount <= 0 || !storage.supportsExtraction()) {
            return FluidStack.EMPTY;
        }
        for (FluidVariant variant : storedVariants()) {
            FluidStack extracted = extract(variant, amount, action);
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }
        return FluidStack.EMPTY;
    }

    private FluidStack extract(FluidVariant variant, int amount, Action action) {
        if (amount <= 0 || !storage.supportsExtraction()) {
            return FluidStack.EMPTY;
        }
        return FluidStack.fromVariant(variant, transfer(amount, action, (requested, transaction) -> storage.extract(variant, requested, transaction)));
    }

    private static int transfer(int milliBuckets, Action action, FluidTransfer operation) {
        try (Transaction transaction = action.execute() ? Transaction.openOuter() : Transaction.openNested(Transaction.getCurrentUnsafe())) {
            long requested = FluidAmounts.toFabric(milliBuckets);
            long transferred;
            try (Transaction probe = transaction.openNested()) {
                transferred = operation.transfer(requested, probe);
                checkAmount(transferred, requested);
                if (transferred % FluidAmounts.FABRIC_UNITS_PER_MILLI_BUCKET == 0) {
                    probe.commit();
                }
            }
            if (transferred % FluidAmounts.FABRIC_UNITS_PER_MILLI_BUCKET != 0) {
                long aligned = FluidAmounts.toFabric(FluidAmounts.toMilliBuckets(transferred));
                if (aligned == 0) {
                    return 0;
                }
                long replayed = operation.transfer(aligned, transaction);
                checkAmount(replayed, aligned);
                if (replayed != aligned) {
                    return 0;
                }
                transferred = aligned;
            }
            if (action.execute() && transferred > 0) {
                transaction.commit();
            }
            return FluidAmounts.toMilliBuckets(transferred);
        }
    }

    private static void checkAmount(long transferred, long requested) {
        if (transferred < 0 || transferred > requested) {
            throw new IllegalStateException("Fabric fluid storage returned an invalid transfer amount");
        }
    }

    @FunctionalInterface
    private interface FluidTransfer {

        long transfer(long amount, TransactionContext transaction);
    }
}
