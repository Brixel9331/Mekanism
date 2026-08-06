package mekanism.api.functions;

import java.util.Objects;

@FunctionalInterface
public interface TriPredicate<T, U, V> {

    boolean test(T first, U second, V third);

    default TriPredicate<T, U, V> and(TriPredicate<? super T, ? super U, ? super V> other) {
        Objects.requireNonNull(other);
        return (first, second, third) -> test(first, second, third) && other.test(first, second, third);
    }

    default TriPredicate<T, U, V> or(TriPredicate<? super T, ? super U, ? super V> other) {
        Objects.requireNonNull(other);
        return (first, second, third) -> test(first, second, third) || other.test(first, second, third);
    }

    default TriPredicate<T, U, V> negate() {
        return (first, second, third) -> !test(first, second, third);
    }
}
