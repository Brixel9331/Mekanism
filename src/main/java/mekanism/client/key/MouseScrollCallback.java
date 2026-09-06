package mekanism.client.key;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;

@FunctionalInterface
public interface MouseScrollCallback {

    Event<MouseScrollCallback> EVENT = EventFactory.createArrayBacked(MouseScrollCallback.class, listeners -> (client, delta) -> {
        for (MouseScrollCallback listener : listeners) {
            if (listener.onScroll(client, delta)) {
                return true;
            }
        }
        return false;
    });

    boolean onScroll(Minecraft client, double delta);
}
