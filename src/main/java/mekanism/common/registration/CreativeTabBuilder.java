package mekanism.common.registration;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CreativeTabBuilder {

    private static final Map<CreativeModeTab, Layout> LAYOUTS = new IdentityHashMap<>();
    private final CreativeModeTab.Builder builder;
    private int searchBarWidth;
    @Nullable
    private ResourceLocation background;

    public CreativeTabBuilder(Component title, Supplier<ItemStack> icon) {
        builder = FabricItemGroup.builder().title(Objects.requireNonNull(title)).icon(Objects.requireNonNull(icon));
    }

    public CreativeTabBuilder withSearchBar() {
        return withSearchBar(89);
    }

    public CreativeTabBuilder withSearchBar(int width) {
        if (width <= 0 || width > 89) {
            throw new IllegalArgumentException("Search bar width must be between 1 and 89");
        }
        searchBarWidth = width;
        builder.backgroundSuffix("item_search.png");
        return this;
    }

    public CreativeTabBuilder withBackgroundLocation(ResourceLocation texture) {
        background = Objects.requireNonNull(texture);
        return this;
    }

    public CreativeTabBuilder displayItems(CreativeModeTab.DisplayItemsGenerator generator) {
        builder.displayItems(Objects.requireNonNull(generator));
        return this;
    }

    public CreativeModeTab build() {
        CreativeModeTab tab = builder.build();
        LAYOUTS.put(tab, new Layout(searchBarWidth, background));
        return tab;
    }

    @Nullable
    public static Layout layout(CreativeModeTab tab) {
        return LAYOUTS.get(tab);
    }

    public static boolean hasSearchBar(CreativeModeTab tab) {
        Layout layout = layout(tab);
        return layout != null && layout.searchBarWidth() > 0;
    }

    public record Layout(int searchBarWidth, @Nullable ResourceLocation background) {
    }
}
