package mekanism.common.integration.energy.fabric;

import java.util.Objects;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.ITransactionalEnergyContainer;
import mekanism.api.math.FloatingLong;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;

public final class FabricEnergyStorage implements EnergyStorage {

    private final ITransactionalEnergyContainer container;
    private final IEnergyConversion conversion;

    public FabricEnergyStorage(ITransactionalEnergyContainer container, IEnergyConversion conversion) {
        this.container = Objects.requireNonNull(container);
        this.conversion = Objects.requireNonNull(conversion);
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        checkAmount(maxAmount);
        if (maxAmount == 0 || !conversion.isEnabled()) {
            return 0;
        }
        FloatingLong requested = conversion.convertFrom(maxAmount);
        FloatingLong remainder = container.insert(requested, Action.SIMULATE, AutomationType.EXTERNAL);
        long accepted = Math.min(maxAmount, conversion.convertToAsLong(requested.subtract(remainder)));
        if (accepted == 0) {
            return 0;
        }
        FloatingLong transferred = conversion.convertFrom(accepted);
        if (transferred.isZero()) {
            return 0;
        }
        try (Transaction nested = transaction.openNested()) {
            if (!container.insert(transferred, nested, AutomationType.EXTERNAL).isZero()) {
                return 0;
            }
            nested.commit();
            return accepted;
        }
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        checkAmount(maxAmount);
        if (maxAmount == 0 || !conversion.isEnabled()) {
            return 0;
        }
        FloatingLong requested = conversion.convertFrom(maxAmount);
        FloatingLong available = container.extract(requested, Action.SIMULATE, AutomationType.EXTERNAL);
        long extracted = Math.min(maxAmount, conversion.convertToAsLong(available));
        if (extracted == 0) {
            return 0;
        }
        FloatingLong transferred = conversion.convertFrom(extracted);
        if (transferred.isZero()) {
            return 0;
        }
        try (Transaction nested = transaction.openNested()) {
            if (!container.extract(transferred, nested, AutomationType.EXTERNAL).equals(transferred)) {
                return 0;
            }
            nested.commit();
            return extracted;
        }
    }

    @Override
    public long getAmount() {
        return conversion.isEnabled() ? conversion.convertToAsLong(container.getEnergy()) : 0;
    }

    @Override
    public long getCapacity() {
        return conversion.isEnabled() ? conversion.convertToAsLong(container.getMaxEnergy()) : 0;
    }

    private static void checkAmount(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Energy amount must not be negative");
        }
    }
}
