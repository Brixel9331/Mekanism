package mekanism.common.util;

import java.util.function.Consumer;
import mekanism.api.capabilities.Capability;
import mekanism.api.capabilities.CapabilityReference;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CapabilityUtils {

    private CapabilityUtils() {
    }

    @NotNull
    public static <T> CapabilityReference<T> getCapability(@Nullable Object provider, @Nullable Capability<T> cap, @Nullable Direction side) {
        if (provider == null || cap == null || !cap.isAvailable()) {
            return CapabilityReference.empty();
        }
        return cap.find(provider, side);
    }

    public static <T> CapabilityReference<T> getCapability(@Nullable Object provider, Capability<T> cap) {
        return getCapability(provider, cap, null);
    }

    /**
     * Helper to add listeners that don't care about the data type to lazy optionals. This makes it so when we have {@code LazyOptional<?>} we can add a listener to it
     * without having to deal with the fact that one is "capture of ?" and the listener is "?".
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addListener(@NotNull CapabilityReference<?> lazyOptional, @NotNull Consumer listener) {
        lazyOptional.addListener(listener);
    }
}
