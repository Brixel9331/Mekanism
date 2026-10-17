package mekanism.tools.client;

import mekanism.tools.client.render.ToolsRenderPropertiesProvider;
import net.fabricmc.api.ClientModInitializer;

public final class ToolsClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ToolsClientRegistration.init();
        ToolsClientRegistration.registerClientReloadListeners();
        ToolsRenderPropertiesProvider.register();
    }
}
