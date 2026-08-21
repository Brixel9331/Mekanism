package mekanism.common.capabilities.chemical;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.capabilities.CapabilityReference;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.common.util.CapabilityUtils;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class BoxedChemicalHandler {

    private final Map<ChemicalType, CapabilityReference<? extends IChemicalHandler<?, ?>>> handlers = new EnumMap<>(ChemicalType.class);

    @Nullable
    @SuppressWarnings("unchecked")
    public <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> IChemicalHandler<CHEMICAL, STACK> getHandlerFor(ChemicalType chemicalType) {
        if (handlers.containsKey(chemicalType)) {
            Optional<? extends IChemicalHandler<?, ?>> handler = handlers.get(chemicalType).resolve();
            if (handler.isPresent()) {
                return (IChemicalHandler<CHEMICAL, STACK>) handler.get();
            }
        }
        return null;
    }

    public void addGasHandler(CapabilityReference<IGasHandler> lazyHandler) {
        handlers.put(ChemicalType.GAS, lazyHandler);
    }

    public void addInfusionHandler(CapabilityReference<IInfusionHandler> lazyHandler) {
        handlers.put(ChemicalType.INFUSION, lazyHandler);
    }

    public void addPigmentHandler(CapabilityReference<IPigmentHandler> lazyHandler) {
        handlers.put(ChemicalType.PIGMENT, lazyHandler);
    }

    public void addSlurryHandler(CapabilityReference<ISlurryHandler> lazyHandler) {
        handlers.put(ChemicalType.SLURRY, lazyHandler);
    }

    public boolean sameHandlers(BoxedChemicalHandler other) {
        return this == other || handlers.size() == other.handlers.size() && handlers.entrySet().stream().noneMatch(entry -> entry.getValue() != other.handlers.get(entry.getKey()));
    }

    public void addRefreshListeners(Consumer<CapabilityReference<BoxedChemicalHandler>> refreshListener) {
        //TODO - V11: Make the listener only have to invalidate specific sub pieces
        for (CapabilityReference<? extends IChemicalHandler<?, ?>> sourceAcceptor : handlers.values()) {
            //Use unchecked generics to add the listener to the source acceptor
            CapabilityUtils.addListener(sourceAcceptor, refreshListener);
        }
    }
}