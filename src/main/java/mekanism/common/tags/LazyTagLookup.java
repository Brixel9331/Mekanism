package mekanism.common.tags;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;

public record LazyTagLookup<TYPE>(TagKey<TYPE> key, Supplier<Registry<TYPE>> registry) implements Iterable<TYPE> {

    public static <TYPE> LazyTagLookup<TYPE> create(Registry<TYPE> registry, TagKey<TYPE> key) {
        return new LazyTagLookup<>(key, () -> registry);
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>> LazyTagLookup<CHEMICAL> create(ChemicalTags<CHEMICAL> registry, TagKey<CHEMICAL> key) {
        return new LazyTagLookup<>(key, registry::getRegistry);
    }

    public List<TYPE> tag() {
        return registry.get().getTag(key).stream().flatMap(HolderSet::stream).map(Holder::value).toList();
    }

    public boolean contains(TYPE element) {
        Registry<TYPE> registry = this.registry.get();
        return registry.getResourceKey(element).flatMap(registry::getHolder).map(holder -> holder.is(key)).orElse(false);
    }

    public Stream<TYPE> stream() {
        return registry.get().getTag(key).stream().flatMap(HolderSet::stream).map(Holder::value);
    }

    @Override
    public Iterator<TYPE> iterator() {
        return stream().iterator();
    }

    public int size() {
        return registry.get().getTag(key).map(HolderSet::size).orElse(0);
    }

    public boolean isBound() {
        return registry.get().getTag(key).isPresent();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LazyTagLookup<?> lookup && key.equals(lookup.key) && registry.get() == lookup.registry.get();
    }

    @Override
    public int hashCode() {
        return 31 * key.hashCode() + System.identityHashCode(registry.get());
    }

    public boolean isEmpty() {
        return registry.get().getTag(key).map(tag -> tag.size() == 0).orElse(true);
    }
}
