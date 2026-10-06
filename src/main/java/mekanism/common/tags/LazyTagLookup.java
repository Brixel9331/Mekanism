package mekanism.common.tags;

import java.util.List;
import java.util.function.Supplier;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;

public record LazyTagLookup<TYPE>(TagKey<TYPE> key, Supplier<Registry<TYPE>> registry) {

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

    public boolean isEmpty() {
        return registry.get().getTag(key).map(tag -> tag.size() == 0).orElse(true);
    }
}
