package mekanism.common.registration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class DeferredRegister<T> {

    private final String namespace;
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final Supplier<Registry<T>> registrySupplier;
    private final Map<ResourceLocation, Consumer<Registry<T>>> entries = new LinkedHashMap<>();
    private State state = State.NEW;

    public DeferredRegister(String namespace, ResourceKey<? extends Registry<T>> registryKey, Supplier<Registry<T>> registrySupplier) {
        this.namespace = Objects.requireNonNull(namespace);
        this.registryKey = Objects.requireNonNull(registryKey);
        this.registrySupplier = Objects.requireNonNull(registrySupplier);
    }

    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
        return createEntry(name, supplier);
    }

    @SuppressWarnings("unchecked")
    private <I extends T> RegistryObject<I> createEntry(String name, Supplier<? extends I> supplier) {
        if (state != State.NEW) {
            throw new IllegalStateException("Registration has already started for " + registryKey.location());
        }
        Objects.requireNonNull(supplier);
        ResourceLocation id = new ResourceLocation(namespace, name);
        if (entries.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate registry entry: " + id);
        }
        ResourceKey<I> key = (ResourceKey<I>) (ResourceKey<?>) ResourceKey.create(registryKey, id);
        RegistryObject<I> object = new RegistryObject<>(key);
        entries.put(id, registry -> object.bind(Registry.register(registry, id, Objects.requireNonNull(supplier.get(), "Registry supplier returned null"))));
        return object;
    }

    protected <I extends T, W extends RegistryObject<I>> W register(String name, Supplier<? extends I> supplier, Function<RegistryObject<I>, W> wrapper) {
        return wrapper.apply(createEntry(name, supplier));
    }

    public void register() {
        if (state == State.REGISTERED) {
            return;
        }
        if (state != State.NEW) {
            throw new IllegalStateException("Cannot register " + registryKey.location() + " while " + state);
        }
        state = State.REGISTERING;
        try {
            Registry<T> registry = Objects.requireNonNull(registrySupplier.get());
            if (!registry.key().equals(registryKey)) {
                throw new IllegalArgumentException("Registry key does not match " + registryKey.location());
            }
            for (ResourceLocation id : entries.keySet()) {
                if (registry.containsKey(id)) {
                    throw new IllegalStateException("Registry entry already exists: " + id);
                }
            }
            for (Consumer<Registry<T>> entry : entries.values()) {
                entry.accept(registry);
            }
            state = State.REGISTERED;
        } catch (RuntimeException | Error failure) {
            state = State.FAILED;
            throw failure;
        }
    }

    private enum State {
        NEW,
        REGISTERING,
        REGISTERED,
        FAILED
    }
}
