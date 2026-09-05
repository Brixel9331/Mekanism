package mekanism.client;

import mekanism.client.key.MekanismKeyHandler;
import mekanism.client.render.MekanismShaders;
import net.fabricmc.api.ClientModInitializer;

public final class MekanismClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MekanismKeyHandler.registerKeybindings();
        ClientRegistration.registerRenderers();
        ClientRegistration.registerLayer();
        ClientRegistration.registerContainers();
        ClientRegistration.registerParticleFactories();
        ClientRegistration.registerBlockColorHandlers();
        ClientRegistration.registerItemColorHandlers();
        ClientRegistration.registerClientReloadListeners();
        MekanismShaders.register();
    }
}
