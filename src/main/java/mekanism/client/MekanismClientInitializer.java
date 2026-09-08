package mekanism.client;

import mekanism.client.key.MekanismKeyHandler;
import mekanism.client.render.MekanismShaders;
import mekanism.client.sound.SoundHandler;
import net.fabricmc.api.ClientModInitializer;

public final class MekanismClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MekanismKeyHandler.registerKeybindings();
        ClientTickHandler.register();
        SoundHandler.register();
        ClientRegistration.registerRenderers();
        ClientRegistration.registerLayer();
        ClientRegistration.registerContainers();
        ClientRegistration.registerParticleFactories();
        ClientRegistration.registerBlockColorHandlers();
        ClientRegistration.registerItemColorHandlers();
        ClientRegistration.registerItemDecorations();
        ClientRegistration.registerClientReloadListeners();
        MekanismShaders.register();
    }
}
