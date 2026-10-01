package mekanism.client;

import mekanism.client.key.MekanismKeyHandler;
import mekanism.client.model.ClientModelRegistration;
import mekanism.client.render.MekanismShaders;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.sound.SoundHandler;
import mekanism.common.lib.BlockEntityLoadQueue;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;

public final class MekanismClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register(BlockEntityLoadQueue::enqueue);
        ClientRegistration.init();
        ClientRegistration.registerModels();
        ClientRegistration.addLayers();
        mekanism.client.render.RenderPropertiesProvider.register();
        if (mekanism.common.Mekanism.hooks.TrinketsLoaded) {
            mekanism.client.render.MekanismTrinketRenderer.register();
        }
        MekanismKeyHandler.registerKeybindings();
        ClientTickHandler.register();
        RenderTickHandler.register();
        SoundHandler.register();
        ClientModelRegistration.register();
        ClientRegistration.registerRenderers();
        ClientRegistration.registerLayer();
        ClientRegistration.registerContainers();
        ClientRegistration.registerParticleFactories();
        ClientRegistration.registerBlockColorHandlers();
        ClientRegistration.registerItemColorHandlers();
        ClientRegistration.registerItemDecorations();
        ClientRegistration.registerOverlays();
        ClientRegistration.registerClientReloadListeners();
        MekanismShaders.register();
    }
}
