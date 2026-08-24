package mekanism.generators.common.registries;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.Attributes.AttributeComparator;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registration.impl.CreativeTabRegistryObject;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

public class GeneratorsCreativeTabs {

    public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(MekanismGenerators.MODID, GeneratorsCreativeTabs::addToExistingTabs);

    public static final CreativeTabRegistryObject GENERATORS = CREATIVE_TABS.registerMain(GeneratorsLang.MEKANISM_GENERATORS, GeneratorsBlocks.HEAT_GENERATOR, builder ->
          builder.withBackgroundLocation(MekanismGenerators.rl("textures/gui/creative_tab.png"))
                .withSearchBar(50)//Allow our tabs to be searchable for convenience purposes
                .displayItems((displayParameters, output) -> {
                    CreativeTabDeferredRegister.addToDisplay(GeneratorsItems.ITEMS.getAllItems(), output);
                    CreativeTabDeferredRegister.addToDisplay(GeneratorsBlocks.BLOCKS.getAllBlocks(), output);
                    CreativeTabDeferredRegister.addToDisplay(GeneratorsFluids.FLUIDS.getAllFluids().stream().map(FluidRegistryObject::getBucket).toList(), output);
                })
    );

    private static void addToExistingTabs(ResourceKey<CreativeModeTab> tabKey, CreativeModeTab.Output output) {
         if (tabKey == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
             CreativeTabDeferredRegister.addToDisplay(output, GeneratorsBlocks.HEAT_GENERATOR, GeneratorsBlocks.SOLAR_GENERATOR, GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR,
                   GeneratorsBlocks.WIND_GENERATOR, GeneratorsBlocks.BIO_GENERATOR, GeneratorsBlocks.GAS_BURNING_GENERATOR);
        } else if (tabKey == CreativeModeTabs.REDSTONE_BLOCKS) {
            for (IBlockProvider block : GeneratorsBlocks.BLOCKS.getAllBlocks()) {
                if (Attribute.has(block.getBlock(), AttributeComparator.class)) {
                    CreativeTabDeferredRegister.addToDisplay(output, block);
                }
            }
        } else if (tabKey == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            CreativeTabDeferredRegister.addToDisplay(GeneratorsFluids.FLUIDS.getAllFluids().stream().map(FluidRegistryObject::getBucket).toList(), output);
        } else if (tabKey == CreativeModeTabs.INGREDIENTS) {
             CreativeTabDeferredRegister.addToDisplay(output, GeneratorsItems.HOHLRAUM, GeneratorsItems.SOLAR_PANEL);
         }
    }
}
