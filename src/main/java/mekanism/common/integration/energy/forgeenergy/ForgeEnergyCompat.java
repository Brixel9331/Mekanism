package mekanism.common.integration.energy.forgeenergy;

import java.util.Collection;
import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.capabilities.Capability;
import mekanism.api.capabilities.CapabilityReference;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedValue;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ForgeEnergyCompat implements IEnergyCompat {

    @Override
    public Capability<IEnergyStorage> getCapability() {
        return ForgeCapabilities.ENERGY;
    }

    @Override
    public boolean isUsable() {
        return EnergyUnit.FORGE_ENERGY.isEnabled();
    }

    @Override
    public Collection<CachedValue<?>> getBackingConfigs() {
        return Set.of(MekanismConfig.general.blacklistForge);
    }

    @Override
    public CapabilityReference<IEnergyStorage> getHandlerAs(IStrictEnergyHandler handler) {
        return CapabilityReference.of(() -> new ForgeEnergyIntegration(handler));
    }

    @Override
    public CapabilityReference<IStrictEnergyHandler> getLazyStrictEnergyHandler(Object provider, @Nullable Direction side) {
        return CapabilityUtils.getCapability(provider, getCapability(), side).lazyMap(ForgeStrictEnergyHandler::new);
    }
}