package mekanism.common.integration.energy.fabric;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import team.reborn.energy.api.EnergyStorage;

public final class FabricStrictEnergyHandler implements IStrictEnergyHandler {

    private final Supplier<EnergyStorage> storage;
    private final IEnergyConversion conversion;
    private final BooleanSupplier validContainer;

    public FabricStrictEnergyHandler(EnergyStorage storage, IEnergyConversion conversion) {
        this(() -> storage, conversion, () -> true);
    }

    public FabricStrictEnergyHandler(Supplier<EnergyStorage> storage, IEnergyConversion conversion, BooleanSupplier validContainer) {
        this.storage = Objects.requireNonNull(storage);
        this.conversion = Objects.requireNonNull(conversion);
        this.validContainer = Objects.requireNonNull(validContainer);
    }

    @Override
    public int getEnergyContainerCount() {
        return 1;
    }

    @Override
    public FloatingLong getEnergy(int container) {
        EnergyStorage current = storage.get();
        return container == 0 && current != null && conversion.isEnabled() ? conversion.convertFrom(current.getAmount()) : FloatingLong.ZERO;
    }

    @Override
    public void setEnergy(int container, FloatingLong energy) {
        throw new UnsupportedOperationException("Fabric energy must be changed through insertion or extraction");
    }

    @Override
    public FloatingLong getMaxEnergy(int container) {
        EnergyStorage current = storage.get();
        return container == 0 && current != null && conversion.isEnabled() ? conversion.convertFrom(current.getCapacity()) : FloatingLong.ZERO;
    }

    @Override
    public FloatingLong getNeededEnergy(int container) {
        return getMaxEnergy(container).subtract(getEnergy(container));
    }

    @Override
    public FloatingLong insertEnergy(int container, FloatingLong amount, Action action) {
        return container == 0 ? amount.subtract(transfer(amount, action, true)) : amount;
    }

    @Override
    public FloatingLong extractEnergy(int container, FloatingLong amount, Action action) {
        return container == 0 ? transfer(amount, action, false) : FloatingLong.ZERO;
    }

    private FloatingLong transfer(FloatingLong amount, Action action, boolean insert) {
        if (!conversion.isEnabled()) {
            return FloatingLong.ZERO;
        }
        EnergyStorage current = storage.get();
        long requested = conversion.convertToAsLong(amount);
        if (requested == 0 || current == null || (insert ? !current.supportsInsertion() : !current.supportsExtraction())) {
            return FloatingLong.ZERO;
        }
        try (Transaction transaction = action.execute() ? Transaction.openOuter() : Transaction.openNested(Transaction.getCurrentUnsafe())) {
            long transferred = insert ? current.insert(requested, transaction) : current.extract(requested, transaction);
            if (transferred < 0 || transferred > requested) {
                throw new IllegalStateException("Fabric energy storage returned an invalid transfer amount");
            }
            if (!validContainer.getAsBoolean()) {
                return FloatingLong.ZERO;
            }
            if (action.execute() && transferred > 0) {
                transaction.commit();
            }
            return conversion.convertFrom(transferred);
        }
    }
}
