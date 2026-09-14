package mekanism.common.integration.item.fabric;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import mekanism.api.capabilities.CapabilityProvider;
import mekanism.api.inventory.IExtendedItemHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.inventory.ITransactionalInventorySlot;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.proxy.ProxyItemHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class FabricItemStorage implements SlottedStorage<ItemVariant> {

    private final Supplier<? extends IExtendedItemHandler> handlers;

    public FabricItemStorage(Supplier<? extends IExtendedItemHandler> handlers) {
        this.handlers = Objects.requireNonNull(handlers);
    }

    @Nullable
    public static FabricItemStorage create(@Nullable BlockEntity tile, @Nullable Direction side) {
        if (!(tile instanceof CapabilityProvider provider) || tile.isRemoved()) {
            return null;
        }
        FabricItemStorage storage = new FabricItemStorage(() -> tile.isRemoved() ? null : provider.getCapability(Capabilities.ITEM_HANDLER, side).orElse(null));
        return storage.resolveHandler() == null ? null : storage;
    }

    @Nullable
    private IExtendedItemHandler resolveHandler() {
        IExtendedItemHandler handler = handlers.get();
        return handler instanceof ProxyItemHandler || handler instanceof IMekanismInventory ? handler : null;
    }

    @Nullable
    private static IInventorySlot inventorySlot(IExtendedItemHandler handler, int slot) {
        if (slot < 0 || slot >= handler.getSlots()) {
            return null;
        }
        if (handler instanceof ProxyItemHandler proxy) {
            return proxy.getInventorySlot(slot);
        }
        if (handler instanceof IMekanismInventory inventory) {
            return inventory.getInventorySlot(slot, inventory.getInventorySideFor());
        }
        return null;
    }

    @Override
    public int getSlotCount() {
        IExtendedItemHandler handler = resolveHandler();
        return handler == null ? 0 : handler.getSlots();
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        IExtendedItemHandler handler = resolveHandler();
        Objects.checkIndex(slot, handler == null ? 0 : handler.getSlots());
        return new SlotView(slot, inventorySlot(handler, slot));
    }

    private List<SlotView> slotViews() {
        IExtendedItemHandler handler = resolveHandler();
        int count = handler == null ? 0 : handler.getSlots();
        List<SlotView> slots = new ArrayList<>(count);
        for (int slot = 0; slot < count; slot++) {
            slots.add(new SlotView(slot, inventorySlot(handler, slot)));
        }
        return slots;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return new ArrayList<StorageView<ItemVariant>>(slotViews()).iterator();
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (maxAmount == 0) {
            return 0;
        }
        List<SlotView> slots = slotViews();
        long remaining = maxAmount;
        try (Transaction nested = transaction.openNested()) {
            for (int pass = 0; pass < 2 && remaining > 0; pass++) {
                for (SlotView slot : slots) {
                    if (pass == 0 ? resource.equals(slot.getResource()) : slot.isResourceBlank()) {
                        remaining -= slot.insert(resource, remaining, nested);
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
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (maxAmount == 0) {
            return 0;
        }
        long remaining = maxAmount;
        try (Transaction nested = transaction.openNested()) {
            for (SlotView slot : slotViews()) {
                remaining -= slot.extract(resource, remaining, nested);
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

    private static int remainderCount(ItemStack remainder, ItemVariant resource, int requested) {
        if (remainder.getCount() < 0 || remainder.getCount() > requested || !remainder.isEmpty() && !resource.matches(remainder)) {
            throw new IllegalStateException("Item handler returned an invalid insertion remainder");
        }
        return remainder.getCount();
    }

    private static int extractedCount(ItemStack extracted, ItemVariant resource, int requested) {
        if (extracted.getCount() < 0 || extracted.getCount() > requested || !extracted.isEmpty() && !resource.matches(extracted)) {
            throw new IllegalStateException("Item handler returned an invalid extracted stack");
        }
        return extracted.getCount();
    }

    private final class SlotView implements SingleSlotStorage<ItemVariant> {

        private final int index;
        @Nullable
        private final IInventorySlot slot;

        private SlotView(int index, @Nullable IInventorySlot slot) {
            this.index = index;
            this.slot = slot;
        }

        @Nullable
        private IExtendedItemHandler resolve() {
            IExtendedItemHandler handler = resolveHandler();
            return handler != null && slot != null && inventorySlot(handler, index) == slot ? handler : null;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            IExtendedItemHandler handler = resolve();
            if (maxAmount == 0 || handler == null || !(slot instanceof ITransactionalInventorySlot transactional)) {
                return 0;
            }
            int requested = (int) Math.min(maxAmount, Integer.MAX_VALUE);
            int allowed = requested - remainderCount(handler.insertItem(index, resource.toStack(requested), true), resource, requested);
            if (allowed == 0) {
                return 0;
            }
            try (Transaction nested = transaction.openNested()) {
                int inserted = allowed - remainderCount(transactional.insertItem(resource.toStack(allowed), nested), resource, allowed);
                if (inserted > 0) {
                    nested.commit();
                }
                return inserted;
            }
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            IExtendedItemHandler handler = resolve();
            if (maxAmount == 0 || handler == null || !(slot instanceof ITransactionalInventorySlot transactional) || !resource.matches(handler.getStackInSlot(index))) {
                return 0;
            }
            int requested = (int) Math.min(maxAmount, Integer.MAX_VALUE);
            int allowed = extractedCount(handler.extractItem(index, requested, true), resource, requested);
            if (allowed == 0) {
                return 0;
            }
            try (Transaction nested = transaction.openNested()) {
                int extracted = extractedCount(transactional.extractItem(allowed, nested), resource, allowed);
                if (extracted > 0) {
                    nested.commit();
                }
                return extracted;
            }
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public ItemVariant getResource() {
            IExtendedItemHandler handler = resolve();
            return handler == null ? ItemVariant.blank() : ItemVariant.of(handler.getStackInSlot(index));
        }

        @Override
        public long getAmount() {
            IExtendedItemHandler handler = resolve();
            return handler == null ? 0 : handler.getStackInSlot(index).getCount();
        }

        @Override
        public long getCapacity() {
            IExtendedItemHandler handler = resolve();
            return handler == null ? 0 : Math.min(handler.getSlotLimit(index), slot.getLimit(handler.getStackInSlot(index)));
        }
    }
}
