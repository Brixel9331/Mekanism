package mekanism.generators.client;

import mekanism.generators.client.render.GeneratorsRenderPropertiesProvider;
import net.fabricmc.api.ClientModInitializer;

public final class GeneratorsClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        GeneratorsClientRegistration.init();
        GeneratorsClientRegistration.registerRenderers();
        GeneratorsClientRegistration.registerLayer();
        GeneratorsClientRegistration.registerContainers();
        GeneratorsClientRegistration.registerClientReloadListeners();
        GeneratorsRenderPropertiesProvider.register();
    }
}
