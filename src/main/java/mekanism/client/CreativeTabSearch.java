package mekanism.client;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.client.searchtree.IdSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeTabSearch {

    private Collection<ItemStack> contents;
    private Language language;
    private SearchTree<ItemStack> names;
    private SearchTree<ItemStack> tags;

    public SearchTree<ItemStack> getTree(CreativeModeTab tab, SearchRegistry.Key<ItemStack> key, Function<ItemStack, Stream<String>> tooltips) {
        Collection<ItemStack> currentContents = tab.getSearchTabDisplayItems();
        Language currentLanguage = Language.getInstance();
        if (contents != currentContents || language != currentLanguage) {
            contents = currentContents;
            language = currentLanguage;
            List<ItemStack> stacks = List.copyOf(contents);
            FullTextSearchTree<ItemStack> nameTree = new FullTextSearchTree<>(tooltips,
                  stack -> Stream.of(BuiltInRegistries.ITEM.getKey(stack.getItem())), stacks);
            nameTree.refresh();
            names = nameTree;
            tags = new IdSearchTree<>(stack -> stack.getTags().map(TagKey::location), stacks);
        }
        return key == SearchRegistry.CREATIVE_TAGS ? tags : names;
    }
}
