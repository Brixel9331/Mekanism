package mekanism.common.capabilities;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import mekanism.api.capabilities.Capability;
import mekanism.api.capabilities.CapabilityProvider;
import mekanism.api.capabilities.CapabilityReference;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemCapabilityWrapper implements CapabilityProvider {

    private final Map<Capability<?>, ItemCapability> capabilities = new IdentityHashMap<>();
    private final CapabilityCache capabilityCache = new CapabilityCache();
    protected final ItemStack itemStack;

    public ItemCapabilityWrapper(ItemStack stack, ItemCapability... caps) {
        itemStack = stack;
        add(caps);
    }

    @Override
    public void invalidateCapabilities() {
        capabilityCache.invalidateAll();
    }

    public void add(ItemCapability... caps) {
        for (ItemCapability c : caps) {
            c.wrapper = this;
            c.init();
            c.gatherCapabilityResolvers(resolver -> {
                capabilityCache.addCapabilityResolver(resolver);
                //Keep track of which item capability helper is providing which capability
                for (Capability<?> supportedCapability : resolver.getSupportedCapabilities()) {
                    capabilities.put(supportedCapability, c);
                }
            });
        }
    }

    @NotNull
    @Override
    public <T> CapabilityReference<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (!itemStack.isEmpty() && capability.isAvailable()) {
            //Only provide capabilities if we are not empty and the capability is registered
            // as if it isn't registered we can just short circuit and not look up the capability
            if (!capabilityCache.isCapabilityDisabled(capability, null) && capabilityCache.canResolve(capability)) {
                //Note: Currently no item capabilities have toggleable capabilities, but check anyway to properly support our API
                ItemCapability cap = capabilities.get(capability);
                if (cap != null) {
                    //This should never be null if we are able to resolve it but validate it just in case
                    //Make sure that we load any data the cap needs from the stack, as it doesn't have any NBT set when it is initially initialized
                    // This also allows us to update to any direct changes on the NBT of the stack that someone may have made
                    //TODO: Potentially move the loading to the capability initializing spot, as NBT shouldn't be randomly changing anyways
                    // and then we could just use capabilityCache.getCapability as we wouldn't be required to load it. We also then in
                    // theory could get rid of our capabilities map
                    cap.load();
                }
                return capabilityCache.getCapabilityUnchecked(capability, null);
            }
        }
        return CapabilityReference.empty();
    }

    public abstract static class ItemCapability {

        private ItemCapabilityWrapper wrapper;

        protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
        }

        protected void init() {
        }

        protected void load() {
        }

        public ItemStack getStack() {
            return wrapper.itemStack;
        }
    }
}
