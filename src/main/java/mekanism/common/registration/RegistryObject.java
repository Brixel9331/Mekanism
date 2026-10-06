package mekanism.common.registration;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class RegistryObject<T> implements Supplier<T>, INamedEntry {

    private final ResourceKey<T> key;
    @Nullable
    private final RegistryObject<T> delegate;
    @Nullable
    private T value;

    RegistryObject(ResourceKey<T> key) {
        this.key = Objects.requireNonNull(key);
        this.delegate = null;
    }

    protected RegistryObject(RegistryObject<T> delegate) {
        this.key = delegate.key;
        this.delegate = delegate;
    }

    void bind(T value) {
        if (delegate != null || this.value != null) {
            throw new IllegalStateException("Registry object already bound: " + key.location());
        }
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public T get() {
        if (delegate != null) {
            return delegate.get();
        }
        if (value == null) {
            throw new IllegalStateException("Registry object has not been registered: " + key.location());
        }
        return value;
    }

    public ResourceKey<T> key() {
        return key;
    }

    public ResourceLocation getId() {
        return key.location();
    }

    @Override
    public String getInternalRegistryName() {
        return getId().getPath();
    }
}
