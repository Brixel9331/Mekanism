package mekanism.common.integration.fluid.fabric;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.fluid.FluidAmounts;
import mekanism.api.fluid.FluidStack;
import mekanism.api.fluid.IExtendedFluidHandlerItem;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class FabricItemFluidHandler implements IExtendedFluidHandlerItem {

    private final ContainerSlot slot;
    private final ContainerItemContext context;

    private FabricItemFluidHandler(ItemStack stack) {
        slot = new ContainerSlot(stack);
        context = ContainerItemContext.ofSingleSlot(slot);
    }

    @Nullable
    public static FabricItemFluidHandler create(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        FabricItemFluidHandler handler = new FabricItemFluidHandler(stack);
        return handler.context.find(FluidStorage.ITEM) == null ? null : handler;
    }

    private Storage<FluidVariant> storage() {
        Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
        return storage == null ? Storage.empty() : storage;
    }

    private static List<StorageView<FluidVariant>> views(Storage<FluidVariant> storage) {
        List<StorageView<FluidVariant>> views = new ArrayList<>();
        storage.forEach(views::add);
        return views;
    }

    @Nullable
    private static StorageView<FluidVariant> view(Storage<FluidVariant> storage, int tank) {
        if (tank < 0) {
            return null;
        }
        if (storage instanceof SlottedStorage<FluidVariant> slotted) {
            return tank < slotted.getSlotCount() ? slotted.getSlot(tank) : null;
        }
        List<StorageView<FluidVariant>> views = views(storage);
        return tank < views.size() ? views.get(tank) : null;
    }

    @Override
    public int getTanks() {
        Storage<FluidVariant> storage = storage();
        return storage instanceof SlottedStorage<FluidVariant> slotted ? slotted.getSlotCount() : views(storage).size();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        StorageView<FluidVariant> view = view(storage(), tank);
        return view == null ? FluidStack.EMPTY : FluidStack.fromVariant(view.getResource(), FluidAmounts.toMilliBuckets(view.getAmount()));
    }

    @Override
    public int getTankCapacity(int tank) {
        StorageView<FluidVariant> view = view(storage(), tank);
        return view == null ? 0 : FluidAmounts.toMilliBuckets(view.getCapacity());
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return !stack.isEmpty() && insertFluid(tank, stack, Action.SIMULATE).getAmount() < stack.getAmount();
    }

    @Override
    public void setFluidInTank(int tank, FluidStack stack) {
        throw new UnsupportedOperationException("Fabric item contents must be changed through insertion or extraction");
    }

    @Override
    public FluidStack insertFluid(int tank, FluidStack stack, Action action) {
        Storage<FluidVariant> storage = storage();
        if (storage instanceof SlottedStorage<FluidVariant>) {
            return new FabricFluidHandler(storage).insertFluid(tank, stack, action);
        }
        return tank == 0 && views(storage).size() == 1 ? new FabricFluidHandler(storage).insertFluid(stack, action) : stack;
    }

    @Override
    public FluidStack insertFluid(FluidStack stack, Action action) {
        return new FabricFluidHandler(storage()).insertFluid(stack, action);
    }

    @Override
    public FluidStack extractFluid(int tank, int amount, Action action) {
        Storage<FluidVariant> storage = storage();
        StorageView<FluidVariant> view = view(storage, tank);
        if (amount <= 0 || !storage.supportsExtraction() || view == null || view.isResourceBlank()) {
            return FluidStack.EMPTY;
        }
        FluidVariant variant = view.getResource();
        return FluidStack.fromVariant(variant, FabricFluidHandler.transfer(amount, action,
              (requested, transaction) -> view.extract(variant, requested, transaction)));
    }

    @Override
    public FluidStack extractFluid(int amount, Action action) {
        return new FabricFluidHandler(storage()).extractFluid(amount, action);
    }

    @Override
    public FluidStack extractFluid(FluidStack stack, Action action) {
        return new FabricFluidHandler(storage()).extractFluid(stack, action);
    }

    @Override
    public ItemStack getContainer() {
        return slot.container;
    }

    private static final class ContainerSlot extends SingleStackStorage {

        private final ItemStack original;
        private ItemStack container;

        private ContainerSlot(ItemStack original) {
            this.original = original;
            container = original;
        }

        @Override
        protected ItemStack getStack() {
            return container;
        }

        @Override
        protected void setStack(ItemStack stack) {
            container = stack;
        }

        @Override
        protected void onFinalCommit() {
            if (ItemStack.isSameItem(original, container)) {
                original.setTag(container.hasTag() ? container.getTag().copy() : null);
                original.setCount(container.getCount());
                container = original;
            }
        }
    }
}
