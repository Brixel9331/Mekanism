package mekanism.generators.common.config;

import mekanism.common.config.MekanismConfigHelper;

public class MekanismGeneratorsConfig {

    private MekanismGeneratorsConfig() {
    }

    public static final GeneratorsConfig generators = new GeneratorsConfig();
    public static final GeneratorsGearConfig gear = new GeneratorsGearConfig();
    public static final GeneratorsStorageConfig storageConfig = new GeneratorsStorageConfig();

    public static void registerConfigs() {
        MekanismConfigHelper.registerConfig("mekanismgenerators", generators);
        MekanismConfigHelper.registerConfig("mekanismgenerators", gear);
        MekanismConfigHelper.registerConfig("mekanismgenerators", storageConfig);
    }
}