package mekanism.common.upgrade.transmitter;

import mekanism.api.fluid.FluidStack;
import mekanism.common.lib.transmitter.ConnectionType;

public class MechanicalPipeUpgradeData extends TransmitterUpgradeData {

    public final FluidStack contents;

    public MechanicalPipeUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, FluidStack contents) {
        super(redstoneReactive, connectionTypes);
        this.contents = contents;
    }
}