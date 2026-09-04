package mekanism.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public record ClientReloadListener(ResourceLocation id, PreparableReloadListener listener) implements IdentifiableResourceReloadListener {

    @Override
    public ResourceLocation getFabricId() {
        return id;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller preparationProfiler,
          ProfilerFiller reloadProfiler, Executor preparationExecutor, Executor reloadExecutor) {
        return listener.reload(barrier, manager, preparationProfiler, reloadProfiler, preparationExecutor, reloadExecutor);
    }
}
