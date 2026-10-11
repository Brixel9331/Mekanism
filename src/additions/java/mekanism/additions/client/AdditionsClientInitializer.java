package mekanism.additions.client;

import java.util.Collection;
import java.util.List;
import mekanism.additions.common.MekanismAdditions;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public final class AdditionsClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AdditionsClientRegistration.init();
        AdditionsClientRegistration.registerKeybindings();
        AdditionsClientRegistration.registerRenderers();
        AdditionsClientRegistration.registerLayer();
        AdditionsClientRegistration.registerBlockColorHandlers();
        AdditionsClientRegistration.registerItemColorHandlers();
        ModelLoadingPlugin.register(AdditionsClientRegistration::registerAdditionalModels);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return MekanismAdditions.rl("model_cache");
            }

            @Override
            public Collection<ResourceLocation> getFabricDependencies() {
                return List.of(ResourceReloadListenerKeys.MODELS);
            }

            @Override
            public void onResourceManagerReload(ResourceManager resources) {
                AdditionsClientRegistration.onModelBake(Minecraft.getInstance().getModelManager());
            }
        });
    }
}
