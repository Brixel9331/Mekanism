package mekanism.common.registration.impl;

import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import mekanism.api.providers.IItemProvider;
import mekanism.api.text.ILangEntry;
import mekanism.common.registration.CreativeTabBuilder;
import mekanism.common.registration.DeferredRegister;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;

public class CreativeTabDeferredRegister extends DeferredRegister<CreativeModeTab> {

    private final BiConsumer<ResourceKey<CreativeModeTab>, CreativeModeTab.Output> addToExistingTabs;
    private final String modid;
    private boolean listenerRegistered;

    public CreativeTabDeferredRegister(String modid) {
        this(modid, (tab, output) -> {
        });
    }

    public CreativeTabDeferredRegister(String modid, BiConsumer<ResourceKey<CreativeModeTab>, CreativeModeTab.Output> addToExistingTabs) {
        super(modid, Registries.CREATIVE_MODE_TAB, () -> BuiltInRegistries.CREATIVE_MODE_TAB);
        this.modid = modid;
        this.addToExistingTabs = addToExistingTabs;
    }

    @Override
    public void register() {
        super.register();
        if (!listenerRegistered) {
            ItemGroupEvents.MODIFY_ENTRIES_ALL.register((tab, entries) ->
                  addToExistingTabs.accept(BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab).orElseThrow(), entries));
            listenerRegistered = true;
        }
    }

    /**
     * @apiNote We manually require the title and icon to be passed so that we ensure all tabs have one.
     */
    public CreativeTabRegistryObject registerMain(ILangEntry title, IItemProvider icon, UnaryOperator<CreativeTabBuilder> operator) {
        return register(modid, title, icon, operator);
    }

    /**
     * @apiNote We manually require the title and icon to be passed so that we ensure all tabs have one.
     */
    public CreativeTabRegistryObject register(String name, ILangEntry title, IItemProvider icon, UnaryOperator<CreativeTabBuilder> operator) {
        return register(name, () -> {
            CreativeTabBuilder builder = new CreativeTabBuilder(title.translate(), icon::getItemStack);
            return operator.apply(builder).build();
        }, CreativeTabRegistryObject::new);
    }

    public static void addToDisplay(CreativeModeTab.Output output, ItemLike... items) {
        for (ItemLike item : items) {
            addToDisplay(output, item);
        }
    }

    public static void addToDisplay(CreativeModeTab.Output output, ItemLike itemLike) {
        if (itemLike.asItem() instanceof ICustomCreativeTabContents contents) {
            if (contents.addDefault()) {
                output.accept(itemLike);
            }
            contents.addItems(output);
        } else {
            output.accept(itemLike);
        }
    }

    public static void addToDisplay(Iterable<? extends ItemLike> items, CreativeModeTab.Output output) {
        for (ItemLike item : items) {
            addToDisplay(output, item);
        }
    }

    public interface ICustomCreativeTabContents {

        void addItems(CreativeModeTab.Output tabOutput);

        default boolean addDefault() {
            return true;
        }
    }
}
