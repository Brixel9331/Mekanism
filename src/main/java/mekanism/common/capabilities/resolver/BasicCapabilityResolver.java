package mekanism.common.capabilities.resolver;

import java.util.List;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.capabilities.Capability;
import mekanism.api.capabilities.CapabilityReference;
import mekanism.common.lib.Lazy;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BasicCapabilityResolver implements ICapabilityResolver {

    public static <T> BasicCapabilityResolver create(Capability<T> supportedCapability, Supplier<T> supplier) {
        return new BasicCapabilityResolver(supplier, supportedCapability);
    }

    /**
     * Creates a capability resolver that strongly caches the result of the supplier. Persisting the calculated value through capability invalidation.
     */
    public static <T> BasicCapabilityResolver persistent(Capability<T> supportedCapability, Supplier<T> supplier) {
        return create(supportedCapability, supplier instanceof Lazy ? supplier : Lazy.of(supplier));
    }

    /**
     * Creates a capability resolver of a constant value. Usually {@code this} for tiles.
     */
    public static <T> BasicCapabilityResolver constant(Capability<T> supportedCapability, T value) {
        return create(supportedCapability, () -> value);
    }

    private final List<Capability<?>> supportedCapability;
    private final Supplier<?> supplier;
    @Nullable
    private CapabilityReference<?> cachedCapability;

    @SafeVarargs
    protected <T> BasicCapabilityResolver(Supplier<T> supplier, Capability<? super T>... supportedCapabilities) {
        this.supportedCapability = List.of(supportedCapabilities);
        this.supplier = supplier;
    }

    @Override
    public List<Capability<?>> getSupportedCapabilities() {
        return supportedCapability;
    }

    @Override
    public <T> CapabilityReference<T> resolve(Capability<T> capability, @Nullable Direction side) {
        if (cachedCapability == null || !cachedCapability.isPresent()) {
            //If the capability has not been retrieved yet, or it is not valid then recreate it
            cachedCapability = CapabilityReference.of(supplier);
        }
        return cachedCapability.cast();
    }

    @Override
    public void invalidate(Capability<?> capability, @Nullable Direction side) {
        //We only have one capability so just invalidate everything
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        CapabilityReference<?> previous = cachedCapability;
        cachedCapability = null;
        if (previous != null) {
            previous.invalidate();
        }
    }
}
