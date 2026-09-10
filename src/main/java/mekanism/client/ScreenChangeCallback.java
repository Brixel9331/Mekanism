package mekanism.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ScreenChangeCallback {

    Event<ScreenChangeCallback> EVENT = EventFactory.createArrayBacked(ScreenChangeCallback.class, listeners -> (client, previous, next) -> {
        for (ScreenChangeCallback listener : listeners) {
            listener.beforeChange(client, previous, next);
        }
    });

    void beforeChange(Minecraft client, @Nullable Screen previous, @Nullable Screen next);
}
