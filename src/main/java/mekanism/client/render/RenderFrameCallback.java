package mekanism.client.render;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;

@FunctionalInterface
public interface RenderFrameCallback {

    Event<RenderFrameCallback> END = EventFactory.createArrayBacked(RenderFrameCallback.class, callbacks -> (client, partialTick) -> {
        for (RenderFrameCallback callback : callbacks) {
            callback.onRenderFrame(client, partialTick);
        }
    });

    void onRenderFrame(Minecraft client, float partialTick);
}
