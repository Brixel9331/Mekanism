package mekanism.common.integration.energy.fabric;

import mekanism.api.Action;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;

public final class FabricItemEnergyStorage implements EnergyStorage {

    private final Item item;
    private final ContainerItemContext context;
    private final IEnergyConversion conversion;

    public FabricItemEnergyStorage(Item item, ContainerItemContext context, IEnergyConversion conversion) {
        this.item = item;
        this.context = context;
        this.conversion = conversion;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        return transfer(maxAmount, transaction, true);
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        return transfer(maxAmount, transaction, false);
    }

    private long transfer(long maxAmount, TransactionContext transaction, boolean insert) {
        if (maxAmount < 0) {
            throw new IllegalArgumentException("Energy amount must not be negative");
        }
        if (maxAmount == 0 || !conversion.isEnabled() || !context.getItemVariant().isOf(item)) {
            return 0;
        }
        long count = context.getAmount();
        ItemStack copy = context.getItemVariant().toStack();
        IStrictEnergyHandler handler = FabricEnergyIntegration.directHandler(copy, null);
        if (handler == null || count == 0) {
            return 0;
        }
        FloatingLong requested = conversion.convertFrom(maxAmount / count);
        FloatingLong available = insert ? requested.subtract(handler.insertEnergy(requested, Action.SIMULATE)) : handler.extractEnergy(requested, Action.SIMULATE);
        long accepted = Math.min(maxAmount / count, conversion.convertToAsLong(available));
        FloatingLong exact = conversion.convertFrom(accepted);
        if (exact.isZero()) {
            return 0;
        }
        FloatingLong transferred = insert ? exact.subtract(handler.insertEnergy(exact, Action.EXECUTE)) : handler.extractEnergy(exact, Action.EXECUTE);
        if (!transferred.equals(exact)) {
            return 0;
        }
        try (Transaction nested = transaction.openNested()) {
            if (context.exchange(ItemVariant.of(copy), count, nested) != count) {
                return 0;
            }
            nested.commit();
            return accepted * count;
        }
    }

    @Override
    public long getAmount() {
        return total(false);
    }

    @Override
    public long getCapacity() {
        return total(true);
    }

    private long total(boolean capacity) {
        if (!conversion.isEnabled() || !context.getItemVariant().isOf(item)) {
            return 0;
        }
        IStrictEnergyHandler handler = FabricEnergyIntegration.directHandler(context.getItemVariant().toStack(), null);
        if (handler == null) {
            return 0;
        }
        FloatingLong total = FloatingLong.ZERO;
        for (int i = 0; i < handler.getEnergyContainerCount(); i++) {
            total = total.add(capacity ? handler.getMaxEnergy(i) : handler.getEnergy(i));
        }
        return conversion.convertToAsLong(total.multiply(context.getAmount()));
    }
}
