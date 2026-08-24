package mekanism.common.capabilities.resolver;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.capabilities.Capability;
import mekanism.api.capabilities.CapabilityReference;
import mekanism.api.inventory.IExtendedItemHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class TransporterCapabilityResolver implements ICapabilityResolver {

    private static final List<Capability<?>> SUPPORTED_CAPABILITY = Collections.singletonList(Capabilities.ITEM_HANDLER);

    private final Map<Direction, IExtendedItemHandler> persistentHandlers = new EnumMap<>(Direction.class);
    private final Predicate<Direction> exposesInsert;
    private final Function<Direction, IExtendedItemHandler> handlerFactory;
    private final Map<Direction, CapabilityReference<IExtendedItemHandler>> handlers = new EnumMap<>(Direction.class);

    public TransporterCapabilityResolver(Predicate<Direction> exposesInsert, Function<Direction, IExtendedItemHandler> handlerFactory) {
        this.exposesInsert = Objects.requireNonNull(exposesInsert);
        this.handlerFactory = Objects.requireNonNull(handlerFactory);
    }

    @Override
    public List<Capability<?>> getSupportedCapabilities() {
        return SUPPORTED_CAPABILITY;
    }

    /**
     * Lazily get and cache a handler instance for the given side, and make it be read only if something else is trying to interact with us using the null side
     */
    @Override
    public <T> CapabilityReference<T> resolve(Capability<T> capability, @Nullable Direction side) {
        if (capability != Capabilities.ITEM_HANDLER) {
            return CapabilityReference.empty();
        }
        if (side == null) {
            //We provide no readonly item handler view
            return CapabilityReference.empty();
        }
        CapabilityReference<IExtendedItemHandler> cachedCapability = handlers.get(side);
        if (cachedCapability == null || !cachedCapability.isPresent()) {
            //Note: We check here whether it exposes the cap rather than in the cap itself as we invalidate the cached cap whenever this changes
            if (exposesInsert.test(side)) {
                handlers.put(side, cachedCapability = CapabilityReference.of(() ->
                      persistentHandlers.computeIfAbsent(side, handlerFactory)));
            } else {
                return CapabilityReference.empty();
            }
        }
        return cachedCapability.cast();
    }

    @Override
    public void invalidate(Capability<?> capability, @Nullable Direction side) {
        if (capability == Capabilities.ITEM_HANDLER && side != null) {
            invalidate(handlers.remove(side));
        }
    }

    @Override
    public void invalidateAll() {
        List<CapabilityReference<IExtendedItemHandler>> previous = List.copyOf(handlers.values());
        handlers.clear();
        previous.forEach(this::invalidate);
    }

    private void invalidate(@Nullable CapabilityReference<?> cachedCapability) {
        if (cachedCapability != null && cachedCapability.isPresent()) {
            cachedCapability.invalidate();
        }
    }
}
