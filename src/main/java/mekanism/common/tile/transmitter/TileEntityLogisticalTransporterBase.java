package mekanism.common.tile.transmitter;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.item.CursedTransporterItemHandler;
import mekanism.common.capabilities.resolver.TransporterCapabilityResolver;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityLogisticalTransporterBase extends TileEntityTransmitter {

    protected TileEntityLogisticalTransporterBase(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        addCapabilityResolver(new TransporterCapabilityResolver(side -> getTransmitter().exposesInsertCap(side),
              side -> new CursedTransporterItemHandler(getTransmitter(), worldPosition.relative(side), () -> level == null ? -1 : level.getGameTime())));
    }

    @Override
    protected abstract LogisticalTransporterBase createTransmitter(IBlockProvider blockProvider);

    @Override
    public LogisticalTransporterBase getTransmitter() {
        return (LogisticalTransporterBase) super.getTransmitter();
    }

    public static void tickClient(Level level, BlockPos pos, BlockState state, TileEntityLogisticalTransporterBase transmitter) {
        transmitter.getTransmitter().onUpdateClient();
    }

    @Override
    public void onUpdateServer() {
        super.onUpdateServer();
        getTransmitter().onUpdateServer();
    }

    @Override
    public void blockRemoved() {
        super.blockRemoved();
        if (!isRemote()) {
            LogisticalTransporterBase transporter = getTransmitter();
            if (!transporter.isUpgrading()) {
                //If the transporter is not currently being upgraded, drop the contents
                for (TransporterStack stack : transporter.getTransit()) {
                    TransporterUtils.drop(transporter, stack);
                }
            }
        }
    }

    @Override
    public void sideChanged(@NotNull Direction side, @NotNull ConnectionType old, @NotNull ConnectionType type) {
        super.sideChanged(side, old, type);
        //Note: We don't expose a cap for when the connection type is none or push and this method only gets called if type != old,
        // so we can check to ensure that if we are one of the two that the other isn't the other one we don't have a cap for
        if (type == ConnectionType.NONE && old != ConnectionType.PUSH ||
            type == ConnectionType.PUSH && old != ConnectionType.NONE) {
            invalidateCapability(Capabilities.ITEM_HANDLER, side);
            //Notify the neighbor on that side our state changed and we no longer have a capability
            WorldUtils.notifyNeighborOfChange(level, side, worldPosition);
        } else if (old == ConnectionType.NONE && type != ConnectionType.PUSH ||
                   old == ConnectionType.PUSH && type != ConnectionType.NONE) {
            //Notify the neighbor on that side our state changed, and we now do have a capability
            WorldUtils.notifyNeighborOfChange(level, side, worldPosition);
        }
    }

}
