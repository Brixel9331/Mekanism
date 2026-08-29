package mekanism.common.lib;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.world.level.Level;

public final class WorldLifecycle {

    public static final Event<Listener> LOAD = EventFactory.createArrayBacked(Listener.class, listeners -> world -> {
        for (Listener listener : listeners) {
            listener.onWorld(world);
        }
    });
    public static final Event<Listener> UNLOAD = EventFactory.createArrayBacked(Listener.class, listeners -> world -> {
        for (Listener listener : listeners) {
            listener.onWorld(world);
        }
    });

    private static boolean registered;

    private WorldLifecycle() {
    }

    public static void register() {
        if (!registered) {
            ServerWorldEvents.LOAD.register((server, world) -> LOAD.invoker().onWorld(world));
            ServerWorldEvents.UNLOAD.register((server, world) -> UNLOAD.invoker().onWorld(world));
            registered = true;
        }
    }

    @FunctionalInterface
    public interface Listener {

        void onWorld(Level world);
    }
}
