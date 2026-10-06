package mekanism.api.energy;

import mekanism.api.AutomationType;
import mekanism.api.math.FloatingLong;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public interface ITransactionalEnergyContainer extends IEnergyContainer {

    FloatingLong insert(FloatingLong amount, TransactionContext transaction, AutomationType automationType);

    FloatingLong extract(FloatingLong amount, TransactionContext transaction, AutomationType automationType);
}
