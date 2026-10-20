package mekanism.tools.client;

import mekanism.api.providers.IItemProvider;
import mekanism.client.ClientRegistrationUtil;
import mekanism.tools.client.render.item.RenderMekanismShieldItem;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;

public class ToolsClientRegistration {

    private ToolsClientRegistration() {
    }

    public static void init() {
        addShieldPropertyOverrides(MekanismTools.rl("blocking"),
              (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F,
              ToolsItems.BRONZE_SHIELD, ToolsItems.LAPIS_LAZULI_SHIELD, ToolsItems.OSMIUM_SHIELD, ToolsItems.REFINED_GLOWSTONE_SHIELD,
              ToolsItems.REFINED_OBSIDIAN_SHIELD, ToolsItems.STEEL_SHIELD);
    }

    private static void addShieldPropertyOverrides(ResourceLocation override, ItemPropertyFunction propertyGetter, IItemProvider... shields) {
        for (IItemProvider shield : shields) {
            ClientRegistrationUtil.setPropertyOverride(shield, override, propertyGetter);
        }
    }

    public static void registerClientReloadListeners() {
        ClientRegistrationUtil.registerClientReloadListeners(RenderMekanismShieldItem.RENDERER);
    }
}