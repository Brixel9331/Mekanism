package mekanism.common.lib;

import java.util.Objects;
import java.util.function.Supplier;

public final class Lazy<T> implements Supplier<T> {

    private volatile Supplier<? extends T> supplier;
    private T value;

    private Lazy(Supplier<? extends T> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    public static <T> Lazy<T> of(Supplier<? extends T> supplier) {
        return new Lazy<>(supplier);
    }

    @Override
    public T get() {
        if (supplier != null) {
            synchronized (this) {
                if (supplier != null) {
                    value = supplier.get();
                    supplier = null;
                }
            }
        }
        return value;
    }
}
