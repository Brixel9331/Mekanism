package mekanism.common.capabilities;

import mekanism.api.IAlloyInteraction;
import mekanism.api.IConfigCardAccess;
import mekanism.api.IConfigurable;
import mekanism.api.IEvaporationSolar;
import mekanism.api.capabilities.Capability;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.inventory.IExtendedItemHandler;
import mekanism.api.lasers.ILaserDissipation;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.api.radiation.capability.IRadiationShielding;
import mekanism.common.integration.fluid.fabric.FabricFluidHandler;
import mekanism.common.integration.item.fabric.FabricItemHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.resources.ResourceLocation;

public class Capabilities {

    private Capabilities() {
    }

    public static final Capability<IGasHandler> GAS_HANDLER = new Capability<>(new ResourceLocation("mekanism", "gas_handler"), IGasHandler.class);
    public static final Capability<IInfusionHandler> INFUSION_HANDLER = new Capability<>(new ResourceLocation("mekanism", "infusion_handler"), IInfusionHandler.class);
    public static final Capability<IPigmentHandler> PIGMENT_HANDLER = new Capability<>(new ResourceLocation("mekanism", "pigment_handler"), IPigmentHandler.class);
    public static final Capability<ISlurryHandler> SLURRY_HANDLER = new Capability<>(new ResourceLocation("mekanism", "slurry_handler"), ISlurryHandler.class);

    public static final Capability<IHeatHandler> HEAT_HANDLER = new Capability<>(new ResourceLocation("mekanism", "heat_handler"), IHeatHandler.class);

    public static final Capability<IStrictEnergyHandler> STRICT_ENERGY = new Capability<>(new ResourceLocation("mekanism", "strict_energy"), IStrictEnergyHandler.class);

    public static final Capability<IExtendedItemHandler> ITEM_HANDLER = new Capability<>(new ResourceLocation("mekanism", "item_handler"), IExtendedItemHandler.class);

    public static final Capability<IExtendedFluidHandler> FLUID_HANDLER = new Capability<>(new ResourceLocation("mekanism", "fluid_handler"), IExtendedFluidHandler.class);

    public static final Capability<IConfigurable> CONFIGURABLE = new Capability<>(new ResourceLocation("mekanism", "configurable"), IConfigurable.class);

    public static final Capability<IAlloyInteraction> ALLOY_INTERACTION = new Capability<>(new ResourceLocation("mekanism", "alloy_interaction"), IAlloyInteraction.class);

    public static final Capability<IConfigCardAccess> CONFIG_CARD = new Capability<>(new ResourceLocation("mekanism", "config_card"), IConfigCardAccess.class);

    public static final Capability<IEvaporationSolar> EVAPORATION_SOLAR = new Capability<>(new ResourceLocation("mekanism", "evaporation_solar"), IEvaporationSolar.class);

    public static final Capability<ILaserReceptor> LASER_RECEPTOR = new Capability<>(new ResourceLocation("mekanism", "laser_receptor"), ILaserReceptor.class);

    public static final Capability<ILaserDissipation> LASER_DISSIPATION = new Capability<>(new ResourceLocation("mekanism", "laser_dissipation"), ILaserDissipation.class);

    public static final Capability<IRadiationShielding> RADIATION_SHIELDING = new Capability<>(new ResourceLocation("mekanism", "radiation_shielding"), IRadiationShielding.class);

    public static final Capability<IRadiationEntity> RADIATION_ENTITY = new Capability<>(new ResourceLocation("mekanism", "radiation_entity"), IRadiationEntity.class);

    static {
        FLUID_HANDLER.block().registerFallback((level, pos, state, tile, side) -> {
            Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, state, tile, side);
            return storage == null ? null : new FabricFluidHandler(storage);
        });
        ITEM_HANDLER.block().registerFallback((level, pos, state, tile, side) -> {
            Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, state, tile, side);
            return storage == null ? null : new FabricItemHandler(storage);
        });
    }
}
