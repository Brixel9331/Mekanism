package mekanism.api.capabilities;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public final class CapabilityReference<T> {

    @Nullable
    private Supplier<? extends T> supplier;
    @Nullable
    private T value;
    private boolean valid;
    private final BooleanSupplier validity;
    @Nullable
    private final Supplier<? extends T> lookup;
    private final Set<Consumer<? super CapabilityReference<T>>> listeners = new LinkedHashSet<>();

    private CapabilityReference(@Nullable Supplier<? extends T> supplier, BooleanSupplier validity) {
        this(supplier, validity, null);
    }

    private CapabilityReference(@Nullable Supplier<? extends T> supplier, BooleanSupplier validity, @Nullable Supplier<? extends T> lookup) {
        this.supplier = supplier;
        this.valid = supplier != null || lookup != null;
        this.validity = Objects.requireNonNull(validity);
        this.lookup = lookup;
    }

    public static <T> CapabilityReference<T> empty() {
        return new CapabilityReference<>(null, () -> false);
    }

    public static <T> CapabilityReference<T> of(Supplier<? extends T> supplier) {
        return of(supplier, () -> true);
    }

    public static <T> CapabilityReference<T> of(Supplier<? extends T> supplier, BooleanSupplier validity) {
        return new CapabilityReference<>(Objects.requireNonNull(supplier), validity);
    }

    public static <T> CapabilityReference<T> fromLookup(Supplier<? extends T> lookup) {
        return new CapabilityReference<>(null, () -> true, Objects.requireNonNull(lookup));
    }

    public boolean isPresent() {
        if (valid && !validity.getAsBoolean()) {
            invalidate();
        }
        if (valid && lookup != null) {
            T resolved = lookup.get();
            if (resolved == null) {
                invalidate();
            } else if (valid) {
                value = resolved;
            }
        }
        return valid;
    }

    public Optional<T> resolve() {
        if (!isPresent()) {
            return Optional.empty();
        }
        if (supplier != null) {
            value = Objects.requireNonNull(supplier.get(), "Capability supplier returned null");
            supplier = null;
        }
        return valid ? Optional.of(value) : Optional.empty();
    }

    public void ifPresent(Consumer<? super T> consumer) {
        resolve().ifPresent(consumer);
    }

    @Nullable
    public T orElse(@Nullable T fallback) {
        return resolve().orElse(fallback);
    }

    public T orElseGet(Supplier<? extends T> fallback) {
        return resolve().orElseGet(fallback);
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exception) throws X {
        return resolve().orElseThrow(exception);
    }

    public <R> Optional<R> map(Function<? super T, ? extends R> mapper) {
        return resolve().map(mapper);
    }

    public Optional<T> filter(Predicate<? super T> predicate) {
        return resolve().filter(predicate);
    }

    public <R> CapabilityReference<R> lazyMap(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return empty();
        }
        CapabilityReference<R> mapped = lookup == null ? of(() -> mapper.apply(resolve().orElseThrow()), this::isPresent) :
              fromLookup(() -> resolve().map(mapper).orElse(null));
        addListener(ignored -> mapped.invalidate());
        return mapped;
    }

    @SuppressWarnings("unchecked")
    public <R> CapabilityReference<R> cast() {
        return (CapabilityReference<R>) this;
    }

    public void addListener(Consumer<? super CapabilityReference<T>> listener) {
        Objects.requireNonNull(listener);
        if (isPresent()) {
            listeners.add(listener);
        } else {
            listener.accept(this);
        }
    }

    public void removeListener(Consumer<? super CapabilityReference<T>> listener) {
        listeners.remove(listener);
    }

    public void invalidate() {
        if (valid) {
            valid = false;
            supplier = null;
            value = null;
            var snapshot = Set.copyOf(listeners);
            listeners.clear();
            snapshot.forEach(listener -> listener.accept(this));
        }
    }
}
