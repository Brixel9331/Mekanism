package mekanism.common.integration;

import net.fabricmc.loader.api.FabricLoader;

import mekanism.common.integration.computer.FactoryRegistry;
import mekanism.common.integration.computer.computercraft.CCCapabilityHelper;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.integration.energy.EnergyCompatUtils;

/**
 * Hooks for Mekanism. Use to grab items or blocks out of different mods.
 *
 * @author AidanBrady
 */
public final class MekanismHooks {

    public static final String CC_MOD_ID = "computercraft";
    public static final String CRAFTTWEAKER_MOD_ID = "crafttweaker";
    public static final String CURIOS_MODID = "curios";
    public static final String DARK_MODE_EVERYWHERE_MODID = "darkmodeeverywhere";
    public static final String FLUX_NETWORKS_MOD_ID = "fluxnetworks";
    public static final String IC2_MOD_ID = "ic2";
    public static final String JEI_MOD_ID = "jei";
    public static final String JEITWEAKER_MOD_ID = "jeitweaker";
    public static final String JSON_THINGS_MOD_ID = "jsonthings";
    public static final String OC2_MOD_ID = "oc2";
    public static final String PROJECTE_MOD_ID = "projecte";
    public static final String RECIPE_STAGES_MOD_ID = "recipestages";
    public static final String TOP_MOD_ID = "theoneprobe";
    public static final String WILDFIRE_GENDER_MOD_ID = "wildfire_gender";

    public boolean CCLoaded;
    public boolean CraftTweakerLoaded;
    public boolean CuriosLoaded;
    public boolean DMELoaded;
    public boolean FluxNetworksLoaded;
    public boolean IC2Loaded;
    public boolean JEILoaded;
    public boolean WildfireGenderModLoaded;

    public void hookConstructor() {
        FabricLoader loader = FabricLoader.getInstance();
        CraftTweakerLoaded = loader.isModLoaded(CRAFTTWEAKER_MOD_ID);
        CuriosLoaded = loader.isModLoaded(CURIOS_MODID);
    }

    public void hookCommonSetup() {
        FabricLoader modList = FabricLoader.getInstance();
        CCLoaded = modList.isModLoaded(CC_MOD_ID);
        DMELoaded = modList.isModLoaded(DARK_MODE_EVERYWHERE_MODID);
        IC2Loaded = modList.isModLoaded(IC2_MOD_ID);
        JEILoaded = modList.isModLoaded(JEI_MOD_ID);
        FluxNetworksLoaded = modList.isModLoaded(FLUX_NETWORKS_MOD_ID);
        WildfireGenderModLoaded = modList.isModLoaded(WILDFIRE_GENDER_MOD_ID);
        if (computerCompatEnabled()) {
            FactoryRegistry.load();
            if (CCLoaded) {
                CCCapabilityHelper.registerApis();
            }
        }
        EnergyCompatUtils.initLoadedCache();

        if (CraftTweakerLoaded) {
            CrTContentUtils.registerCrTContent();
        }
    }

    public boolean computerCompatEnabled() {
        return CCLoaded;
    }

}
