package mekanism.tools.common;

import mekanism.common.Mekanism;
import mekanism.common.base.IModModule;
import mekanism.common.lib.Version;
import mekanism.tools.common.config.MekanismToolsConfig;
import mekanism.tools.common.registries.ToolsCreativeTabs;
import mekanism.tools.common.registries.ToolsItems;
import mekanism.tools.common.registries.ToolsRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class MekanismTools implements IModModule, ModInitializer {

    public static final String MODID = "mekanismtools";

    public static MekanismTools instance;

    /**
     * MekanismTools version number
     */
    public final Version versionNumber;

    public MekanismTools() {
        Mekanism.initialize();
        Mekanism.addModule(instance = this);
        MekanismToolsConfig.registerConfigs();
        //Register the listener for special mob spawning (mobs with Mekanism armor/tools)

        ToolsItems.ITEMS.register();
        ToolsCreativeTabs.CREATIVE_TABS.register();
        ToolsRecipeSerializers.RECIPE_SERIALIZERS.register();
        //Set our version number to match the mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(FabricLoader.getInstance().getModContainer(MODID).orElseThrow());
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MekanismTools.MODID, path);
    }

    @Override
    public void onInitialize() {
        //Ensure our tags are all initialized
        ToolsTags.init();
        Mekanism.logger.info("Loaded 'Mekanism: Tools' module.");
    }

    @Override
    public Version getVersion() {
        return versionNumber;
    }

    @Override
    public String getName() {
        return "Tools";
    }

    @Override
    public void resetClient() {
    }
}
