package mekanism.common.integration.item.fabric;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import mekanism.api.inventory.IExtendedItemHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class FabricItemHandler implements IExtendedItemHandler {

    private final Storage<ItemVariant> storage;
    @Nullable
    private final SlottedStorage<ItemVariant> slotted;
    private List<ItemVariant> variants = List.of();

    public FabricItemHandler(Storage<ItemVariant> storage) {
        this.storage = Objects.requireNonNull(storage);
        slotted = storage instanceof SlottedStorage<ItemVariant> slots ? slots : null;
        getSlots();
    }

    @Override
    public int getSlots() {
        if (slotted != null) {
            return slotted.getSlotCount();
        }
        LinkedHashSet<ItemVariant> snapshot = new LinkedHashSet<>();
        storage.nonEmptyViews().forEach(view -> snapshot.add(view.getResource()));
        variants = List.copyOf(snapshot);
        return variants.size() + 1;
    }

    @Nullable
    private StorageView<ItemVariant> getView(int slot) {
        if (slot < 0) {
            return null;
        }
        return slotted != null && slot < slotted.getSlotCount() ? slotted.getSlot(slot) : null;
    }

    @Nullable
    private ItemVariant getVariant(int slot) {
        return slot >= 0 && slot < variants.size() ? variants.get(slot) : null;
    }

    private int getGenericAmount(int slot, boolean capacity) {
        ItemVariant variant = getVariant(slot);
        if (variant == null) {
            return 0;
        }
        long amount = 0;
        for (StorageView<ItemVariant> view : storage) {
            if (variant.equals(view.getResource())) {
                amount += Math.min(Integer.MAX_VALUE, capacity ? view.getCapacity() : view.getAmount());
                if (amount >= Integer.MAX_VALUE) {
                    return Integer.MAX_VALUE;
                }
            }
        }
        return (int) amount;
    }

    @Nullable
    private Storage<ItemVariant> insertionTarget(int slot) {
        if (slot < 0 || !storage.supportsInsertion()) {
            return null;
        }
        return slotted == null ? slot == variants.size() ? storage : null : slot < slotted.getSlotCount() ? slotted.getSlot(slot) : null;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slotted == null) {
            ItemVariant variant = getVariant(slot);
            return variant == null ? ItemStack.EMPTY : variant.toStack(getGenericAmount(slot, false));
        }
        StorageView<ItemVariant> view = getView(slot);
        return view == null || view.isResourceBlank() ? ItemStack.EMPTY : view.getResource().toStack((int) Math.min(Integer.MAX_VALUE, view.getAmount()));
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        throw new UnsupportedOperationException("Fabric storage contents must be changed through insertion or extraction");
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        Storage<ItemVariant> target = insertionTarget(slot);
        if (stack.isEmpty() || target == null) {
            return stack;
        }
        try (Transaction transaction = openTransaction(simulate)) {
            long inserted = target.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            checkAmount(inserted, stack.getCount());
            if (!simulate && inserted > 0) {
                transaction.commit();
            }
            return inserted == 0 ? stack : stack.copyWithCount(stack.getCount() - (int) inserted);
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0 || !storage.supportsExtraction()) {
            return ItemStack.EMPTY;
        }
        StorageView<ItemVariant> view = getView(slot);
        ItemVariant variant = slotted == null ? getVariant(slot) : view == null ? null : view.getResource();
        if (variant == null || variant.isBlank()) {
            return ItemStack.EMPTY;
        }
        int requested = Math.min(amount, variant.toStack().getMaxStackSize());
        try (Transaction transaction = openTransaction(simulate)) {
            long extracted = slotted == null ? storage.extract(variant, requested, transaction) : view.extract(variant, requested, transaction);
            checkAmount(extracted, requested);
            if (!simulate && extracted > 0) {
                transaction.commit();
            }
            return variant.toStack((int) extracted);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slotted == null) {
            return slot == variants.size() ? Integer.MAX_VALUE : getGenericAmount(slot, true);
        }
        StorageView<ItemVariant> view = getView(slot);
        return view == null ? 0 : (int) Math.min(Integer.MAX_VALUE, view.getCapacity());
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !stack.isEmpty() && insertItem(slot, stack.copyWithCount(1), true).isEmpty();
    }

    public InsertionSimulation simulateInsertions() {
        return new InsertionSimulation();
    }

    public final class InsertionSimulation implements AutoCloseable {

        private final Transaction transaction = openTransaction(true);

        private InsertionSimulation() {
        }

        public int insert(ItemStack stack) {
            if (stack.isEmpty() || !storage.supportsInsertion()) {
                return stack.getCount();
            }
            long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            checkAmount(inserted, stack.getCount());
            return stack.getCount() - (int) inserted;
        }

        @Override
        public void close() {
            transaction.close();
        }
    }

    private static Transaction openTransaction(boolean simulate) {
        return simulate ? Transaction.openNested(Transaction.getCurrentUnsafe()) : Transaction.openOuter();
    }

    private static void checkAmount(long transferred, int requested) {
        if (transferred < 0 || transferred > requested) {
            throw new IllegalStateException("Fabric item storage returned an invalid transfer amount");
        }
    }
}
