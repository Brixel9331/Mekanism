package mekanism.generators.client.render;

import mekanism.client.render.RenderPropertiesProvider.MekRenderProperties;
import mekanism.generators.client.render.item.RenderWindGeneratorItem;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import mekanism.generators.common.registries.GeneratorsBlocks;

//This class is used to prevent class loading issues on the server without having to use OnlyIn hacks
public class GeneratorsRenderPropertiesProvider {

    private GeneratorsRenderPropertiesProvider() {
    }

    public static void register() {
        BuiltinItemRendererRegistry.INSTANCE.register(GeneratorsBlocks.WIND_GENERATOR.asItem(), wind().renderer()::renderByItem);
    }

    public static MekRenderProperties wind() {
        return new MekRenderProperties(RenderWindGeneratorItem.RENDERER);
    }
}