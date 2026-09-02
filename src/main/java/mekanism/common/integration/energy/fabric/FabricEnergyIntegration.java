package mekanism.common.integration.energy.fabric;

import mekanism.api.AutomationType;
import mekanism.api.capabilities.CapabilityProvider;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.proxy.ProxyStrictEnergyHandler;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public final class FabricEnergyIntegration {

    private static boolean registered;

    private FabricEnergyIntegration() {
    }

    public static synchronized void register(IEnergyConversion conversion) {
        if (registered) {
            return;
        }
        registered = true;
        Capabilities.STRICT_ENERGY.block().registerFallback((level, pos, state, tile, side) -> {
            EnergyStorage storage = conversion.isEnabled() ? EnergyStorage.SIDED.find(level, pos, state, tile, side) : null;
            return storage == null ? null : new FabricStrictEnergyHandler(storage, conversion);
        });
        Capabilities.STRICT_ENERGY.item().registerFallback((stack, context) ->
              conversion.isEnabled() ? FabricItemEnergyHandler.create(stack, conversion) : null);
        EnergyStorage.SIDED.registerFallback((level, pos, state, tile, side) ->
              wrap(directHandler(tile, side), conversion));
        EnergyStorage.ITEM.registerFallback((stack, context) -> directHandler(stack, null) == null ? null :
              new FabricItemEnergyStorage(stack.getItem(), context, conversion));
    }

    @Nullable
    static IStrictEnergyHandler directHandler(@Nullable Object target, @Nullable Direction side) {
        return target instanceof CapabilityProvider provider ? provider.getCapability(Capabilities.STRICT_ENERGY, side).orElse(null) : null;
    }

    @Nullable
    private static EnergyStorage wrap(@Nullable IStrictEnergyHandler handler, IEnergyConversion conversion) {
        if (handler instanceof ProxyStrictEnergyHandler proxy) {
            return new FabricEnergyHandlerStorage(proxy, proxy::getEnergyContainer, conversion, AutomationType.EXTERNAL);
        }
        if (handler instanceof IMekanismStrictEnergyHandler mekanismHandler) {
            return new FabricEnergyHandlerStorage(handler, index -> mekanismHandler.getEnergyContainer(index, mekanismHandler.getEnergySideFor()),
                  conversion, mekanismHandler.getEnergySideFor() == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
        }
        return null;
    }
}
