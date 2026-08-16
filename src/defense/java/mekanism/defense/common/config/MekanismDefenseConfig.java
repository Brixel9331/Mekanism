package mekanism.defense.common.config;

import mekanism.common.config.MekanismConfigHelper;

public class MekanismDefenseConfig {

    private MekanismDefenseConfig() {
    }

    public static final DefenseConfig defense = new DefenseConfig();

    public static void registerConfigs() {
        MekanismConfigHelper.registerConfig("mekanismdefense", defense);
    }
}