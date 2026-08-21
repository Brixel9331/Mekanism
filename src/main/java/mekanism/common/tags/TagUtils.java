package mekanism.common.tags;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class TagUtils {

    public static <TYPE> LazyTagLookup<TYPE> tag(Registry<TYPE> registry, TagKey<TYPE> key) {
        return LazyTagLookup.create(registry, key);
    }

    public static <TYPE> TagKey<TYPE> createKey(Registry<TYPE> registry, ResourceLocation tag) {
        return TagKey.create(registry.key(), tag);
    }

    public static <TYPE> TagKey<TYPE> createKey(ResourceKey<Registry<TYPE>> registryKey, ResourceLocation tag) {
        return TagKey.create(registryKey, tag);
    }

    public static <TYPE> Stream<LazyTagLookup<TYPE>> stream(Registry<TYPE> registry) {
        return registry.getTagNames().map(key -> tag(registry, key));
    }

    public static <TYPE> Set<TagKey<TYPE>> tags(Registry<TYPE> registry, TYPE element) {
        return tagsStream(registry, element).collect(Collectors.toSet());
    }

    public static <TYPE> Stream<TagKey<TYPE>> tagsStream(Registry<TYPE> registry, TYPE element) {
        return registry.getResourceKey(element).flatMap(registry::getHolder).stream().flatMap(Holder::tags);
    }

    public static <TYPE> Set<ResourceLocation> tagNames(Registry<TYPE> registry, TYPE element) {
        return tagNames(tagsStream(registry, element));
    }

    public static Set<ResourceLocation> tagNames(Stream<? extends TagKey<?>> stream) {
        return stream.map(TagKey::location).collect(Collectors.toUnmodifiableSet());
    }
}
