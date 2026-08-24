package mekanism.common.mixin.client;

import mekanism.client.CreativeTabSearch;
import mekanism.client.SpecialColors;
import mekanism.common.registration.CreativeTabBuilder;
import mekanism.common.registration.CreativeTabBuilder.Layout;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    @Shadow
    private static CreativeModeTab selectedTab;
    @Shadow
    private EditBox searchBox;
    @Unique
    private final CreativeTabSearch mekanism$search = new CreativeTabSearch();

    protected CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Redirect(method = {"charTyped", "keyPressed", "refreshCurrentTabContents"}, at = @At(value = "INVOKE",
          target = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;"))
    private CreativeModeTab.Type mekanism$searchableTabType(CreativeModeTab tab) {
        return CreativeTabBuilder.hasSearchBar(tab) ? CreativeModeTab.Type.SEARCH : tab.getType();
    }

    @Redirect(method = "selectTab", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;", ordinal = 4))
    private CreativeModeTab.Type mekanism$showSearchBox(CreativeModeTab tab) {
        return mekanism$searchableTabType(tab);
    }

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void mekanism$positionSearchBox(CreativeModeTab tab, CallbackInfo ci) {
        Layout layout = CreativeTabBuilder.layout(tab);
        int width = layout != null && layout.searchBarWidth() > 0 ? layout.searchBarWidth() : 89;
        searchBox.setWidth(width);
        searchBox.setX(leftPos + 171 - width);
    }

    @Redirect(method = "refreshSearchResults", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/client/Minecraft;getSearchTree(Lnet/minecraft/client/searchtree/SearchRegistry$Key;)Lnet/minecraft/client/searchtree/SearchTree;"))
    private SearchTree<ItemStack> mekanism$searchWithinTab(Minecraft client, SearchRegistry.Key<ItemStack> key) {
        if (CreativeTabBuilder.hasSearchBar(selectedTab)) {
            return mekanism$search.getTree(selectedTab, key, stack -> stack.getTooltipLines(client.player, TooltipFlag.Default.NORMAL).stream()
                  .map(Component::getString).map(ChatFormatting::stripFormatting).map(String::trim).filter(text -> !text.isEmpty()));
        }
        return client.getSearchTree(key);
    }

    @ModifyArg(method = "renderBg", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V", ordinal = 0), index = 0)
    private ResourceLocation mekanism$tabBackground(ResourceLocation texture) {
        Layout layout = CreativeTabBuilder.layout(selectedTab);
        return layout != null && layout.background() != null ? layout.background() : texture;
    }

    @ModifyArg(method = "renderLabels", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"), index = 4)
    private int mekanism$labelColor(int color) {
        return CreativeTabBuilder.layout(selectedTab) == null ? color : SpecialColors.TEXT_TITLE.argb();
    }
}
