package mekanism.common.integration.energy.fabric;

import java.util.Objects;
import java.util.function.IntFunction;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.energy.ITransactionalEnergyContainer;
import mekanism.api.math.FloatingLong;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;

public final class FabricEnergyHandlerStorage implements EnergyStorage {

    private final IStrictEnergyHandler handler;
    private final IntFunction<IEnergyContainer> containers;
    private final IEnergyConversion conversion;
    private final AutomationType automation;

    public FabricEnergyHandlerStorage(IStrictEnergyHandler handler, IntFunction<IEnergyContainer> containers, IEnergyConversion conversion,
          AutomationType automation) {
        this.handler = Objects.requireNonNull(handler);
        this.containers = Objects.requireNonNull(containers);
        this.conversion = Objects.requireNonNull(conversion);
        this.automation = Objects.requireNonNull(automation);
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
        if (maxAmount == 0 || !conversion.isEnabled()) {
            return 0;
        }
        FloatingLong requested = conversion.convertFrom(maxAmount);
        FloatingLong available = insert ? requested.subtract(handler.insertEnergy(requested, Action.SIMULATE)) : handler.extractEnergy(requested, Action.SIMULATE);
        long accepted = Math.min(maxAmount, conversion.convertToAsLong(available));
        FloatingLong remaining = conversion.convertFrom(accepted);
        if (remaining.isZero()) {
            return 0;
        }
        try (Transaction nested = transaction.openNested()) {
            for (int i = 0; i < handler.getEnergyContainerCount() && !remaining.isZero(); i++) {
                if (containers.apply(i) instanceof ITransactionalEnergyContainer container) {
                    FloatingLong allowed = insert ? remaining.subtract(handler.insertEnergy(i, remaining, Action.SIMULATE))
                          : handler.extractEnergy(i, remaining, Action.SIMULATE);
                    FloatingLong transferred = insert ? allowed.subtract(container.insert(allowed, nested, automation)) : container.extract(allowed, nested, automation);
                    remaining = remaining.subtract(transferred);
                }
            }
            if (!remaining.isZero()) {
                return 0;
            }
            nested.commit();
            return accepted;
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
        if (!conversion.isEnabled()) {
            return 0;
        }
        FloatingLong total = FloatingLong.ZERO;
        for (int i = 0; i < handler.getEnergyContainerCount(); i++) {
            total = total.add(capacity ? handler.getMaxEnergy(i) : handler.getEnergy(i));
        }
        return conversion.convertToAsLong(total);
    }
}
