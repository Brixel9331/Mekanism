package mekanism.tools.client.render;

import mekanism.tools.client.render.item.RenderMekanismShieldItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import mekanism.api.providers.IItemProvider;
import mekanism.tools.common.registries.ToolsItems;

//This class is used to prevent class loading issues on the server without having to use OnlyIn hacks
public class ToolsRenderPropertiesProvider {

    private ToolsRenderPropertiesProvider() {
    }

    public static void register() {
        for (IItemProvider item : new IItemProvider[]{ToolsItems.BRONZE_SHIELD, ToolsItems.LAPIS_LAZULI_SHIELD, ToolsItems.OSMIUM_SHIELD,
              ToolsItems.REFINED_GLOWSTONE_SHIELD, ToolsItems.REFINED_OBSIDIAN_SHIELD, ToolsItems.STEEL_SHIELD}) {
            BuiltinItemRendererRegistry.INSTANCE.register(item.asItem(), shield()::renderByItem);
        }
    }

    public static BlockEntityWithoutLevelRenderer shield() {
        return RenderMekanismShieldItem.RENDERER;
    }
}
