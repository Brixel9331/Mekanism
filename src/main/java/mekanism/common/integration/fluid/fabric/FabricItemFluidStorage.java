package mekanism.common.integration.fluid.fabric;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import mekanism.api.Action;
import mekanism.api.capabilities.CapabilityProvider;
import mekanism.api.fluid.FluidAmounts;
import mekanism.api.fluid.FluidStack;
import mekanism.api.fluid.IExtendedFluidHandlerItem;
import mekanism.common.capabilities.Capabilities;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class FabricItemFluidStorage implements SlottedStorage<FluidVariant> {

    private final Item item;
    private final ContainerItemContext context;

    private FabricItemFluidStorage(Item item, ContainerItemContext context) {
        this.item = item;
        this.context = context;
    }

    @Nullable
    public static FabricItemFluidStorage create(ItemStack stack, ContainerItemContext context) {
        if (stack.isEmpty()) {
            return null;
        }
        FabricItemFluidStorage storage = new FabricItemFluidStorage(stack.getItem(), context);
        return storage.resolveHandler() == null ? null : storage;
    }

    @Nullable
    private static IExtendedFluidHandlerItem directHandler(ItemStack stack) {
        return (Object) stack instanceof CapabilityProvider provider ? provider.getCapability(Capabilities.FLUID_HANDLER_ITEM, null).orElse(null) : null;
    }

    @Nullable
    private IExtendedFluidHandlerItem resolveHandler() {
        ItemVariant variant = context.getItemVariant();
        return variant.isOf(item) && context.getAmount() > 0 ? directHandler(variant.toStack()) : null;
    }

    @Override
    public int getSlotCount() {
        IExtendedFluidHandlerItem handler = resolveHandler();
        return handler == null ? 0 : handler.getTanks();
    }

    @Override
    public SingleSlotStorage<FluidVariant> getSlot(int slot) {
        Objects.checkIndex(slot, getSlotCount());
        return new TankView(slot);
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        int count = getSlotCount();
        List<StorageView<FluidVariant>> views = new ArrayList<>(count);
        for (int tank = 0; tank < count; tank++) {
            views.add(new TankView(tank));
        }
        return views.iterator();
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        return transfer(resource, maxAmount, transaction, -1, true);
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        return transfer(resource, maxAmount, transaction, -1, false);
    }

    private long transfer(FluidVariant resource, long maxAmount, TransactionContext transaction, int tank, boolean insert) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        int requested = FluidAmounts.toMilliBuckets(maxAmount);
        ItemVariant original = context.getItemVariant();
        if (requested == 0 || !original.isOf(item) || context.getAmount() == 0) {
            return 0;
        }
        IExtendedFluidHandlerItem handler = directHandler(original.toStack());
        if (handler == null || tank >= handler.getTanks() || !insert && tank >= 0 && !resource.equals(handler.getFluidInTank(tank).getVariant())) {
            return 0;
        }
        try (Transaction nested = transaction.openNested()) {
            FluidStack requestedStack = FluidStack.fromVariant(resource, requested);
            FluidStack result;
            if (insert) {
                result = tank < 0 ? handler.insertFluid(requestedStack, Action.EXECUTE) : handler.insertFluid(tank, requestedStack, Action.EXECUTE);
            } else {
                result = tank < 0 ? handler.extractFluid(requestedStack, Action.EXECUTE) : handler.extractFluid(tank, requested, Action.EXECUTE);
            }
            if (result.getAmount() > requested || !result.isEmpty() && !resource.equals(result.getVariant())) {
                throw new IllegalStateException("Item fluid handler returned an invalid transfer stack");
            }
            int transferred = insert ? requested - result.getAmount() : result.getAmount();
            if (transferred == 0 || !original.equals(context.getItemVariant()) || context.getAmount() == 0) {
                return 0;
            }
            ItemStack replacement = handler.getContainer();
            if (!replacement.isEmpty() && replacement.getCount() != 1) {
                throw new IllegalStateException("Item fluid handler changed the container count");
            }
            long exchanged = replacement.isEmpty() ? context.extract(original, 1, nested) : context.exchange(ItemVariant.of(replacement), 1, nested);
            if (exchanged != 1) {
                return 0;
            }
            nested.commit();
            return FluidAmounts.toFabric(transferred);
        }
    }

    private final class TankView implements SingleSlotStorage<FluidVariant> {

        private final int index;

        private TankView(int index) {
            this.index = index;
        }

        @Nullable
        private IExtendedFluidHandlerItem resolve() {
            IExtendedFluidHandlerItem handler = resolveHandler();
            return handler != null && index < handler.getTanks() ? handler : null;
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            return transfer(resource, maxAmount, transaction, index, true);
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            return transfer(resource, maxAmount, transaction, index, false);
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public FluidVariant getResource() {
            IExtendedFluidHandlerItem handler = resolve();
            return handler == null ? FluidVariant.blank() : handler.getFluidInTank(index).getVariant();
        }

        @Override
        public long getAmount() {
            IExtendedFluidHandlerItem handler = resolve();
            return handler == null ? 0 : FluidAmounts.toFabric(handler.getFluidInTank(index).getAmount());
        }

        @Override
        public long getCapacity() {
            IExtendedFluidHandlerItem handler = resolve();
            return handler == null ? 0 : FluidAmounts.toFabric(handler.getTankCapacity(index));
        }
    }
}
