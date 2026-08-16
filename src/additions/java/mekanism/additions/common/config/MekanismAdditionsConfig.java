package mekanism.additions.common.config;

import mekanism.common.config.MekanismConfigHelper;

public class MekanismAdditionsConfig {

    private MekanismAdditionsConfig() {
    }

    public static final AdditionsConfig additions = new AdditionsConfig();
    public static final AdditionsClientConfig client = new AdditionsClientConfig();

    public static void registerConfigs() {
        MekanismConfigHelper.registerConfig("mekanismadditions", client);
        MekanismConfigHelper.registerConfig("mekanismadditions", additions);
    }
}