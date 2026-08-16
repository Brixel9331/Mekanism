package mekanism.tools.common.config;

import mekanism.common.config.MekanismConfigHelper;

public class MekanismToolsConfig {

    private MekanismToolsConfig() {
    }

    public static final ToolsConfig tools = new ToolsConfig();
    public static final ToolsClientConfig toolsClient = new ToolsClientConfig();

    public static void registerConfigs() {
        MekanismConfigHelper.registerConfig("mekanismtools", tools);
        MekanismConfigHelper.registerConfig("mekanismtools", toolsClient);
    }
}