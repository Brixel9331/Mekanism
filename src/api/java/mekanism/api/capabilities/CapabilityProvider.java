package mekanism.api.capabilities;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface CapabilityProvider {

    <T> CapabilityReference<T> getCapability(Capability<T> capability, @Nullable Direction side);

    default <T> CapabilityReference<T> getCapability(Capability<T> capability) {
        return getCapability(capability, null);
    }

    default void invalidateCapabilities() {
    }
}
